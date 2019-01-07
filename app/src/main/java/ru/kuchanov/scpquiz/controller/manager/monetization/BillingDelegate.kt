package ru.kuchanov.scpquiz.controller.manager.monetization

import android.arch.persistence.room.Database
import android.content.Context
import android.support.v7.app.AppCompatActivity
import com.android.billingclient.api.*
import io.reactivex.BackpressureStrategy
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import ru.kuchanov.scpquiz.Constants
import ru.kuchanov.scpquiz.R
import ru.kuchanov.scpquiz.controller.api.ApiClient
import ru.kuchanov.scpquiz.controller.api.response.GOOGLE_SERVER_ERROR
import ru.kuchanov.scpquiz.controller.api.response.INVALID
import ru.kuchanov.scpquiz.controller.api.response.VALID
import ru.kuchanov.scpquiz.controller.db.AppDatabase
import ru.kuchanov.scpquiz.controller.manager.preference.MyPreferenceManager
import ru.kuchanov.scpquiz.di.Di
import ru.kuchanov.scpquiz.model.db.QuizTransaction
import ru.kuchanov.scpquiz.model.db.TransactionType
import ru.kuchanov.scpquiz.mvp.presenter.monetization.MonetizationPresenter
import ru.kuchanov.scpquiz.mvp.view.monetization.MonetizationView
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

                    if (!preferencesManager.isAdsDisabled()) {
                        isHasDisableAdsInApp()
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribeBy { preferencesManager.disableAds(it) }
                    }
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
                                        Timber.d("GET TRANSACTION BY ID : %s", appDatabase.transactionDao().getOneById(quizTransactionId))
                                    }
                                    .ignoreElement()
                                    .onErrorComplete()
                        }
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeBy(
                                onComplete = {
                                    Timber.d("on Complete purchase")
                                    preferencesManager.disableAds(true)
                                    view?.showMessage(R.string.ads_disabled)
                                },
                                onError = {
                                    Timber.e(it)
                                    view?.showMessage(it.message.toString())
                                }
                        )
            }
        } else if (responseCode == BillingClient.BillingResponse.USER_CANCELED) {
            // Handle an error caused by a user cancelling the purchase flow.
            //nothing to do
        } else {
            // Handle any other error codes.
            view?.showMessage(context.getString(R.string.error_purchase, responseCode.toString())
                    ?: "Error")
        }
    }

    fun loadInAppsToBuy(): Flowable<SkuDetails> = Flowable.create<SkuDetails>({
        val skuList = listOf(Constants.SKU_INAPP_DISABLE_ADS)
        val params = SkuDetailsParams.newBuilder()
                .setSkusList(skuList)
                .setType(BillingClient.SkuType.INAPP)
                .build()
        billingClient.querySkuDetailsAsync(params) { responseCode, skuDetailsList ->
            Timber.d("inapps: $responseCode, $skuDetailsList")
            if (responseCode == BillingClient.BillingResponse.OK && skuDetailsList != null) {
                val disableAdsInapp = skuDetailsList.firstOrNull { it.sku == Constants.SKU_INAPP_DISABLE_ADS }
                if (disableAdsInapp != null) {
                    it.onNext(disableAdsInapp)
                    it.onComplete()
                } else {
                    it.onError(IllegalStateException("skuDetail with sku not found"))
                }
            }
        }
    }, BackpressureStrategy.BUFFER)

    fun startPurchaseFlow(sku: String): Boolean {
        return if (clientReady) {
            val flowParams = BillingFlowParams.newBuilder()
                    .setSku(sku)
                    .setType(BillingClient.SkuType.INAPP)
                    .build()
            val responseCode = billingClient.launchBillingFlow(activity, flowParams)
            Timber.d("startPurchaseFlow responseCode $responseCode")

            responseCode == BillingClient.BillingResponse.OK
        } else {
            view?.showMessage(R.string.error_billing_client_not_ready)
            false
        }
    }

    fun isHasDisableAdsInApp(): Flowable<Boolean> = Flowable.fromCallable { billingClient.queryPurchases(BillingClient.SkuType.INAPP) }
            .flatMap { purchasesResult ->
                val disableAdsInApp = purchasesResult.purchasesList?.firstOrNull { it.sku == Constants.SKU_INAPP_DISABLE_ADS }
                if (disableAdsInApp == null) {
                    Flowable.just(false)
                } else {
                    apiClient
                            .validateInApp(disableAdsInApp.sku, disableAdsInApp.purchaseToken)
                            .map { it == VALID }
                            .toFlowable()
                }
            }
}