package ru.kuchanov.scpquiz.controller.interactor

import io.reactivex.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import ru.kuchanov.scpquiz.controller.api.ApiClient
import ru.kuchanov.scpquiz.controller.db.AppDatabase
import ru.kuchanov.scpquiz.controller.manager.preference.MyPreferenceManager
import ru.kuchanov.scpquiz.model.db.QuizTransaction
import ru.kuchanov.scpquiz.model.db.TransactionType
import timber.log.Timber
import javax.inject.Inject

class TransactionInteractor @Inject constructor(
        private val appDatabase: AppDatabase,
        private val apiClient: ApiClient,
        private val preferences: MyPreferenceManager
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


    fun syncTransactions() =
            appDatabase.transactionDao().getAllWithoutExternalId()
                    .doOnSuccess { Timber.d("doOnSuccess :%s", it) }
                    .flatMap { listWithoutExtId ->
                        Timber.d("LIST WITHOUT ExtID:%s", listWithoutExtId)
                        Observable.fromIterable(listWithoutExtId)
                                .flatMapSingle { transaction ->
                                    Timber.d("Transaction:%s", transaction)
                                    makeTransaction(transaction.quizId, transaction.transactionType, transaction.coinsAmount).toSingleDefault(transaction)
                                }
                                .doOnNext { Timber.d("Transaction BEFORE TO LIST():%s", it) }
                                .toList()
                                .doOnSuccess { Timber.d("Transaction AFTER TO LIST:%s", it) }
                    }
                    .ignoreElement()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())

    private fun syncScoreWithServer() =
            Maybe.fromCallable {
                val updateSyncTransaction = appDatabase.transactionDao().getOneByTypeNoReactive(TransactionType.UPDATE_SYNC)
                return@fromCallable if (updateSyncTransaction.externalId == null) {
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
                                .doOnNext { Timber.d("DO ON NEXT syncFinishedLevels") }
                                .doOnComplete { Timber.d("DO ON COMPLETE syncFinishedLevels") }
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