package com.scp.scpexam.ui

//import com.vk.sdk.VKAccessToken
//import com.vk.sdk.VKCallback
//import com.vk.sdk.api.VKError
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.annotation.LayoutRes
import com.mopub.common.MoPub
import com.mopub.common.MoPubReward
import com.mopub.common.SdkConfiguration
import com.mopub.common.SdkInitializationListener
import com.mopub.common.logging.MoPubLog
import com.mopub.mobileads.MoPubInterstitial
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
import com.scp.scpexam.ui.utils.MyMoPubListener
import moxy.MvpAppCompatActivity
import ru.kuchanov.rate.PreRate
import ru.terrakok.cicerone.Navigator
import ru.terrakok.cicerone.NavigatorHolder
import ru.terrakok.cicerone.Router
import timber.log.Timber
import toothpick.Scope
import toothpick.Toothpick
import toothpick.config.Module
import toothpick.smoothie.module.SmoothieActivityModule
import javax.inject.Inject

abstract class BaseActivity<V : BaseView, P : BasePresenter<V>> : MvpAppCompatActivity(), BaseView {

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
        MoPub.onPause(this)
    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
////        VKSdk.onActivityResult(requestCode, resultCode, data, object : VKCallback<VKAccessToken> {
////            override fun onResult(res: VKAccessToken) {
////                for (fragment in supportFragmentManager.fragments) {
////                    fragment.onActivityResult(requestCode, resultCode, data)
////                }
////            }
////
////            override fun onError(error: VKError) {}
////        })
//        super.onActivityResult(requestCode, resultCode, data)
//    }


    override fun onResume() {
        super.onResume()
        PreRate.init(this, getString(R.string.feedback_email), getString(R.string.feedback_title)).showIfNeed()
        if (MoPub.isSdkInitialized()){
            requestNewInterstitial()
        }
    }

    /**
     * your activity scope, which constructs from [scopes]
     *
     * and contains installed [modules]
     */
    val scope: Scope by lazy {
        @Suppress("LocalVariableName")
        val _scope = Toothpick.openScopes(Di.Scope.APP, *scopes)
        _scope.installModules(SmoothieActivityModule(this), *modules)
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

        val configBuilder: SdkConfiguration.Builder = SdkConfiguration.Builder(getString(R.string.ad_unit_id_banner))
        if (BuildConfig.DEBUG) {
            configBuilder.withLogLevel(MoPubLog.LogLevel.DEBUG)
        } else {
            configBuilder.withLogLevel(MoPubLog.LogLevel.NONE)
        }

        MoPub.initializeSdk(this, configBuilder.build(), initMoPubSdkListener())

        billingDelegate = BillingDelegate(this, null, null)
        billingDelegate.startConnection()
    }

    private fun initMoPub() {

        MoPubRewardedVideos.setRewardedVideoListener(object : MyMoPubListener(){
            override fun onRewardedVideoClosed(adUnitId: String) {
                Timber.d("onRewardedVideoClosed")
                loadRewardedVideoAd()
            }

            override fun onRewardedVideoCompleted(adUnitIds: MutableSet<String>, reward: MoPubReward) {
                Timber.d("onRewardedVideoCompleted")
                presenter.onRewardedVideoFinished()
            }

        })

        moPubInterstitial = MoPubInterstitial(this, getString(R.string.ad_unit_id_interstitial))

        moPubInterstitial.interstitialAdListener = MyMoPubListener()
        moPubInterstitial.load()

        loadRewardedVideoAd()
    }

    private fun initMoPubSdkListener(): SdkInitializationListener = SdkInitializationListener {
        Timber.d("On mopub init finish")
        initMoPub()
    }

    fun showInterstitial(quizId: Long) {

        if (moPubInterstitial.isReady){
            moPubInterstitial.show()
        } else {
            requestNewInterstitial()
        }
        moPubInterstitial.interstitialAdListener = object : MyMoPubListener() {

            override fun onInterstitialShown(interstitial: MoPubInterstitial?) {
                requestNewInterstitial()
                preferenceManager.setNeedToShowInterstitial(false)
                router.replaceScreen(Constants.Screens.GameScreen(QuizScreenLaunchData(quizId, true)))
            }
        }
    }

    protected fun requestNewInterstitial() {
        if (::moPubInterstitial.isInitialized) {
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
    }

    private fun loadRewardedVideoAd(){
        MoPubRewardedVideos.loadRewardedVideo(getString(R.string.ad_unit_id_rewarded_video))
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

    }

    fun showRewardedVideo(){

        if (MoPubRewardedVideos.hasRewardedVideo(getString(R.string.ad_unit_id_rewarded_video))) {
            MoPubRewardedVideos.showRewardedVideo(getString(R.string.ad_unit_id_rewarded_video))
        } else {
            showMessage(R.string.reward_not_loaded_yet)
            loadRewardedVideoAd()
        }
    }

    fun buyCoins(skuId: String) {
        billingDelegate.startPurchaseFlow(skuId)
    }
}
