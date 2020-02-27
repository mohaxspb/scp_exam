package com.scp.scpexam.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.annotation.LayoutRes
import com.mopub.common.MoPub
import com.mopub.common.MoPubReward
import com.mopub.common.SdkConfiguration
import com.mopub.common.SdkInitializationListener
import com.mopub.common.logging.MoPubLog
import com.mopub.mobileads.MoPubErrorCode
import com.mopub.mobileads.MoPubInterstitial
import com.mopub.mobileads.MoPubRewardedVideoListener
import com.mopub.mobileads.MoPubRewardedVideos
import com.scp.scpexam.BuildConfig
import com.scp.scpexam.Constants
import com.scp.scpexam.R
import com.scp.scpexam.controller.manager.monetization.BillingDelegate
import com.scp.scpexam.controller.manager.preference.MyPreferenceManager
import com.scp.scpexam.di.Di
import com.scp.scpexam.model.ui.QuizScreenLaunchData
import com.scp.scpexam.mvp.BaseView
import com.scp.scpexam.mvp.presenter.BasePresenter
import com.scp.scpexam.utils.AdsUtils
import com.vk.sdk.VKAccessToken
import com.vk.sdk.VKCallback
import com.vk.sdk.VKSdk
import com.vk.sdk.api.VKError
import moxy.MvpAppCompatActivity
import ru.kuchanov.rate.PreRate
import ru.terrakok.cicerone.Navigator
import ru.terrakok.cicerone.NavigatorHolder
import ru.terrakok.cicerone.Router
import timber.log.Timber
import toothpick.Scope
import toothpick.Toothpick
import toothpick.config.Module
import toothpick.smoothie.module.SmoothieSupportActivityModule
import javax.inject.Inject

abstract class BaseActivity<V : BaseView, P : BasePresenter<V>> : MvpAppCompatActivity(), BaseView, MoPubInterstitial.InterstitialAdListener {

    @Inject
    lateinit var myLayoutInflater: LayoutInflater

    @Inject
    lateinit var navigationHolder: NavigatorHolder

    @Inject
    lateinit var preferenceManager: MyPreferenceManager

    @Inject
    lateinit var router: Router

    abstract val scopes: Array<String>

    abstract val modules: Array<out Module>

    abstract val containerId: Int

    /**
     * initialize it to provide navigation for concrete activity
     */
    abstract var navigator: Navigator

    private lateinit var moPubInterstitial: MoPubInterstitial

    override fun onResumeFragments() {
        super.onResumeFragments()
        navigationHolder.setNavigator(navigator)
    }

    override fun onPause() {
        super.onPause()
        navigationHolder.removeNavigator()
//        MoPub.onPause(this)
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
//        MoPub.onResume(this)
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

        initMoPub()

        billingDelegate = BillingDelegate(this, null, null)
        billingDelegate.startConnection()
    }

    private fun initMoPub() {

        val configBuilder: SdkConfiguration.Builder = SdkConfiguration.Builder (AdsUtils.REWARD_VIDEO_AD_UNIT_ID)
        if (BuildConfig.DEBUG) {
            configBuilder.withLogLevel(MoPubLog.LogLevel.DEBUG)
        } else {
            configBuilder.withLogLevel(MoPubLog.LogLevel.NONE)
        }

        MoPub.initializeSdk(this, configBuilder.build(), initMoPubSdkListener())

        moPubInterstitial = MoPubInterstitial(this, AdsUtils.INTERSTITIAL_AD_UNIT_ID)
        moPubInterstitial.interstitialAdListener = this
        moPubInterstitial.load()

//        if (!moPubInterstitial.isReady) {
//            requestNewInterstitial()
//        }

        loadRewardedVideoAd()

        MoPubRewardedVideos.setRewardedVideoListener(object : MoPubRewardedVideoListener{
            override fun onRewardedVideoClosed(adUnitId: String) {
                Timber.d("onRewardedVideoClosed")
                loadRewardedVideoAd()
            }

            override fun onRewardedVideoCompleted(adUnitIds: MutableSet<String>, reward: MoPubReward) {
                Timber.d("onRewardedVideoCompleted")
                presenter.onRewardedVideoFinished()
            }

            override fun onRewardedVideoPlaybackError(adUnitId: String, errorCode: MoPubErrorCode) { Timber.d("onRewardedVideoCompleted") }

            override fun onRewardedVideoLoadFailure(adUnitId: String, errorCode: MoPubErrorCode) { Timber.d("onRewardedVideoLoadFailure") }

            override fun onRewardedVideoClicked(adUnitId: String) { Timber.d("onRewardedVideoClicked") }

            override fun onRewardedVideoStarted(adUnitId: String) { Timber.d("onRewardedVideoStarted") }

            override fun onRewardedVideoLoadSuccess(adUnitId: String) { Timber.d("onRewardedVideoLoadSuccess") }
        })
    }

