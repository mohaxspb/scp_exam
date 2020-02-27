package com.scp.scpexam.ui

import android.content.Intent
import android.os.Bundle
import androidx.annotation.LayoutRes
import android.view.LayoutInflater
import android.widget.Toast
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.reward.RewardItem
import com.google.android.gms.ads.reward.RewardedVideoAd
import com.mopub.common.MoPub
import com.mopub.common.SdkConfiguration
import com.mopub.common.SdkInitializationListener
import com.mopub.common.logging.MoPubLog
import com.mopub.mobileads.BuildConfig.DEBUG
import com.scp.scpexam.BuildConfig
import com.vk.sdk.VKAccessToken
import com.vk.sdk.VKCallback
import com.vk.sdk.VKSdk
import com.vk.sdk.api.VKError
import moxy.MvpAppCompatActivity
import ru.kuchanov.rate.PreRate
import com.scp.scpexam.Constants
import com.scp.scpexam.R
import com.scp.scpexam.controller.manager.monetization.BillingDelegate
import com.scp.scpexam.controller.manager.preference.MyPreferenceManager
import com.scp.scpexam.controller.navigation.ScpRouter
import com.scp.scpexam.di.Di
import com.scp.scpexam.model.ui.QuizScreenLaunchData
import com.scp.scpexam.mvp.BaseView
import com.scp.scpexam.mvp.presenter.BasePresenter
import com.scp.scpexam.ui.utils.MyRewardedVideoCallbacks
import com.scp.scpexam.utils.AdsUtils
import ru.terrakok.cicerone.Navigator
import ru.terrakok.cicerone.NavigatorHolder
import timber.log.Timber
import toothpick.Scope
import toothpick.Toothpick
import toothpick.config.Module
import toothpick.smoothie.module.SmoothieSupportActivityModule
import javax.inject.Inject

abstract class BaseActivity<V : BaseView, P : BasePresenter<V>> : MvpAppCompatActivity(), BaseView {

    @Inject
    lateinit var myLayoutInflater: LayoutInflater

    @Inject
    lateinit var navigationHolder: NavigatorHolder

    @Inject
    lateinit var preferenceManager: MyPreferenceManager

    @Inject
    lateinit var router: ScpRouter

    abstract val scopes: Array<String>

    abstract val modules: Array<out Module>

    abstract val containerId: Int

    /**
     * initialize it to provide navigation for concrete activity
     */
    abstract var navigator: Navigator

    private lateinit var interstitialAd: InterstitialAd

    private lateinit var mRewardedVideoAd: RewardedVideoAd

    override fun onResumeFragments() {
        super.onResumeFragments()
        navigationHolder.setNavigator(navigator)
    }

