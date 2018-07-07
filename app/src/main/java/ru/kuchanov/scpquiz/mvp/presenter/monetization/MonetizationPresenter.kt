package ru.kuchanov.scpquiz.mvp.presenter.monetization

import android.app.Application
import com.arellomobile.mvp.InjectViewState
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import ru.kuchanov.scpquiz.Constants
import ru.kuchanov.scpquiz.R
import ru.kuchanov.scpquiz.controller.adapter.MyListItem
import ru.kuchanov.scpquiz.controller.adapter.viewmodel.MonetizationHeaderViewModel
import ru.kuchanov.scpquiz.controller.adapter.viewmodel.MonetizationViewModel
import ru.kuchanov.scpquiz.controller.db.AppDatabase
import ru.kuchanov.scpquiz.controller.manager.MyPreferenceManager
import ru.kuchanov.scpquiz.controller.navigation.ScpRouter
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
    override var appDatabase: AppDatabase
) : BasePresenter<MonetizationView>(appContext, preferences, router, appDatabase) {

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()

        appDatabase.userDao().getOneByRole(UserRole.PLAYER)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy {
                    val actions = mutableListOf<MyListItem>()
                    actions += MonetizationHeaderViewModel(it)
                    actions += MonetizationViewModel(
                        R.drawable.ic_no_money,
                        appContext.getString(R.string.monetization_action_appodeal_title),
                        appContext.getString(R.string.monetization_action_appodeal_description, Constants.REWARD_VIDEO_ADS)
                    ) { showAppodealAds() }
                    actions += MonetizationViewModel(
                        R.drawable.ic_adblock,
                        appContext.getString(R.string.monetization_action_noads_title),
                        appContext.getString(R.string.monetization_action_noads_description)
                    ) { buyNoAdsInApp() }

                    viewState.showMonetizationActions(actions)
                }
    }

    private fun buyNoAdsInApp() {
        //todo
        Timber.d("buyNoAdsInApp")
    }

    private fun showAppodealAds() {
        Timber.d("showAppodealAds")
        viewState.onNeedToShowRewardedVideo()
    }
}