    private fun initMoPubSdkListener(): SdkInitializationListener = object : SdkInitializationListener {
        override fun onInitializationFinished() {
            Timber.d("On mopub init finish")
        }
    }

    fun showInterstitial(quizId: Long) {
        moPubInterstitial.show()
        moPubInterstitial.interstitialAdListener = object : MoPubInterstitial.InterstitialAdListener {

            override fun onInterstitialShown(interstitial: MoPubInterstitial?) {
                requestNewInterstitial()
                preferenceManager.setNeedToShowInterstitial(false)
                router.replaceScreen(Constants.Screens.GameScreen(QuizScreenLaunchData(quizId, true)))
            }

            override fun onInterstitialLoaded(interstitial: MoPubInterstitial?) { Timber.d("onInterstitialLoaded") }

            override fun onInterstitialFailed(interstitial: MoPubInterstitial?, errorCode: MoPubErrorCode?) { Timber.d("onInterstitialFailed") }

            override fun onInterstitialDismissed(interstitial: MoPubInterstitial?) { Timber.d("onInterstitialDismissed") }

            override fun onInterstitialClicked(interstitial: MoPubInterstitial?) { Timber.d("onInterstitialClicked") }

        }
    }

    protected fun requestNewInterstitial() {
        Timber.d(
                "requestNewInterstitial ready/disabled: %s/%s",
                moPubInterstitial.isReady,
                preferenceManager.isAdsDisabled()
        )
        if (moPubInterstitial.isReady || preferenceManager.isAdsDisabled()) {
            Timber.d("loading already done or disabled")
        } else {
            moPubInterstitial.load()
        }
    }

    private fun loadRewardedVideoAd(){
        MoPubRewardedVideos.loadRewardedVideo(AdsUtils.REWARD_VIDEO_AD_UNIT_ID)
    }

    protected fun isInterstitialLoaded() = moPubInterstitial.isReady

    override fun showMessage(message: String) = Toast.makeText(this, message, Toast.LENGTH_LONG).show()

    override fun showMessage(message: Int) = showMessage(getString(message))

    override fun onDestroy() {
        moPubInterstitial.destroy()
        super.onDestroy()

        for (scope in scopes) {
            Toothpick.closeScope(scope)
        }

        PreRate.clearDialogIfOpen()
//        MoPub.onDestroy(this)

    }

    fun showRewardedVideo(){
        if (MoPubRewardedVideos.hasRewardedVideo(AdsUtils.REWARD_VIDEO_AD_UNIT_ID)) {
            MoPubRewardedVideos.showRewardedVideo(AdsUtils.REWARD_VIDEO_AD_UNIT_ID)
        } else {
            showMessage(R.string.reward_not_loaded_yet)
            loadRewardedVideoAd()
        }
    }

    fun buyCoins(skuId: String) {
        billingDelegate.startPurchaseFlow(skuId)
    }

    override fun onInterstitialLoaded(interstitial: MoPubInterstitial?) {
    }

    override fun onInterstitialShown(interstitial: MoPubInterstitial?) {
    }

    override fun onInterstitialFailed(interstitial: MoPubInterstitial?, errorCode: MoPubErrorCode?) {
    }

    override fun onInterstitialDismissed(interstitial: MoPubInterstitial?) {
    }

    override fun onInterstitialClicked(interstitial: MoPubInterstitial?) {
    }
}
