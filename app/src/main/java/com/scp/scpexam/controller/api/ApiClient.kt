package com.scp.scpexam.controller.api

import io.reactivex.Single
import com.scp.scpexam.App
import com.scp.scpexam.BuildConfig
import com.scp.scpexam.Constants
import com.scp.scpexam.controller.api.response.TokenResponse
import com.scp.scpexam.model.api.NwQuizTransaction
import com.scp.scpexam.model.api.NwUser
import com.scp.scpexam.model.db.TransactionType
import javax.inject.Inject


class ApiClient @Inject constructor(
        private val quizApi: QuizApi,
        private val authApi: AuthApi,
        private val transactionApi: TransactionApi
) {

    fun validateInApp(sku: String, purchaseToken: String): Single<Int> =
            authApi
                    .validatePurchase(
                            false,
                            App.INSTANCE.packageName,
                            sku,
                            purchaseToken
                    )

    fun getNwQuizList() = quizApi.getNwQuizList()

    fun socialLogin(provider: Constants.Social, tokenValue: String): Single<TokenResponse> =
            authApi
                    .socialLogin(
                            provider,
                            tokenValue,
                            BuildConfig.CLIENT_ID,
                            BuildConfig.CLIENT_SECRET,
                            Constants.GAME_NEW
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