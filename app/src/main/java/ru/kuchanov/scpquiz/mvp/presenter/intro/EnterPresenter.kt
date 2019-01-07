package ru.kuchanov.scpquiz.mvp.presenter.intro

import android.app.Application
import android.graphics.Bitmap
import com.arellomobile.mvp.InjectViewState
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import ru.kuchanov.scpquiz.BuildConfig
import ru.kuchanov.scpquiz.Constants
import ru.kuchanov.scpquiz.R
import ru.kuchanov.scpquiz.controller.api.ApiClient
import ru.kuchanov.scpquiz.controller.db.AppDatabase
import ru.kuchanov.scpquiz.controller.manager.preference.MyPreferenceManager
import ru.kuchanov.scpquiz.controller.navigation.ScpRouter
import ru.kuchanov.scpquiz.model.api.NwQuiz
import ru.kuchanov.scpquiz.model.api.QuizConverter
import ru.kuchanov.scpquiz.model.db.FinishedLevel
import ru.kuchanov.scpquiz.model.db.User
import ru.kuchanov.scpquiz.model.db.UserRole
import ru.kuchanov.scpquiz.model.ui.ProgressPhrase
import ru.kuchanov.scpquiz.model.ui.ProgressPhrasesJson
import ru.kuchanov.scpquiz.mvp.presenter.BasePresenter
import ru.kuchanov.scpquiz.mvp.view.intro.EnterView
import ru.kuchanov.scpquiz.utils.BitmapUtils
import ru.kuchanov.scpquiz.utils.StorageUtils
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
        public override var apiClient: ApiClient
) : BasePresenter<EnterView>(appContext, preferences, router, appDatabase) {

    private var dbFilled: Boolean = false

    private var secondsPast: Long = 0

    private lateinit var progressPhrases: List<ProgressPhrase>

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

        val dbFillObservable = Single.fromCallable {
            Timber.d("read initial data from json")
            val json = StorageUtils.readFromAssets(appContext, "baseData.json")
            val type = Types.newParameterizedType(List::class.java, NwQuiz::class.java)
            val adapter = moshi.adapter<List<NwQuiz>>(type)
            adapter.fromJson(json)
        }
                .map { initialQuizes -> initialQuizes.sortedBy { it.id } }
                .map { initialQuizes ->
                    Timber.d("write initial data to DB")

                    Timber.d("write users:")
                    val doctorUser = User(
                            name = appContext.getString(R.string.doctor_name),
                            role = UserRole.DOCTOR
                    )
                    appDatabase.userDao().insert(doctorUser)

                    val playerUser = User(
                            name = appContext.getString(R.string.player_name, Random().nextInt(10000)),
                            role = UserRole.PLAYER
                    )
                    appDatabase.userDao().insert(playerUser)

                    Timber.d("write quizes")
                    appDatabase.quizDao().insertQuizesWithQuizTranslations(
                            quizConverter.convertCollection(
                                    initialQuizes,
                                    quizConverter::convert
                            ))
                    appDatabase.finishedLevelsDao().insert(initialQuizes.mapIndexed { index, nwQuiz ->
                        Timber.d("initialQuizes: $index, ${nwQuiz.id}")
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

        val dbFillIfEmptyObservable = Single.fromCallable { appDatabase.quizDao().getCount() }
                .flatMap {
                    if (it != 0L) {
                        Timber.d("data in DB already exists")
                        Single.just(-1L)
                    } else {
                        Timber.d("fill DB with initial data")
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
                    if (dbFilled && secondsPast > 2) Flowable.error(IllegalStateException()) else Flowable.just(it)
                }
                .onErrorResumeNext { _: Throwable -> Flowable.empty() }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                        onNext = {
                            Timber.d("onNext: $it")
                            if (it == -1L) {
                                dbFilled = true
                            } else {
                                viewState.showProgressText(progressPhrases[Random().nextInt(progressPhrases.size)].translation)
                                viewState.showProgressAnimation()
                                viewState.showImage(it.toInt())
                            }
                        },
                        onComplete = {
                            Timber.d("onComplete")
                            if (preferences.isIntroDialogShown()) {
                                router.newRootScreen(Constants.Screens.QUIZ_LIST)
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
                        onComplete = { router.newRootScreen(Constants.Screens.INTRO_DIALOG) }
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