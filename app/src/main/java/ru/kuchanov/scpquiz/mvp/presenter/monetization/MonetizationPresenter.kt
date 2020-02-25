package ru.kuchanov.scpquiz.mvp.presenter.monetization

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
import ru.kuchanov.scpquiz.Constants
import ru.kuchanov.scpquiz.R
import ru.kuchanov.scpquiz.controller.adapter.MyListItem
import ru.kuchanov.scpquiz.controller.adapter.viewmodel.MonetizationHeaderViewModel
import ru.kuchanov.scpquiz.controller.adapter.viewmodel.MonetizationViewModel
import ru.kuchanov.scpquiz.controller.api.ApiClient
import ru.kuchanov.scpquiz.controller.db.AppDatabase
import ru.kuchanov.scpquiz.controller.interactor.TransactionInteractor
import ru.kuchanov.scpquiz.controller.manager.monetization.BillingDelegate
import ru.kuchanov.scpquiz.controller.manager.preference.MyPreferenceManager
import ru.kuchanov.scpquiz.controller.navigation.ScpRouter
import ru.kuchanov.scpquiz.model.db.User
import ru.kuchanov.scpquiz.model.db.UserRole
import ru.kuchanov.scpquiz.mvp.presenter.BasePresenter
import ru.kuchanov.scpquiz.mvp.view.monetization.MonetizationView
import timber.log.Timber
import javax.inject.Inject

@InjectViewState
class MonetizationPresenter @Inject constructor(
        override var appContext: Application,
        override var preferences: MyPreferenceManager,
        override var router: ScpRouter,
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