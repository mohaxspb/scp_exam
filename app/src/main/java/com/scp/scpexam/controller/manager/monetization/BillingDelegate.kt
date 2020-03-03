package com.scp.scpexam.controller.manager.monetization

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.android.billingclient.api.*
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import com.scp.scpexam.Constants
import com.scp.scpexam.R
import com.scp.scpexam.controller.api.ApiClient
import com.scp.scpexam.controller.api.response.GOOGLE_SERVER_ERROR
import com.scp.scpexam.controller.api.response.INVALID
import com.scp.scpexam.controller.api.response.VALID
import com.scp.scpexam.controller.db.AppDatabase
import com.scp.scpexam.controller.manager.preference.MyPreferenceManager
import com.scp.scpexam.di.Di
import com.scp.scpexam.model.db.QuizTransaction
import com.scp.scpexam.model.db.TransactionType
import com.scp.scpexam.model.db.UserRole
import com.scp.scpexam.mvp.presenter.monetization.MonetizationPresenter
import com.scp.scpexam.mvp.view.monetization.MonetizationView
import timber.log.Timber
import toothpick.Toothpick
import javax.inject.Inject

@SuppressWarnings("Injectable")
class BillingDelegate(
        val activity: AppCompatActivity?,
        val view: MonetizationView?,
        val presenter: MonetizationPresenter?
) : PurchasesUpdatedListener {

    @Inject
    lateinit var preferencesManager: MyPreferenceManager

    @Inject
    lateinit var apiClient: ApiClient

    @Inject
    lateinit var context: Context

    @Inject
    lateinit var appDatabase: AppDatabase

    private var billingClient: BillingClient = BillingClient.newBuilder(activity!!).setListener(this).build()

    private var clientReady = false

    init {
        Toothpick.inject(this, Toothpick.openScope(Di.Scope.APP))
    }

    fun startConnection() {
        view?.showProgress(true)
        view?.showRefreshFab(false)
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(@BillingClient.BillingResponse billingResponseCode: Int) {
                Timber.d("billingClient onBillingSetupFinished: $billingResponseCode")
                if (billingResponseCode == BillingClient.BillingResponse.OK) {
                    clientReady = true
                    // The billing client is ready. You can query purchases here.

                    presenter?.onBillingClientReady()

                } else {
                    clientReady = false
                    presenter?.onBillingClientFailedToStart(billingResponseCode)
                }
            }

            override fun onBillingServiceDisconnected() {
                Timber.d("billingClient onBillingServiceDisconnected")
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
                clientReady = false
                presenter?.onBillingClientFailedToStart(BillingClient.BillingResponse.ERROR)
            }
        })
    }

    override fun onPurchasesUpdated(responseCode: Int, purchases: MutableList<Purchase>?) {
        Timber.d("onPurchasesUpdated: $responseCode, $purchases")

        if (responseCode == BillingClient.BillingResponse.OK && purchases != null) {
            for (purchase in purchases) {
                if (purchase.sku == Constants.SKU_INAPP_DISABLE_ADS) {
                    apiClient.validateInApp(purchase.sku, purchase.purchaseToken)
                            .flatMap {
                                when (it) {
                                    VALID -> return@flatMap Single.just(it)
                                    INVALID -> return@flatMap Single.error<Int>(IllegalStateException(context.getString(R.string.purchase_not_valid)))
                                    GOOGLE_SERVER_ERROR -> return@flatMap Single.error<Int>(IllegalStateException(context.getString(R.string.purchase_validation_google_error)))
                                    else -> return@flatMap Single.error<Int>(IllegalStateException(context.getString(R.string.error_buy)))
                                }
                            }
                            .map {
                                val quizTransaction = QuizTransaction(
                                        quizId = null,
                                        transactionType = TransactionType.ADV_BUY_NEVER_SHOW,
                                        coinsAmount = Constants.COINS_FOR_ADS_DISABLE
                                )
                                return@map appDatabase.transactionDao().insert(quizTransaction)
                            }
                            .flatMapCompletable { quizTransactionId ->
                                apiClient.addTransaction(
                                        null,
                                        TransactionType.ADV_BUY_NEVER_SHOW,
                                        Constants.COINS_FOR_ADS_DISABLE
                                )
                                        .doOnSuccess { nwQuizTransaction ->
                                            appDatabase.transactionDao().updateQuizTransactionExternalId(
                                                    quizTransactionId = quizTransactionId,
                                                    quizTransactionExternalId = nwQuizTransaction.id)
                                            //Timber.d("GET TRANSACTION BY ID : %s", appDatabase.transactionDao().getOneById(quizTransactionId))
                                        }
                                        .ignoreElement()
                                        .onErrorComplete()
                            }
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeBy(
                                    onComplete = {
                                        //Timber.d("on Complete purchase")
                                        preferencesManager.disableAds(true)
                                        view?.showMessage(R.string.ads_disabled)
                                        presenter?.loadInAppsToBuy(true)
                                    },
                                    onError = {
                                        Timber.e(it)
                                        view?.showMessage(it.message.toString())
                                    }
                            )
                } else {
                    apiClient.validateInApp(purchase.sku, purchase.purchaseToken)
                            .flatMap {
                                when (it) {
                                    VALID -> return@flatMap Single.just(it)
                                    INVALID -> return@flatMap Single.error<Int>(IllegalStateException(context.getString(R.string.purchase_not_valid)))
                                    GOOGLE_SERVER_ERROR -> return@flatMap Single.error<Int>(IllegalStateException(context.getString(R.string.purchase_validation_google_error)))
                                    else -> return@flatMap Single.error<Int>(IllegalStateException(context.getString(R.string.error_buy)))
                                }
                            }
                            .flatMapCompletable { writeAndConsumePurchase(purchase) }
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeBy(
                                    onComplete = {
                                        view?.showMessage(R.string.success_purchase)
                                        presenter?.loadInAppsToBuy(true)
                                    },
                                    onError = {
                                        Timber.e(it)
                                        view?.showMessage(it.message.toString())
                                    }
                            )
                }
            }
        } else if (responseCode == BillingClient.BillingResponse.USER_CANCELED) {
            Timber.d("User cancelled purchase: $responseCode")
            // Handle an error caused by a user cancelling the purchase flow.
            //nothing to do
        } else {
            // Handle any other error codes.
            Timber.e("Error while onPurchasesUpdated: $responseCode")
            view?.showMessage(context.getString(R.string.error_purchase, responseCode.toString()))
        }
    }

    fun writeAndConsumePurchase(purchase: Purchase) =
            apiClient.addInAppPurchase(
                    purchase.sku,
                    purchase.purchaseTime,
                    purchase.purchaseToken,
                    purchase.orderId,
                    when (purchase.sku) {
                        Constants.SKU_INAPP_BUY_COINS_0 -> Constants.COINS_FOR_SKU_INAPP_0
                        Constants.SKU_INAPP_BUY_COINS_1 -> Constants.COINS_FOR_SKU_INAPP_1
                        Constants.SKU_INAPP_BUY_COINS_2 -> Constants.COINS_FOR_SKU_INAPP_2
                        Constants.SKU_INAPP_BUY_COINS_3 -> Constants.COINS_FOR_SKU_INAPP_3
                        else -> throw (IllegalStateException(context.getString(R.string.error_buy)))
                    }
            )
                    .map { nwQuizTransaction ->
                        val insertedTransaction = appDatabase.transactionDao().getOneByExternalId(nwQuizTransaction.id)

                        if (insertedTransaction == null) {
                            appDatabase.userDao().getOneByRoleSync(UserRole.PLAYER)?.apply {
                                this.score += nwQuizTransaction.coinsAmount!!
                                appDatabase.userDao().update(this)
                            }

                            appDatabase.transactionDao().insert(QuizTransaction(
                                    quizId = null,
                                    transactionType = TransactionType.INAPP_PURCHASE,
                                    externalId = nwQuizTransaction.id,
                                    coinsAmount = nwQuizTransaction.coinsAmount
                            ))
                        } else {
                            insertedTransaction.id
                        }
                    }
                    .flatMapCompletable { insertedTransactionId ->
                        val insertedTransaction = appDatabase.transactionDao().getOneById(insertedTransactionId)
                        consumeInApp(purchase.purchaseToken)
                                .observeOn(Schedulers.io())
                                .doOnError {
                                    appDatabase.userDao().getOneByRoleSync(UserRole.PLAYER)?.apply {
                                        score -= insertedTransaction.coinsAmount!!
                                        appDatabase.userDao().update(this)
                                    }
                                    appDatabase.transactionDao().delete(insertedTransaction)
                                }
                    }

    fun loadInAppsToBuy(): Single<List<SkuDetails>> =
            Single
                    .create<List<SkuDetails>> { emitter ->
                        val skuList = listOf(
                                Constants.SKU_INAPP_DISABLE_ADS,
                                Constants.SKU_INAPP_BUY_COINS_0,
                                Constants.SKU_INAPP_BUY_COINS_1,
                                Constants.SKU_INAPP_BUY_COINS_2,
                                Constants.SKU_INAPP_BUY_COINS_3
                        )
                        val params = SkuDetailsParams.newBuilder()
                                .setSkusList(skuList)
                                .setType(BillingClient.SkuType.INAPP)
                                .build()
                        if (clientReady) {
                            billingClient.querySkuDetailsAsync(params) { responseCode, skuDetailsList ->
                                //Timber.d("inapps: $responseCode, $skuDetailsList")
                                if (responseCode == BillingClient.BillingResponse.OK && skuDetailsList != null) {
                                    emitter.onSuccess(skuDetailsList)
                                } else {
                                    emitter.onError(IllegalStateException("skuDetail with sku not found"))
                                }
                            }
                        } else {
                            emitter.onError(IllegalStateException(context.getString(R.string.error_billing_client_not_ready)))
                        }
                    }

    fun startPurchaseFlow(sku: String): Boolean =
            if (clientReady) {
                val flowParams = BillingFlowParams.newBuilder()
                        .setSku(sku)
                        .setType(BillingClient.SkuType.INAPP)
                        .build()
                val responseCode = billingClient.launchBillingFlow(activity, flowParams)
                Timber.d("startPurchaseFlow responseCode $responseCode")

                if (responseCode == BillingClient.BillingResponse.ITEM_ALREADY_OWNED) {
                    getAllUserOwnedPurchases()
                            .flatMapObservable { purchaseList ->
                                Observable.fromIterable(purchaseList)
                            }
                            .flatMapCompletable { purchase -> writeAndConsumePurchase(purchase) }
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeBy(
                                    onComplete = {
                                        startPurchaseFlow(sku)
                                    },
                                    onError = {
                                        Timber.e(it)
                                        view?.showMessage(it.message.toString())
                                    }
                            )
                }
                responseCode == BillingClient.BillingResponse.OK
            } else {
                view?.showMessage(R.string.error_billing_client_not_ready)
                false
            }

    fun getAllUserOwnedPurchases(): Single<List<Purchase>> =
            Single
                    .fromCallable {
                        if (clientReady) {
                            billingClient.queryPurchases(BillingClient.SkuType.INAPP)
                        } else {
                            throw IllegalStateException(context.getString(R.string.error_billing_client_not_ready))
                        }
                    }
                    .flatMap { purchasesResult ->
                        Timber.d("purchasesResult: ${purchasesResult.purchasesList}")

                        if (purchasesResult.purchasesList == null) {
                            Single.just(listOf())
                        } else {
                            Flowable.fromIterable(purchasesResult.purchasesList)
                                    .flatMapSingle { purchase ->
                                        apiClient
                                                .validateInApp(purchase.sku, purchase.purchaseToken)
                                                .doOnSuccess { Timber.d("getAllUserOwnedPurchases validateInApp doOnSuccess: $it") }
                                                .map { Pair(purchase, it == VALID) }
                                    }
                                    .toList()
                                    .map { listOfPair ->
                                        listOfPair.filter { pair -> pair.second == true }
                                                .map { it.first }
                                    }
                        }
                    }

    fun consumeInAppIfUserHasIt(sku: String): Completable =
            userInAppHistory()
                    .map { inApps -> inApps.first { it.sku == sku }.purchaseToken }
                    .flatMapCompletable { consumeInApp(it) }

    @Suppress("UNUSED_ANONYMOUS_PARAMETER")
    private fun consumeInApp(purchaseTokenToConsume: String): Completable =
            Completable.create {
                if (clientReady) {
                    billingClient.consumeAsync(purchaseTokenToConsume) { responseCode, purchaseToken ->
                        when (responseCode) {
                            BillingClient.BillingResponse.OK -> it.onComplete()
                            else -> it.onError(IllegalStateException("Error while consume inapp. Code: $responseCode"))
                        }
                    }
                } else {
                    throw IllegalStateException(context.getString(R.string.error_billing_client_not_ready))
                }
            }

    fun userInAppHistory(): Single<List<Purchase>> =
            Single.create {
                if (clientReady) {
                    billingClient.queryPurchaseHistoryAsync(BillingClient.SkuType.INAPP) { responseCode, purchasesList ->
                        when (responseCode) {
                            BillingClient.BillingResponse.OK -> it.onSuccess(purchasesList)
                            else -> it.onError(IllegalStateException("Error while get userInAppHistory. Code: $responseCode"))
                        }
                    }
                } else {
                    throw IllegalStateException(context.getString(R.string.error_billing_client_not_ready))
                }
            }
}