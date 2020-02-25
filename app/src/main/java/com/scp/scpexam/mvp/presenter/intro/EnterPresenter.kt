package com.scp.scpexam.mvp.presenter.intro

import android.annotation.SuppressLint
import android.app.Application
import android.graphics.Bitmap
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import moxy.InjectViewState
import com.scp.scpexam.BuildConfig
import com.scp.scpexam.Constants
import com.scp.scpexam.R
import com.scp.scpexam.controller.api.ApiClient
import com.scp.scpexam.controller.db.AppDatabase
import com.scp.scpexam.controller.interactor.TransactionInteractor
import com.scp.scpexam.controller.manager.preference.MyPreferenceManager
import com.scp.scpexam.controller.navigation.ScpRouter
import com.scp.scpexam.model.api.NwQuiz
import com.scp.scpexam.model.api.QuizConverter
import com.scp.scpexam.model.db.FinishedLevel
import com.scp.scpexam.model.db.User
import com.scp.scpexam.model.db.UserRole
import com.scp.scpexam.model.db.generateRandomName
import com.scp.scpexam.model.ui.ProgressPhrase
import com.scp.scpexam.model.ui.ProgressPhrasesJson
import com.scp.scpexam.mvp.presenter.BasePresenter
import com.scp.scpexam.mvp.view.intro.EnterView
import com.scp.scpexam.utils.BitmapUtils
import com.scp.scpexam.utils.StorageUtils
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@InjectViewState
class EnterPresenter @Inject constructor(
        override var appContext: Application,
        override var preferences: MyPreferenceManager,
        override var router: ScpRouter,
        override var appDatabase: AppDatabase,
        private val moshi: Moshi,
        private var quizConverter: QuizConverter,
        public override var apiClient: ApiClient,
        override var transactionInteractor: TransactionInteractor
) : BasePresenter<EnterView>(appContext, preferences, router, appDatabase, apiClient, transactionInteractor) {

    private var dbFilled: Boolean = false

    private var secondsPast: Long = 0

    private lateinit var progressPhrases: List<ProgressPhrase>

    @SuppressLint("CheckResult")
    override fun onFirstViewAttach() {
        super.onFirstViewAttach()

        readProgressPhrases()

        val timerObservable = Flowable.intervalRange(
                0,
                10,
                0,
                1050,
                TimeUnit.MILLISECONDS
        )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())

        val dbFillObservable = Single
                .fromCallable {
                    //                    Timber.d("read initial data from json")
                    val json = StorageUtils.readFromAssets(appContext, "baseData.json")
                    val type = Types.newParameterizedType(List::class.java, NwQuiz::class.java)
                    val adapter = moshi.adapter<List<NwQuiz>>(type)
                    adapter.fromJson(json)
                }
                .map { initialQuizes -> initialQuizes.sortedBy { it.id } }
                .map { initialQuizes ->
                    //                    Timber.d("write initial data to DB")

//                    Timber.d("write users:")
                    val doctorUser = User(
                            name = appContext.getString(R.string.doctor_name),
                            role = UserRole.DOCTOR
                    )
                    appDatabase.userDao().insert(doctorUser)

                    val playerUser = User(
                            name = generateRandomName(appContext),
                            role = UserRole.PLAYER
                    )
                    appDatabase.userDao().insert(playerUser)

//                    Timber.d("write quizes")
                    appDatabase.quizDao().insertQuizesWithQuizTranslations(
                            quizConverter.convertCollection(
                                    initialQuizes,
                                    quizConverter::convert
                            ))
                    appDatabase.finishedLevelsDao().insert(initialQuizes.mapIndexed { index, nwQuiz ->
                        //                        Timber.d("initialQuizes: $index, ${nwQuiz.id}")
                        FinishedLevel(
                                nwQuiz.id,
                                //first 5 levels must be available always
                                isLevelAvailable = index < 5
                        )
                    })
                    val langs = appDatabase.quizTranslationsDao().getAllLangs().toSet()
                    preferences.setLangs(langs)

                    preferences.setLang(getDefaultLang(langs))

                    -1L
                }

        val dbFillIfEmptyObservable = Single
                .fromCallable { appDatabase.quizDao().getCount() }
                .flatMap {
                    if (it != 0L) {
//                        Timber.d("data in DB already exists")
                        Single.just(-1L)
                    } else {
//                        Timber.d("fill DB with initial data")
                        dbFillObservable
                    }
                }

        Flowable.merge(timerObservable, dbFillIfEmptyObservable.toFlowable())
                .doOnNext {
                    if (it != -1L) {
                        secondsPast = it
                    }
                }
                .flatMap {
                    if (dbFilled && secondsPast > 2) {
                        Flowable.error(IllegalStateException("Stop condition is true"))
                    } else {
                        Flowable.just(it)
                    }
                }
                .onErrorResumeNext { error: Throwable ->
                    Timber.d("onErrorResumeNext: $error")
                    Flowable.empty()
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                        onNext = {
                            //                            Timber.d("onNext: $it")
                            if (it == -1L) {
                                dbFilled = true
                            } else {
                                viewState.showProgressText(progressPhrases[Random().nextInt(progressPhrases.size)].translation)
                                viewState.showProgressAnimation()
                                viewState.showImage(it.toInt())
                            }
                        },
                        onComplete = {
                            if (preferences.isIntroDialogShown()) {
                                router.newRootScreen(Constants.Screens.LevelsScreen)
                            } else {
                                viewState.onNeedToOpenIntroDialogFragment()
                            }
                        },
                        onError = Timber::e
                )
    }

    private fun readProgressPhrases() {
        val json = StorageUtils.readFromAssets(appContext, "progressPhrases.json")
        val adapter = moshi.adapter(ProgressPhrasesJson::class.java)
        val parsedJson = adapter.fromJson(json)!!
        //todo add translations
        progressPhrases = when (preferences.getLang()) {
            "ru" -> parsedJson.ru
            else -> parsedJson.en
        }
    }

    fun openIntroDialogScreen(bitmap: Bitmap) {
        Completable.fromAction {
            BitmapUtils.persistImage(
                    appContext,
                    bitmap,
                    Constants.INTRO_DIALOG_BACKGROUND_FILE_NAME)
        }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                        onComplete = {
                            if (preferences.isIntroDialogShown()) {
                                router.newRootScreen(Constants.Screens.LevelsScreen)
                            } else {
                                router.newRootScreen(Constants.Screens.IntroDialogScreen)
                            }
                        }
                )
    }

    private fun getDefaultLang(langs: Set<String>): String {
        var lang: String = Constants.DEFAULT_LANG
        langs.forEach {
            val curLangLocale = Locale(it)
            if (curLangLocale.language == Locale.getDefault().language) {
                lang = it
                return@forEach
            }
        }

        return lang
    }

    fun onProgressTextClicked() {
        //later there will be an easter egg
        @Suppress("ConstantConditionIf")
        if (BuildConfig.DEBUG) {
            val scoreToDecrease = 1000
            Completable.fromAction {
                with(appDatabase.userDao().getOneByRole(UserRole.PLAYER).blockingGet()) {
                    score += scoreToDecrease
                    appDatabase.userDao().update(this).toLong()
                }
            }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe()
        }
    }
}
