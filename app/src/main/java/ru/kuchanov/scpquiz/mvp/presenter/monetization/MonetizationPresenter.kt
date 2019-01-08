package ru.kuchanov.scpquiz.mvp.presenter.monetization

import android.app.Application
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.SkuDetails
import com.arellomobile.mvp.InjectViewState
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Function3
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import ru.kuchanov.scpquiz.Constants
import ru.kuchanov.scpquiz.R
import ru.kuchanov.scpquiz.controller.adapter.MyListItem
import ru.kuchanov.scpquiz.controller.adapter.viewmodel.MonetizationHeaderViewModel
import ru.kuchanov.scpquiz.controller.adapter.viewmodel.MonetizationViewModel
import ru.kuchanov.scpquiz.controller.api.ApiClient
import ru.kuchanov.scpquiz.controller.db.AppDatabase
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
        public override var apiClient: ApiClient
) : BasePresenter<MonetizationView>(appContext, preferences, router, appDatabase, apiClient) {

    var billingDelegate: BillingDelegate? = null

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()

        billingDelegate?.startConnection()
    }

    private fun buyNoAdsInApp() {
        Timber.d("buyNoAdsInApp")
        billingDelegate?.startPurchaseFlow(Constants.SKU_INAPP_DISABLE_ADS)
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
                        billingDelegate!!.isHasDisableAdsInApp().toFlowable(),
                        Function3 { player: User, disableAdsSkuDetails: SkuDetails, isHasDisableAds: Boolean ->
                            Triple(player, disableAdsSkuDetails, isHasDisableAds)
                        }
                )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { viewState?.showProgress(true) }
                .doOnNext { viewState.showProgress(false) }
                .subscribeBy(
                        onNext = {
                            val hasDisableAdsInApp = it.third
                            preferences.disableAds(hasDisableAdsInApp)

                            val items = mutableListOf<MyListItem>()
                            items += MonetizationHeaderViewModel(it.first)
                            items += MonetizationViewModel(
                                    R.drawable.ic_no_money,
                                    appContext.getString(R.string.monetization_action_appodeal_title),
                                    appContext.getString(R.string.monetization_action_appodeal_description, Constants.REWARD_VIDEO_ADS),
                                    "FREE",
                                    null,
                                    false
                            ) { showAppodealAds() }
                            items += MonetizationViewModel(
                                    R.drawable.ic_adblock,
                                    appContext.getString(R.string.monetization_action_noads_title),
                                    appContext.getString(R.string.monetization_action_noads_description),
                                    it.second.price,
                                    it.second.sku,
                                    hasDisableAdsInApp
                            ) { buyNoAdsInApp() }

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