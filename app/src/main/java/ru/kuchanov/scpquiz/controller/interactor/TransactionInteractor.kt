package ru.kuchanov.scpquiz.controller.interactor

import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import ru.kuchanov.scpquiz.controller.api.ApiClient
import ru.kuchanov.scpquiz.controller.db.AppDatabase
import ru.kuchanov.scpquiz.model.db.QuizTransaction
import ru.kuchanov.scpquiz.model.db.TransactionType
import ru.kuchanov.scpquiz.model.db.UserRole
import timber.log.Timber
import javax.inject.Inject

class TransactionInteractor @Inject constructor(
        private val appDatabase: AppDatabase,
        private val apiClient: ApiClient
) {
    fun makeTransaction(quizId: Long?, transactionType: TransactionType, coinsAmount: Int) =
            Single
                    .fromCallable {
                        val quizTransaction = QuizTransaction(
                                quizId = quizId,
                                transactionType = transactionType,
                                coinsAmount = coinsAmount
                        )
                        appDatabase.transactionDao().insert(quizTransaction)
                    }
                    .flatMapCompletable { quizTransactionId ->
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

    fun syncAllProgress() =
            syncScoreWithServer().andThen(syncFinishedLevels().onErrorComplete())

    fun syncScoreWithServer() =
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


    /**
     * получаем все finishedLevel() ,  getAll()
     * отфильтровываем все с levelAvailable = true ,  filter()
     * преобразовываем список finishedLevel в список transactions, map()
     * пишем в БД, map()
     * отправляем на сервер, flatmap()
     * обновляем externalId doOnSuccess()
     */
    fun syncFinishedLevels() =
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
                    .flatMapSingle { localIds ->
                        Flowable
                                .fromIterable(localIds)
                                .flatMap {
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
                                            .toFlowable()
                                }
                                .toList()
                    }
                    .firstOrError()
                    .ignoreElement()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())

    private fun quizTransactionForMigration(quizId: Long, transactionType: TransactionType) =
            QuizTransaction(
                    coinsAmount = 0,
                    quizId = quizId,
                    transactionType = transactionType
            )
}