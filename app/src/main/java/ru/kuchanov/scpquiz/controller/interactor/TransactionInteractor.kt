package ru.kuchanov.scpquiz.controller.interactor

import io.reactivex.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import ru.kuchanov.scpquiz.controller.api.ApiClient
import ru.kuchanov.scpquiz.controller.db.AppDatabase
import ru.kuchanov.scpquiz.controller.manager.preference.MyPreferenceManager
import ru.kuchanov.scpquiz.model.api.QuizConverter
import ru.kuchanov.scpquiz.model.db.FinishedLevel
import ru.kuchanov.scpquiz.model.db.QuizTransaction
import ru.kuchanov.scpquiz.model.db.TransactionType
import timber.log.Timber
import javax.inject.Inject

class TransactionInteractor @Inject constructor(
        private val appDatabase: AppDatabase,
        private val apiClient: ApiClient,
        private val preferences: MyPreferenceManager,
        private val converter: QuizConverter
) {

    fun makeTransaction(quizId: Long?, transactionType: TransactionType, coinsAmount: Int?) =
            Single
                    .fromCallable {
                        val quizTransaction = QuizTransaction(
                                quizId = quizId,
                                transactionType = transactionType,
                                coinsAmount = coinsAmount
                        )
                        appDatabase.transactionDao().insert(quizTransaction).also { Timber.d("LOCAL DB TRANSACTION :%s", appDatabase.transactionDao().getOneById(it)) }
                    }
                    .flatMapCompletable { quizTransactionId ->
                        if (preferences.getAccessToken() == null) {
                            Completable.complete()
                        } else {
                            apiClient.addTransaction(
                                    quizId,
                                    transactionType,
                                    coinsAmount
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
                    }

    fun syncAllProgress() = Maybe.fromCallable {
        if (preferences.getAccessToken() != null) {
            true
        } else {
            null
        }
    }
            .flatMapCompletable { syncScoreWithServer() }
            .andThen(syncFinishedLevels())
            .andThen(getProgressFromServer())

    private fun getProgressFromServer() =
            apiClient.getNwQuizTransactionList()
                    .map { nwTransactionList ->
                        nwTransactionList.forEach { nwQuizTransaction ->
                            Timber.d("ALL LOCAL TRANSACTIONS :%s", appDatabase.transactionDao().getAllList())
                            Timber.d("ALL LOCAL FINISHED LEVELS :%s", appDatabase.finishedLevelsDao().getAllList())
                            Timber.d("nwQuizTransaction.quizTransactionType :%s", nwQuizTransaction.quizTransactionType)
                            Timber.d("TransactionType.NAME_WITH_PRICE :%s", TransactionType.NAME_WITH_PRICE)
                            val finishedLevel = appDatabase.finishedLevelsDao().getByQuizId(nwQuizTransaction.quizId!!)
                            val quizTransactionFromBd = appDatabase.transactionDao().getOneByQuizIdAndTransactionType(nwQuizTransaction.quizId!!, nwQuizTransaction.quizTransactionType)

                            /**
                            Этот блок не исполняется
                             */
                            when (nwQuizTransaction.quizTransactionType) {
                                TransactionType.NAME_WITH_PRICE, TransactionType.NAME_NO_PRICE -> {
                                    Timber.d(" LevelFinished:%s", finishedLevel)
                                    appDatabase.finishedLevelsDao().update(finishedLevel.apply {
                                        scpNameFilled = true
                                        isLevelAvailable = true
                                    })
                                }
                                TransactionType.NUMBER_WITH_PRICE, TransactionType.NUMBER_NO_PRICE -> {
                                    Timber.d(" LevelFinished:%s", finishedLevel)
                                    appDatabase.finishedLevelsDao().update(finishedLevel.apply {
                                        scpNumberFilled = true
                                        isLevelAvailable = true
                                    })
                                }
                                TransactionType.NAME_CHARS_REMOVED -> {
                                    Timber.d(" LevelFinished:%s", finishedLevel)
                                    appDatabase.finishedLevelsDao().update(finishedLevel.apply {
                                        nameRedundantCharsRemoved = true
                                        isLevelAvailable = true
                                    })
                                }
                                TransactionType.NUMBER_CHARS_REMOVED -> {
                                    Timber.d(" LevelFinished:%s", finishedLevel)
                                    appDatabase.finishedLevelsDao().update(finishedLevel.apply {
                                        numberRedundantCharsRemoved = true
                                        isLevelAvailable = true
                                    })
                                }
                                TransactionType.LEVEL_ENABLE_FOR_COINS -> {
                                    Timber.d(" LevelFinished:%s", finishedLevel)
                                    appDatabase.finishedLevelsDao().update(finishedLevel.apply {
                                        isLevelAvailable = true
                                    })
                                }

                                else -> Timber.d("Type is not LevelFinished:%s", finishedLevel)
                            }

//                            if (nwQuizTransaction.quizTransactionType.toString() == TransactionType.NAME_WITH_PRICE.toString() || nwQuizTransaction.quizTransactionType.toString() == TransactionType.NAME_NO_PRICE.toString()) {
//                                appDatabase.finishedLevelsDao().update(finishedLevel.apply {
//                                    scpNameFilled = true
//                                    isLevelAvailable = true
//                                })
//                                Timber.d("Finished level :%s", finishedLevel)
//                            }
//
//                            if (nwQuizTransaction.quizTransactionType.toString() == TransactionType.UPDATE_SYNC.toString()) {
//                                Timber.d("Finished level :%s", finishedLevel)
//                            }
//
//                            if (nwQuizTransaction.quizTransactionType.toString() == TransactionType.NUMBER_WITH_PRICE.toString() || nwQuizTransaction.quizTransactionType.toString() == TransactionType.NUMBER_NO_PRICE.toString()) {
//                                appDatabase.finishedLevelsDao().update(finishedLevel.apply {
//                                    scpNumberFilled = true
//                                    isLevelAvailable = true
//                                })
//                                Timber.d("Finished level :%s", finishedLevel)
//                            }

//                            if (nwQuizTransaction.quizTransactionType.toString() == TransactionType.NAME_CHARS_REMOVED.toString()) {
//                                appDatabase.finishedLevelsDao().update(finishedLevel.apply {
//                                    nameRedundantCharsRemoved = true
//                                    isLevelAvailable = true
//                                })
//                                Timber.d("Finished level :%s", finishedLevel)
//                            }

//                            if (nwQuizTransaction.quizTransactionType.toString() == TransactionType.NUMBER_CHARS_REMOVED.toString()) {
//                                appDatabase.finishedLevelsDao().update(finishedLevel.apply {
//                                    numberRedundantCharsRemoved = true
//                                    isLevelAvailable = true
//                                })
//                                Timber.d("Finished level :%s", finishedLevel)
//                            }

//                            if (nwQuizTransaction.quizTransactionType.toString() == TransactionType.LEVEL_ENABLE_FOR_COINS.toString()) {
//                                appDatabase.finishedLevelsDao().update(finishedLevel.apply {
//                                    isLevelAvailable = true
//                                })
//                                Timber.d("Finished level :%s", finishedLevel)
//                            }
                            /**
                             Этот блок исполняется
                             */
                            if (quizTransactionFromBd == null) {
                                appDatabase.transactionDao().insert(QuizTransaction(
                                        quizId = nwQuizTransaction.quizId,
                                        externalId = nwQuizTransaction.id,
                                        coinsAmount = nwQuizTransaction.coinsAmount,
                                        createdOnClient = nwQuizTransaction.createdOnClient,
                                        transactionType = nwQuizTransaction.quizTransactionType
                                ))
                            } else
                                if (quizTransactionFromBd.externalId == null) {
                                    appDatabase.transactionDao().updateQuizTransactionExternalId(quizTransactionFromBd.id!!, nwQuizTransaction.id)
                                }
                        }
                    }
                    .ignoreElement()
                    .onErrorComplete()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())


    fun syncTransactions() =
            appDatabase.transactionDao().getAllWithoutExternalId()
                    .flatMap { listWithoutExtId ->
                        Observable.fromIterable(listWithoutExtId)
                                .flatMapSingle { transaction ->
                                    apiClient.addTransaction(
                                            transaction.quizId,
                                            transaction.transactionType,
                                            transaction.coinsAmount
                                    )
                                            .doOnSuccess { nwTransaction ->
                                                appDatabase.transactionDao().updateQuizTransactionExternalId(
                                                        transaction.id!!,
                                                        nwTransaction.id
                                                )
                                            }
                                }
                                .toList()
                    }
                    .ignoreElement()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())

    private fun syncScoreWithServer() =
            Maybe.fromCallable {
                val updateSyncTransaction = appDatabase.transactionDao().getOneByTypeNoReactive(TransactionType.UPDATE_SYNC)
                return@fromCallable if (updateSyncTransaction?.externalId == null) {
                    updateSyncTransaction
                } else {
                    null
                }
            }
                    .flatMapCompletable { quizTransaction ->
                        apiClient.addTransaction(
                                quizTransaction.quizId,
                                quizTransaction.transactionType,
                                quizTransaction.coinsAmount
                        )
                                .doOnSuccess { nwQuizTransaction ->
                                    appDatabase.transactionDao().updateQuizTransactionExternalId(
                                            quizTransactionId = quizTransaction.id!!,
                                            quizTransactionExternalId = nwQuizTransaction.id)
                                    Timber.d("GET TRANSACTION BY ID : %s", appDatabase.transactionDao().getOneById(quizTransaction.id))
                                }
                                .ignoreElement()
                                .onErrorComplete()
                    }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())


    /**
     * получаем все finishedLevel() ,  getAll()
     * отфильтровываем все с levelAvailable = true ,  filter()
     * преобразовываем список finishedLevel в список transactions, map()
     * пишем в БД, map()
     * отправляем на сервер, flatmap()
     * обновляем externalId doOnSuccess()
     */
    private fun syncFinishedLevels() =
            appDatabase.finishedLevelsDao().getAllSingle()
                    .map { finishedLevels -> finishedLevels.filter { it.isLevelAvailable } }
                    .map { levelsAvailableTrue ->
                        val finishedLevelsToTransactions = arrayListOf<QuizTransaction>()
                        levelsAvailableTrue.forEach { levelAvailableFinishedLevel ->
                            val quizId = levelAvailableFinishedLevel.quizId
                            if (levelAvailableFinishedLevel.nameRedundantCharsRemoved
                                    && appDatabase.transactionDao().getOneByQuizIdAndTransactionType(quizId, TransactionType.NAME_CHARS_REMOVED) == null
                                    && appDatabase.transactionDao().getOneByQuizIdAndTransactionType(quizId, TransactionType.NAME_CHARS_REMOVED_MIGRATION) == null) {
                                finishedLevelsToTransactions.add(quizTransactionForMigration(quizId, TransactionType.NAME_CHARS_REMOVED_MIGRATION))
                            }
                            if (levelAvailableFinishedLevel.numberRedundantCharsRemoved
                                    && appDatabase.transactionDao().getOneByQuizIdAndTransactionType(quizId, TransactionType.NUMBER_CHARS_REMOVED) == null
                                    && appDatabase.transactionDao().getOneByQuizIdAndTransactionType(quizId, TransactionType.NUMBER_CHARS_REMOVED_MIGRATION) == null) {
                                finishedLevelsToTransactions.add(quizTransactionForMigration(quizId, TransactionType.NUMBER_CHARS_REMOVED_MIGRATION))
                            }
                            if (levelAvailableFinishedLevel.scpNameFilled
                                    && appDatabase.transactionDao().getOneByQuizIdAndTransactionType(quizId, TransactionType.NAME_WITH_PRICE) == null
                                    && appDatabase.transactionDao().getOneByQuizIdAndTransactionType(quizId, TransactionType.NAME_NO_PRICE) == null
                                    && appDatabase.transactionDao().getOneByQuizIdAndTransactionType(quizId, TransactionType.NAME_ENTERED_MIGRATION) == null) {
                                finishedLevelsToTransactions.add(quizTransactionForMigration(quizId, TransactionType.NAME_ENTERED_MIGRATION))
                            }
                            if (levelAvailableFinishedLevel.scpNumberFilled
                                    && appDatabase.transactionDao().getOneByQuizIdAndTransactionType(quizId, TransactionType.NUMBER_WITH_PRICE) == null
                                    && appDatabase.transactionDao().getOneByQuizIdAndTransactionType(quizId, TransactionType.NUMBER_NO_PRICE) == null
                                    && appDatabase.transactionDao().getOneByQuizIdAndTransactionType(quizId, TransactionType.NUMBER_ENTERED_MIGRATION) == null) {
                                finishedLevelsToTransactions.add(quizTransactionForMigration(quizId, TransactionType.NUMBER_ENTERED_MIGRATION))
                            }
                            if (appDatabase.transactionDao().getOneByQuizIdAndTransactionType(quizId, TransactionType.LEVEL_ENABLE_FOR_COINS) == null
                                    && appDatabase.transactionDao().getOneByQuizIdAndTransactionType(quizId, TransactionType.LEVEL_AVAILABLE_MIGRATION) == null) {
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
                    .flatMap { localIds ->
                        Flowable
                                .fromIterable(localIds)
                                .flatMapSingle {
                                    apiClient
                                            .addTransaction(
                                                    quizId = appDatabase.transactionDao().getOneById(it).quizId,
                                                    typeTransaction = appDatabase.transactionDao().getOneById(it).transactionType,
                                                    coinsAmount = appDatabase.transactionDao().getOneById(it).coinsAmount
                                            )
                                            .doOnSuccess { nwQuizTransaction ->
                                                Timber.d("OnSuccess :%s", nwQuizTransaction)
                                                appDatabase.transactionDao().updateQuizTransactionExternalId(
                                                        quizTransactionId = it,
                                                        quizTransactionExternalId = nwQuizTransaction.id)
                                                Timber.d("GET TRANSACTION BY ID : %s", appDatabase.transactionDao().getOneById(it))
                                            }
                                }
                                .toList()
                    }
                    .ignoreElement()
                    .onErrorComplete()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())

    private fun quizTransactionForMigration(quizId: Long, transactionType: TransactionType) =
            QuizTransaction(
                    coinsAmount = 0,
                    quizId = quizId,
                    transactionType = transactionType
            )
}