package com.scp.scpexam.mvp.presenter.monetization

import android.app.Application
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.SkuDetails
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Function3
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import moxy.InjectViewState
import com.scp.scpexam.Constants
import com.scp.scpexam.R
import com.scp.scpexam.controller.adapter.MyListItem
import com.scp.scpexam.controller.adapter.viewmodel.MonetizationHeaderViewModel
import com.scp.scpexam.controller.adapter.viewmodel.MonetizationViewModel
import com.scp.scpexam.controller.api.ApiClient
import com.scp.scpexam.controller.db.AppDatabase
import com.scp.scpexam.controller.interactor.TransactionInteractor
import com.scp.scpexam.controller.manager.monetization.BillingDelegate
import com.scp.scpexam.controller.manager.preference.MyPreferenceManager
import com.scp.scpexam.model.db.User
import com.scp.scpexam.model.db.UserRole
import com.scp.scpexam.mvp.presenter.BasePresenter
import com.scp.scpexam.mvp.view.monetization.MonetizationView
import ru.terrakok.cicerone.Router
import timber.log.Timber
import javax.inject.Inject

@InjectViewState
class MonetizationPresenter @Inject constructor(
        override var appContext: Application,
        override var preferences: MyPreferenceManager,
        override var router: Router,
        override var appDatabase: AppDatabase,
        public override var apiClient: ApiClient,
        override var transactionInteractor: TransactionInteractor
) : BasePresenter<MonetizationView>(appContext, preferences, router, appDatabase, apiClient, transactionInteractor) {

    var billingDelegate: BillingDelegate? = null

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()

        billingDelegate?.startConnection()
    }

    private fun buyInApp(sku: String) {
        Timber.d("buyInApp")
        if (preferences.getTrueAccessToken() != null || sku != Constants.SKU_INAPP_DISABLE_ADS) {
            billingDelegate?.startPurchaseFlow(sku)
        } else {
            viewState.showMessage(appContext.getString(R.string.need_to_login))
        }
    }

    private fun showAppodealAds() {
        Timber.d("showAppodealAds")
        viewState.onNeedToShowRewardedVideo()
    }

    fun onNavigationIconClicked() = router.exit()

    fun loadInAppsToBuy(force: Boolean) {
        if (force) {
            billingDelegate?.apply {
                userInAppHistory()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnSubscribe { view?.showProgress(true) }
                        .doOnEvent { _, _ -> view?.showProgress(false) }
                        .subscribeBy(
                                onSuccess = {
                                    Timber.d("loadInAppsToBuy force: $it")
                                    onBillingClientReady()
                                },
                                onError = {
                                    Timber.e(it, "error while force update users purchases")
                                    view?.showMessage(it.message
                                            ?: context.getString(R.string.error_unexpected))
                                }
                        )
                        .addTo(compositeDisposable)
            }
        } else {
            onBillingClientReady()
        }
    }

    fun onBillingClientReady() {
        Flowable
                .combineLatest(
                        appDatabase.userDao().getByRoleWithUpdates(UserRole.PLAYER).map { it.first() },
                        billingDelegate!!.loadInAppsToBuy().toFlowable(),
                        billingDelegate!!.getAllUserOwnedPurchases()
                                .doOnSuccess { purchaseList ->
                                    purchaseList.filter { it.sku != Constants.SKU_INAPP_DISABLE_ADS }.forEach {
                                        billingDelegate?.writeAndConsumePurchase(it)
                                                ?.subscribeOn(Schedulers.io())
                                                ?.observeOn(AndroidSchedulers.mainThread())
                                                ?.subscribeBy(
                                                        onComplete = {
                                                            Timber.d("Purchase consumed: $it")
                                                        },
                                                        onError = {
                                                            Timber.e(it)
                                                        }
                                                )
                                    }
                                }
                                .toFlowable(),
                        Function3 { player: User, listSkuDetails: List<SkuDetails>, allUsersOwnedPurchases: List<Purchase> ->
                            Triple(player, listSkuDetails, allUsersOwnedPurchases)
                        }
                )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { viewState?.showProgress(true) }
                .doOnNext { viewState.showProgress(false) }
                .subscribeBy(
                        onNext = { tripple ->
                            val disableAdsInApp = tripple.third.firstOrNull { it.sku == Constants.SKU_INAPP_DISABLE_ADS }
                            preferences.disableAds(disableAdsInApp != null)

                            val items = mutableListOf<MyListItem>()
                            items += MonetizationHeaderViewModel(tripple.first)
                            items += MonetizationViewModel(
                                    R.drawable.ic_no_money,
                                    appContext.getString(R.string.monetization_action_appodeal_title),
                                    appContext.getString(R.string.monetization_action_appodeal_description, Constants.REWARD_VIDEO_ADS),
                                    "FREE",
                                    null,
                                    false
                            ) { showAppodealAds() }

                            items += tripple.second.map { skuDetails ->
                                MonetizationViewModel(
                                        if (skuDetails.sku == Constants.SKU_INAPP_DISABLE_ADS) {
                                            R.drawable.ic_adblock
                                        } else {
                                            R.drawable.ic_coin
                                        }
                                        ,
                                        skuDetails.title,
                                        skuDetails.description,
                                        skuDetails.price,
                                        skuDetails.sku,
                                        if (skuDetails.sku == Constants.SKU_INAPP_DISABLE_ADS) {
                                            disableAdsInApp != null
                                        } else {
                                            false
                                        }
                                ) { buyInApp(skuDetails.sku) }
                            }

                            viewState.showMonetizationActions(items)

                            viewState.showRefreshFab(true)
                        },
                        onError = {
                            Timber.e(it)
                            viewState.showProgress(false)
                            viewState.showMessage(it.message
                                    ?: appContext.getString(R.string.error_unknown))
                            viewState.showRefreshFab(true)
                        }
                )
                .addTo(compositeDisposable)
    }

    fun onBillingClientFailedToStart(@BillingClient.BillingResponse billingResponseCode: Int) {
        viewState.showProgress(false)
        viewState.showMessage(appContext.getString(R.string.error_billing_client_connection, billingResponseCode))
        viewState.showRefreshFab(true)
    }

    fun onOwnedItemClicked(sku: String) {
        billingDelegate?.apply {
            consumeInAppIfUserHasIt(sku)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSubscribe { view?.showProgress(true) }
                    .doOnEvent { view?.showProgress(false) }
                    .subscribeBy(
                            onComplete = {
                                Timber.d("Successfully consume in app")
                                view?.showMessage("Successfully consume inApp!")
                                loadInAppsToBuy(true)
                            },
                            onError = {
                                Timber.e(it, "Error while consume inApp")
                                view?.showMessage("Error while consume inApp: ${it.message}")
                            }
                    )
                    .addTo(compositeDisposable)
        }
    }
}