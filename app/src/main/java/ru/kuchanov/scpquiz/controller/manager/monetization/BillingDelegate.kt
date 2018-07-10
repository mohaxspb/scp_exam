package ru.kuchanov.scpquiz.controller.manager.monetization

import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import com.android.billingclient.api.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import ru.kuchanov.scpquiz.Constants
import ru.kuchanov.scpquiz.R
import ru.kuchanov.scpquiz.controller.api.ApiClient
import ru.kuchanov.scpquiz.controller.api.response.GOOGLE_SERVER_ERROR
import ru.kuchanov.scpquiz.controller.api.response.INVALID
import ru.kuchanov.scpquiz.controller.api.response.VALID
import ru.kuchanov.scpquiz.di.Di
import ru.kuchanov.scpquiz.mvp.view.monetization.MonetizationView
import timber.log.Timber
import toothpick.Toothpick
import javax.inject.Inject

class BillingDelegate(
    val activity: AppCompatActivity,
    val view: MonetizationView
) : PurchasesUpdatedListener {

    @Inject
    lateinit var preferenceManager: PreferenceManager

    @Inject
    lateinit var apiClient: ApiClient

    private var billingClient: BillingClient = BillingClient.newBuilder(activity).setListener(this).build()

    private var clientReady = false

    private var disableAdsInApp: SkuDetails? = null

    init {
        Toothpick.inject(this, Toothpick.openScope(Di.Scope.APP))
    }

    fun startConnection() = billingClient.startConnection(object : BillingClientStateListener {
        override fun onBillingSetupFinished(@BillingClient.BillingResponse billingResponseCode: Int) {
            Timber.d("billingClient onBillingSetupFinished: $billingResponseCode")
            if (billingResponseCode == BillingClient.BillingResponse.OK) {
                clientReady = true
                // The billing client is ready. You can query purchases here.

                loadInAppsToBuy()
            }
        }

        override fun onBillingServiceDisconnected() {
            Timber.d("billingClient onBillingServiceDisconnected")
            // Try to restart the connection on the next request to
            // Google Play by calling the startConnection() method.
            clientReady = false
        }
    })

    override fun onPurchasesUpdated(responseCode: Int, purchases: MutableList<Purchase>?) {
        Timber.d("onPurchasesUpdated: $responseCode, $purchases")

        if (responseCode == BillingClient.BillingResponse.OK && purchases != null) {
            for (purchase in purchases) {
                apiClient.validateInApp(purchase.sku, purchase.purchaseToken)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeBy(
                            onSuccess = {
                                Timber.d("validation request response: $it")

                                when (it) {
                                    VALID -> {
                                        //todo apply premium
                                    }
                                    INVALID -> {
                                        view.showMessage(R.string.purchase_not_valid)
                                    }
                                    GOOGLE_SERVER_ERROR -> {
                                        view.showMessage(R.string.purchase_validation_google_error)
                                    }
                                }
                            },
                            onError = {
                                Timber.e(it)
                                view.showMessage(R.string.error_buy)
                            }
                        )

            }
        } else if (responseCode == BillingClient.BillingResponse.USER_CANCELED) {
            // Handle an error caused by a user cancelling the purchase flow.
            //nothing to do
        } else {
            // Handle any other error codes.
            view.showMessage(activity.getString(R.string.error_purchase, responseCode.toString()))
        }
    }

    //todo wrap in flowable
    private fun loadInAppsToBuy() {
        val skuList = listOf(Constants.SKU_INAPP_DISABLE_ADS)
        val params = SkuDetailsParams.newBuilder()
                .setSkusList(skuList)
                .setType(BillingClient.SkuType.INAPP)
                .build()
        billingClient.querySkuDetailsAsync(params) { responseCode, skuDetailsList ->
            Timber.d("inapps: $responseCode, $skuDetailsList")
            if (responseCode == BillingClient.BillingResponse.OK && skuDetailsList != null) {
                for (skuDetails in skuDetailsList) {
                    if (skuDetails.sku == Constants.SKU_INAPP_DISABLE_ADS) {
                        disableAdsInApp = skuDetails
                        view.enableBuyButton(disableAdsInApp!!)
                    } else {
                        Timber.wtf("unexpected sku: $skuDetails")
                    }
                }
            }
        }
    }

    fun startPurchaseFlow(sku: String): Boolean {
        val flowParams = BillingFlowParams.newBuilder()
                .setSku(sku)
                .setType(BillingClient.SkuType.INAPP)
                .build()
        val responseCode = billingClient.launchBillingFlow(activity, flowParams)
        Timber.d("startPurchaseFlow responseCode $responseCode")

        return responseCode == BillingClient.BillingResponse.OK
    }
}