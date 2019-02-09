package ru.kuchanov.scpquiz.ui.activity

import android.content.Context
import android.content.Intent
import android.support.annotation.IdRes
import android.support.v4.app.Fragment
import com.afollestad.materialdialogs.MaterialDialog
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import ru.kuchanov.scpquiz.Constants
import ru.kuchanov.scpquiz.R
import ru.kuchanov.scpquiz.controller.navigation.ShowCommand
import ru.kuchanov.scpquiz.model.ui.QuizScreenLaunchData
import ru.kuchanov.scpquiz.mvp.presenter.activity.MainPresenter
import ru.kuchanov.scpquiz.mvp.view.activity.MainView
import ru.kuchanov.scpquiz.ui.BaseActivity
import ru.kuchanov.scpquiz.ui.fragment.game.GameFragment
import ru.kuchanov.scpquiz.ui.fragment.game.LevelsFragment
import ru.kuchanov.scpquiz.ui.fragment.intro.EnterFragment
import ru.kuchanov.scpquiz.ui.fragment.intro.IntroDialogFragment
import ru.kuchanov.scpquiz.ui.fragment.monetization.MonetizationFragment
import ru.kuchanov.scpquiz.ui.fragment.util.ScpSettingsFragment
import ru.terrakok.cicerone.Navigator
import ru.terrakok.cicerone.android.SupportAppNavigator
import ru.terrakok.cicerone.commands.Command
import ru.terrakok.cicerone.commands.Forward
import ru.terrakok.cicerone.commands.Replace
import timber.log.Timber
import toothpick.Toothpick
import toothpick.config.Module


class MainActivity : BaseActivity<MainView, MainPresenter>(), MainView {

    @IdRes
    override val containerId = R.id.container

    override val scopes: Array<String> = arrayOf()

    override val modules: Array<Module> = arrayOf()

    override var navigator: Navigator = object : SupportAppNavigator(this, containerId) {

        override fun createActivityIntent(context: Context, screenKey: String?, data: Any?): Intent? = null

        override fun createFragment(screenKey: String?, data: Any?): Fragment? {
            Timber.d("createFragment key: $screenKey, data: $data")
            return when (screenKey) {
                Constants.Screens.ENTER -> EnterFragment.newInstance()
                Constants.Screens.QUIZ_LIST -> LevelsFragment.newInstance()
                Constants.Screens.QUIZ -> GameFragment.newInstance((data as QuizScreenLaunchData).quizId)
                Constants.Screens.SETTINGS -> ScpSettingsFragment.newInstance()
                Constants.Screens.INTRO_DIALOG -> IntroDialogFragment.newInstance()
                Constants.Screens.MONETIZATION -> MonetizationFragment.newInstance()
                else -> null
            }
        }

        override fun applyCommand(command: Command?) {
            Timber.d("applyCommand: ${command?.javaClass?.simpleName ?: command}")
            if (command is ShowCommand) {
                supportFragmentManager.beginTransaction()
                        .add(containerId, createFragment(command.screenKey, command.transitionData)!!)
                        .addToBackStack(null)
                        .commit()
            } else {
                var screenKey: String? = null
                var data: Any? = null
                if (command is Forward) {
                    screenKey = command.screenKey
                    data = command.transitionData
                } else if (command is Replace) {
                    screenKey = command.screenKey
                    data = command.transitionData
                }
                if (
                        screenKey != null
                        && screenKey == Constants.Screens.QUIZ
                        && !(data as QuizScreenLaunchData).skipAdsShowing
                        && isInterstitialLoaded()
                ) {
                    showAdsDialog(data.quizId)
                } else {
                    requestNewInterstitial()
                    super.applyCommand(command)
                }
            }
        }
    }

    @InjectPresenter
    override lateinit var presenter: MainPresenter

    @ProvidePresenter
    override fun providePresenter(): MainPresenter = scope.getInstance(MainPresenter::class.java)

    override fun getLayoutResId() = R.layout.activity_main

    override fun inject() = Toothpick.inject(this, scope)

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