    override fun onPause() {
        super.onPause()
        navigationHolder.removeNavigator()
        mRewardedVideoAd.pause(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        VKSdk.onActivityResult(requestCode, resultCode, data, object : VKCallback<VKAccessToken> {
            override fun onResult(res: VKAccessToken) {
                for (fragment in supportFragmentManager.fragments) {
                    fragment.onActivityResult(requestCode, resultCode, data)
                }
            }

            override fun onError(error: VKError) {}
        })
        super.onActivityResult(requestCode, resultCode, data)
    }


    override fun onResume() {
        super.onResume()
        PreRate.init(this, getString(R.string.feedback_email), getString(R.string.feedback_title)).showIfNeed()
        requestNewInterstitial()
        mRewardedVideoAd.resume(this)
    }

    /**
     * your activity scope, which constructs from [scopes]
     *
     * and contains installed [modules]
     */
    val scope: Scope by lazy {
        @Suppress("LocalVariableName")
        val _scope = Toothpick.openScopes(Di.Scope.APP, *scopes)
        _scope.installModules(SmoothieSupportActivityModule(this), *modules)
        _scope
    }

    /**
     * add @InjectPresenter annotation in realization
     */
    abstract var presenter: P

    /**
     * add @ProvidePresenter annotation in realization
     * @return presenterProvider.get() for provide singe instance of presenter, provided by dagger
     */
    abstract fun providePresenter(): P

    @LayoutRes
    abstract fun getLayoutResId(): Int

    /**
     * call [Toothpick].inject(YourActivityClass.this, [scope]) in concrete realization here
     */
    abstract fun inject()

    lateinit var billingDelegate: BillingDelegate

    override fun onCreate(savedInstanceState: Bundle?) {
        inject()
        super.onCreate(savedInstanceState)
        setContentView(getLayoutResId())

        initAds()
        initMoPub()

        billingDelegate = BillingDelegate(this, null, null)
        billingDelegate.startConnection()
    }

    private fun initMoPub() {

        val configBuilder: SdkConfiguration.Builder = SdkConfiguration.Builder (AdsUtils.TEST_REWARD_VIDEO_AD_UNIT_ID)
        if (BuildConfig.DEBUG) {
            configBuilder.withLogLevel(MoPubLog.LogLevel.DEBUG)
        } else {
            configBuilder.withLogLevel(MoPubLog.LogLevel.NONE)
        }

        MoPub.initializeSdk(this, configBuilder.build(), initMoPubSdkListener())

    }

    private fun initMoPubSdkListener(): SdkInitializationListener = object : SdkInitializationListener {
        override fun onInitializationFinished() {
            Timber.d("On mopub init finish")
        }
    }

    private fun initAds() {
        MobileAds.initialize(applicationContext, getString(R.string.ads_app_id))

        // Set app volume to be half of current device volume.
        if (preferenceManager.isSoundEnabled()) {
            MobileAds.setAppMuted(false)
            MobileAds.setAppVolume(0.5f)
        } else {
            // Set app volume to be half of current device volume.
            MobileAds.setAppMuted(true)
        }
        interstitialAd = InterstitialAd(this)
        interstitialAd.adUnitId = getString(R.string.ad_unit_id_interstitial)

        if (!interstitialAd.isLoaded) {
            requestNewInterstitial()
        }

        mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(this)
        mRewardedVideoAd.rewardedVideoAdListener = object : MyRewardedVideoCallbacks() {
            override fun onRewardedVideoAdClosed() {
                super.onRewardedVideoAdClosed()
                loadRewardedVideoAd()
            }

            override fun onRewarded(rewardItem: RewardItem?) {
                super.onRewarded(rewardItem)
                presenter.onRewardedVideoFinished()
            }
        }

        loadRewardedVideoAd()
    }

    fun showInterstitial(quizId: Long) {
        interstitialAd.adListener = object : AdListener() {
            override fun onAdOpened() {
                super.onAdOpened()
                requestNewInterstitial()
                preferenceManager.setNeedToShowInterstitial(false)
                router.replaceScreen(Constants.Screens.GameScreen(QuizScreenLaunchData(quizId, true)))

            }
        }
        interstitialAd.show()
    }

    protected fun requestNewInterstitial() {
        Timber.d(
                "requestNewInterstitial loading/loaded/disabled: %s/%s/%s",
                interstitialAd.isLoading,
                interstitialAd.isLoaded,
                preferenceManager.isAdsDisabled()
        )
        if (interstitialAd.isLoading || interstitialAd.isLoaded || preferenceManager.isAdsDisabled()) {
            Timber.d("loading already in progress or already done or disabled")
        } else {
            interstitialAd.loadAd(AdsUtils.buildAdRequest())
        }
    }

    private fun loadRewardedVideoAd() {
        @Suppress("ConstantConditionIf")
        mRewardedVideoAd.loadAd(
                getString(R.string.ad_unit_id_rewarded_video),
                AdsUtils.buildAdRequest()
        )
    }

    protected fun isInterstitialLoaded() = interstitialAd.isLoaded

    override fun showMessage(message: String) = Toast.makeText(this, message, Toast.LENGTH_LONG).show()

    override fun showMessage(message: Int) = showMessage(getString(message))

    override fun onDestroy() {
        super.onDestroy()

        for (scope in scopes) {
            Toothpick.closeScope(scope)
        }

        PreRate.clearDialogIfOpen()

        mRewardedVideoAd.destroy(this)
    }

    fun showRewardedVideo() {
        if (mRewardedVideoAd.isLoaded) {
            mRewardedVideoAd.show()
        } else {
            showMessage(R.string.reward_not_loaded_yet)
            loadRewardedVideoAd()
        }
    }

    fun buyCoins(skuId: String) {
        billingDelegate.startPurchaseFlow(skuId)
    }
}
