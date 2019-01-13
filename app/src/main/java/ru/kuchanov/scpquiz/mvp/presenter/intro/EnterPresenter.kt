package ru.kuchanov.scpquiz.mvp.presenter.intro

import android.annotation.SuppressLint
import android.app.Application
import android.content.Intent
import android.graphics.Bitmap
import android.support.v4.content.ContextCompat
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
import ru.kuchanov.scpquiz.controller.interactor.TransactionInteractor
import ru.kuchanov.scpquiz.controller.manager.preference.MyPreferenceManager
import ru.kuchanov.scpquiz.controller.navigation.ScpRouter
import ru.kuchanov.scpquiz.model.api.NwQuiz
import ru.kuchanov.scpquiz.model.api.QuizConverter
import ru.kuchanov.scpquiz.model.db.*
import ru.kuchanov.scpquiz.model.ui.ProgressPhrase
import ru.kuchanov.scpquiz.model.ui.ProgressPhrasesJson
import ru.kuchanov.scpquiz.mvp.presenter.BasePresenter
import ru.kuchanov.scpquiz.mvp.view.intro.EnterView
import ru.kuchanov.scpquiz.services.UploadService
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

        val dbFillIfEmptyObservable = Single
                .fromCallable { appDatabase.quizDao().getCount() }
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
                            val serviceIntent = Intent(appContext, UploadService::class.java)
                            ContextCompat.startForegroundService(appContext, serviceIntent)

                            if (preferences.isIntroDialogShown()) {
                                router.newRootScreen(Constants.Screens.QUIZ_LIST)
                            } else {
                                viewState.onNeedToOpenIntroDialogFragment()
                            }
                        },
                        onError = Timber::e
                )
        Single.fromCallable {
            if (appDatabase.transactionDao().getTransactionsCount() == 0) {
                syncScoreWithServer()
                syncFinishedLevels()
            }
        }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe()


    }

    private fun syncScoreWithServer() {
        Single.fromCallable {
            val quizTransaction = QuizTransaction(
                    quizId = null,
                    transactionType = TransactionType.UPDATE_SYNC,
                    coinsAmount = appDatabase.userDao().getOneByRole(UserRole.PLAYER).blockingGet().score
            )
            appDatabase.transactionDao().insert(quizTransaction)

        }
                .flatMapCompletable { quizTransactionId ->
                    apiClient.addTransaction(
                            null,
                            TransactionType.UPDATE_SYNC,
                            appDatabase.userDao().getOneByRole(UserRole.PLAYER).blockingGet().score
                    )
                            .doOnSuccess { nwQuizTransaction ->
                                appDatabase.transactionDao().updateQuizTransactionExternalId(
                                        quizTransactionId = quizTransactionId,
                                        quizTransactionExternalId = nwQuizTransaction.id)
                                Timber.d("GET TRANSACTION BY ID : %s", appDatabase.transactionDao().getOneById(quizTransactionId))
                            }
                            .ignoreElement()
                            .onErrorComplete()
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                        onError = {
                            Timber.e(it)
                            viewState.showMessage(it.message
                                    ?: "Unexpected error")
                        },
                        onComplete = {
                            viewState.showMessage("Sync score succeed")
                            Timber.d("Sync score succeed")
                        }
                )
    }

    /**
     * получаем все finishedLevel() ,  getAll()
     * отфильтровываем все с levelAvailable = true ,  filter()
     * преобразовываем список finishedLevel в список transactions, map()
     * пишем в БД, map()
     * отправляем на сервер, flatmap()
     * обновляем externalId doOnSuccess()
     */
    private fun syncFinishedLevels() {
        Timber.d("start Sync FinishedLevels")
        appDatabase.finishedLevelsDao().getAll()
                .map { finishedLevels -> finishedLevels.filter { it.isLevelAvailable } }
                .map { levelsAvailableTrue ->
                    val finishedLevelsToTransactions = arrayListOf<QuizTransaction>()
                    levelsAvailableTrue.forEach { levelAvailable ->
                        val quizId = levelAvailable.quizId
                        if (levelAvailable.nameRedundantCharsRemoved) {
                            finishedLevelsToTransactions.add(quizTransactionForMigration(quizId, TransactionType.NAME_CHARS_REMOVED_MIGRATION))
                        }
                        if (levelAvailable.numberRedundantCharsRemoved) {
                            finishedLevelsToTransactions.add(quizTransactionForMigration(quizId, TransactionType.NUMBER_CHARS_REMOVED_MIGRATION))
                        }
                        if (levelAvailable.scpNameFilled) {
                            finishedLevelsToTransactions.add(quizTransactionForMigration(quizId, TransactionType.NAME_ENTERED_MIGRATION))
                        }
                        if (levelAvailable.scpNumberFilled) {
                            finishedLevelsToTransactions.add(quizTransactionForMigration(quizId, TransactionType.NUMBER_ENTERED_MIGRATION))
                        } else {
                            finishedLevelsToTransactions.add(quizTransactionForMigration(quizId, TransactionType.LEVEL_AVAILABLE_MIGRATION))
                        }
                    }
                    Timber.d("finishedLevelsToTransactions.toList() : %s", finishedLevelsToTransactions.toList())
                    return@map finishedLevelsToTransactions.toList()
                }
                .map { quizTransactionList ->
                    Timber.d("quizTransactionList :%s", quizTransactionList)
                    appDatabase.transactionDao().insertQuizTransactionList(quizTransactionList)

                }
                .flatMapIterable { it }
                .flatMapSingle { localId ->
                    Timber.d("Local IDS :%s", localId)

                    return@flatMapSingle apiClient.addTransaction(
                            quizId = appDatabase.transactionDao().getOneById(localId).quizId,
                            typeTransaction = appDatabase.transactionDao().getOneById(localId).transactionType,
                            coinsAmount = appDatabase.transactionDao().getOneById(localId).coinsAmount
                    )
                            .doOnSuccess { nwQuizTransaction ->
                                Timber.d("OnSuccess :%s", nwQuizTransaction)
                                appDatabase.transactionDao().updateQuizTransactionExternalId(
                                        quizTransactionId = localId,
                                        quizTransactionExternalId = nwQuizTransaction.id)
                                Timber.d("GET TRANSACTION BY ID : %s", appDatabase.transactionDao().getOneById(localId))
                            }
                }
                .toList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                        onError = {
                            Timber.e(it, "Error Finished Levels")
                            viewState.showMessage(it.message
                                    ?: "Unexpected error")
                        },
                        onSuccess = {
                            Timber.d("Finished Levels into transactions succeed: $it")
                            viewState.showMessage("Finished Levels into transactions succeed")
                        }
                )
    }

    private fun quizTransactionForMigration(quizId: Long, transactionType: TransactionType): QuizTransaction =
            QuizTransaction(
                    coinsAmount = 0,
                    quizId = quizId,
                    transactionType = transactionType
            )

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