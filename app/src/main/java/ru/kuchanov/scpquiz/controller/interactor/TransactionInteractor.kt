package ru.kuchanov.scpquiz.controller.interactor

import io.reactivex.Completable
import io.reactivex.Single
import ru.kuchanov.scpquiz.controller.api.ApiClient
import ru.kuchanov.scpquiz.controller.db.AppDatabase
import ru.kuchanov.scpquiz.model.db.QuizTransaction
import ru.kuchanov.scpquiz.model.db.TransactionType
import timber.log.Timber
import javax.inject.Inject

class TransactionInteractor @Inject constructor(
        private val appDatabase: AppDatabase,
        private val apiClient: ApiClient
) {
    fun makeTransaction(quizId: Long?, transactionType: TransactionType, coinsAmount: Int): Completable {
        return Single
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
    }
}