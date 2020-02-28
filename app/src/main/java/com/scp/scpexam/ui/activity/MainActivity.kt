package com.scp.scpexam.ui.activity

import androidx.annotation.IdRes
import com.afollestad.materialdialogs.MaterialDialog
import com.mopub.common.MoPub
import com.scp.scpexam.Constants
import com.scp.scpexam.R
import com.scp.scpexam.mvp.presenter.activity.MainPresenter
import com.scp.scpexam.mvp.view.activity.MainView
import com.scp.scpexam.ui.BaseActivity
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import ru.terrakok.cicerone.Navigator
import ru.terrakok.cicerone.android.support.SupportAppNavigator
import ru.terrakok.cicerone.commands.*
import timber.log.Timber
import toothpick.Toothpick
import toothpick.config.Module

class MainActivity : BaseActivity<MainView, MainPresenter>(), MainView {

    @IdRes
    override val containerId = R.id.container

    override val scopes: Array<String> = arrayOf()

    override val modules: Array<Module> = arrayOf()

    @InjectPresenter
    override lateinit var presenter: MainPresenter

    @ProvidePresenter
    override fun providePresenter(): MainPresenter = scope.getInstance(MainPresenter::class.java)

    override fun getLayoutResId() = R.layout.activity_main

    override fun inject() = Toothpick.inject(this, scope)

    override var navigator: Navigator = object : SupportAppNavigator(this, containerId) {

        override fun applyCommand(command: Command) {
            Timber.d("applyCommand: ${command.javaClass.simpleName}")

            val screen = when (command) {
                is Forward -> {
                    command.screen
                }
                is Replace -> {
                    command.screen
                }
                is BackTo -> {
                    command.screen
                }
                is Back -> {
                    null
                }
                else -> {
                    throw IllegalArgumentException("Unexpected command: ${command::class.java.simpleName}")
                }
            }
            if ( screen is Constants.Screens.GameScreen
                    && !screen.quizScreenLaunchData.skipAdsShowing
                    && isInterstitialLoaded()
            ) {
                showAdsDialog(screen.quizScreenLaunchData.quizId)
            } else {
                if (MoPub.isSdkInitialized()){
                    requestNewInterstitial()
                }
                super.applyCommand(command)
            }
        }
    }

    override fun showFirstTimeAppodealAdsDialog() {
        Timber.d("showFirstTimeAppodealAdsDialog")
        //todo move to DialogUtils
        MaterialDialog.Builder(this)
                .title(R.string.will_show_ads_title)
                .content(getString(R.string.want_watch_ads_content, Constants.REWARD_VIDEO_ADS))
                .positiveText(android.R.string.ok)
                .onPositive { _, _ ->
                    preferenceManager.setRewardedDescriptionShown(true)
                    showRewardedVideo()
                }
                .negativeText(android.R.string.cancel)
                .show()
    }

    override fun showAdsDialog(quizId: Long) {
        Timber.d("showAdsDialog")
        //todo move to DialogUtils
        MaterialDialog.Builder(this)
                .title(R.string.will_show_ads_title)
                .content(R.string.will_show_ads_content)
                .positiveText(android.R.string.ok)
                .onPositive { _, _ -> showInterstitial(quizId) }
                .negativeText(R.string.remove_ads)
                .onNegative { _, _ -> startPurchase() }
                .neutralText(R.string.why_ads)
                .onNeutral { _, _ -> showWhyAdsDialog(quizId) }
                .show()
    }

    override fun showWhyAdsDialog(quizId: Long) {
        Timber.d("showWhyAdsDialog")
        //todo move to DialogUtils
        MaterialDialog.Builder(this)
                .title(R.string.why_ads_title)
                .content(R.string.why_ads_content)
                .positiveText(R.string.watch_ads)
                .onPositive { _, _ -> showInterstitial(quizId) }
                .negativeText(R.string.remove_ads)
                .onNegative { _, _ -> startPurchase() }
                .show()
    }

    override fun startPurchase() {
        Timber.d("startPurchase")
        billingDelegate.startPurchaseFlow(Constants.SKU_INAPP_DISABLE_ADS)
    }
}
