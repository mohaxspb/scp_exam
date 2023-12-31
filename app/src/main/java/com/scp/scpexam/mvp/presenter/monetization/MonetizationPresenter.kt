package com.scp.scpexam.mvp.presenter.monetization

import android.app.Application
import android.content.Intent
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.SkuDetails
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
import com.scp.scpexam.mvp.AuthPresenter
import com.scp.scpexam.mvp.presenter.BasePresenter
import com.scp.scpexam.mvp.view.monetization.MonetizationView
import com.scp.scpexam.ui.fragment.monetization.MonetizationFragment
import com.scp.scpexam.ui.utils.AuthDelegate
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Function3
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import moxy.InjectViewState
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
) : BasePresenter<MonetizationView>(
    appContext,
    preferences,
    router,
    appDatabase,
    apiClient,
    transactionInteractor
),
    AuthPresenter<MonetizationFragment> {

    var billingDelegate: BillingDelegate? = null

    private val monetizationItems = mutableListOf<MyListItem>()

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()

        billingDelegate?.startConnection()
    }

    private fun buyInApp(sku: String) {
        if (!preferences.getTrueAccessToken().isNullOrEmpty()) {
            billingDelegate?.startPurchaseFlow(sku)
        } else {
            viewState.showMessage(appContext.getString(R.string.need_to_login))
            viewState.scrollToTop()
        }
    }

    private fun showAppodealAds() {
        Timber.d("showAppodealAds")
        viewState.onNeedToShowRewardedVideo()
    }

    override fun onAuthSuccess() {
        preferences.setIntroDialogShown(true)
        viewState.showMessage(R.string.settings_success_auth)
        (monetizationItems.first() as MonetizationHeaderViewModel).showAuthButtons = false
        viewState.showMonetizationActions(monetizationItems)
    }

    override fun onAuthCanceled() {
        viewState.showMessage(R.string.canceled_auth)
    }

    override fun onAuthError() {
        viewState.showMessage(appContext.getString(R.string.auth_retry))
    }

    override lateinit var authDelegate: AuthDelegate<MonetizationFragment>

    override fun getAuthView(): MonetizationView = viewState

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
                            view?.showMessage(
                                it.message
                                    ?: context.getString(R.string.error_unexpected)
                            )
                        }
                    )
                    .addTo(compositeDisposable)
            }
        } else {
            onBillingClientReady()
        }
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        authDelegate.onActivityResult(requestCode, resultCode, data)
    }

    fun onBillingClientReady() {
        Flowable
            .combineLatest(
                appDatabase.userDao().getByRoleWithUpdates(UserRole.PLAYER).map { it.first() },
                billingDelegate!!.loadInAppsToBuy().toFlowable(),
                billingDelegate!!.getAllUserOwnedPurchases()
                    .doOnSuccess { purchaseList ->
                        purchaseList.filter { it.products.first() != Constants.SKU_INAPP_DISABLE_ADS }
                            .forEach {
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
                                    ?.addTo(compositeDisposable)
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
                    val disableAdsInApp =
                        tripple.third.firstOrNull { it.products.first() == Constants.SKU_INAPP_DISABLE_ADS }
                    preferences.disableAds(disableAdsInApp != null)

                    monetizationItems.clear()
                    monetizationItems += MonetizationHeaderViewModel(
                        tripple.first,
                        preferences.getTrueAccessToken().isNullOrEmpty()
                    )
                    monetizationItems += MonetizationViewModel(
                        R.drawable.ic_no_money,
                        appContext.getString(R.string.monetization_action_appodeal_title),
                        appContext.getString(
                            R.string.monetization_action_appodeal_description,
                            Constants.REWARD_VIDEO_ADS
                        ),
                        "FREE",
                        null,
                        false
                    ) { showAppodealAds() }

                    monetizationItems += tripple.second.map { skuDetails ->
                        MonetizationViewModel(
                            if (skuDetails.sku == Constants.SKU_INAPP_DISABLE_ADS) {
                                R.drawable.ic_adblock
                            } else {
                                R.drawable.ic_coin
                            },
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

                    viewState.showMonetizationActions(monetizationItems)

                },
                onError = {
                    Timber.e(it)
                    viewState.showProgress(false)
                    viewState.showMessage(
                        it.message
                            ?: appContext.getString(R.string.error_unknown)
                    )
                }
            )
            .addTo(compositeDisposable)
    }

    fun onBillingClientFailedToStart(@BillingClient.BillingResponseCode billingResponseCode: Int) {
        viewState.showProgress(false)
        viewState.showMessage(
            appContext.getString(
                R.string.error_billing_client_connection,
                billingResponseCode
            )
        )
    }

    fun onOwnedItemClicked(sku: String) {
        if (preferences.getTrueAccessToken().isNullOrEmpty()) {
            viewState.showMessage(R.string.need_to_login)
            viewState.scrollToTop()
        } else {
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
}