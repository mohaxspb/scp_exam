package ru.kuchanov.scpquiz.controller.api

import io.reactivex.Single
import ru.kuchanov.scpquiz.App
import ru.kuchanov.scpquiz.BuildConfig
import ru.kuchanov.scpquiz.Constants
import ru.kuchanov.scpquiz.controller.api.response.TokenResponse
import ru.kuchanov.scpquiz.model.api.NwInAppPurchase
import ru.kuchanov.scpquiz.model.api.NwQuiz
import ru.kuchanov.scpquiz.model.api.NwQuizTransaction
import ru.kuchanov.scpquiz.model.api.NwUser
import ru.kuchanov.scpquiz.model.db.TransactionType
import javax.inject.Inject


class ApiClient @Inject constructor(
        private val toolsApi: ToolsApi,
        private val quizApi: QuizApi,
        private val authApi: AuthApi,
        private val transactionApi: TransactionApi
) {

    //todo create method on quiz api (I mean server)
    fun validateInApp(sku: String, purchaseToken: String): Single<Int> =
            toolsApi
                    .validatePurchase(
                            false,
                            App.INSTANCE.packageName,
                            sku,
                            purchaseToken
                    )
                    .map { it.status }

    fun getNwQuizList() = quizApi.getNwQuizList()

    fun socialLogin(provider: Constants.Social, tokenValue: String): Single<TokenResponse> =
            authApi
                    .socialLogin(
                            provider,
                            tokenValue,
                            BuildConfig.CLIENT_ID,
                            BuildConfig.CLIENT_SECRET,
                            Constants.GAME
                    )

    fun getNwQuizTransactionList(): Single<List<NwQuizTransaction>> =
            transactionApi.getNwQuizTransactionList()

    fun addTransaction(quizId: Long?, typeTransaction: TransactionType, coinsAmount: Int?): Single<NwQuizTransaction> =
            transactionApi.addTransaction(
                    quizId,
                    typeTransaction,
                    coinsAmount,
                    System.currentTimeMillis().toString()
            )

    fun addInAppPurchase(skuId: String, purchaseTime: Long, purchaseToken: String, orderId: String, coinsAmount: Int): Single<NwQuizTransaction> =
            transactionApi.addInAppPurchase(
                    skuId,
                    purchaseTime,
                    purchaseToken,
                    orderId,
                    coinsAmount
            )

    fun resetProgress(): Single<Int> = transactionApi.resetProgress()

    fun getNwUser(): Single<NwUser> = transactionApi.getNwUser()

    fun getUserForLeaderboard(): Single<NwUser> = transactionApi.getUserForLeaderboard()

    fun getLeaderboard(offset: Int, limit: Int): Single<List<NwUser>> = authApi.getLeaderboard(offset, limit)

    fun getCurrentPositionInLeaderboard(): Single<Int> = transactionApi.getCurrentPositionInLeaderboard()
}