package ru.kuchanov.scpquiz.ui

import android.os.Bundle
import android.support.annotation.LayoutRes
import android.view.LayoutInflater
import android.widget.Toast
import com.appodeal.ads.Appodeal
import com.arellomobile.mvp.MvpAppCompatActivity
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.MobileAds
import ru.kuchanov.rate.PreRate
import ru.kuchanov.scpquiz.Constants
import ru.kuchanov.scpquiz.R
import ru.kuchanov.scpquiz.controller.manager.monetization.BillingDelegate
import ru.kuchanov.scpquiz.controller.manager.preference.MyPreferenceManager
import ru.kuchanov.scpquiz.controller.navigation.ScpRouter
import ru.kuchanov.scpquiz.di.Di
import ru.kuchanov.scpquiz.mvp.BaseView
import ru.kuchanov.scpquiz.mvp.presenter.BasePresenter
import ru.kuchanov.scpquiz.ui.utils.MyRewardedVideoCallbacks
import ru.kuchanov.scpquiz.utils.AdsUtils
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

    override fun onResumeFragments() {
        super.onResumeFragments()
        navigationHolder.setNavigator(navigator)
    }

    override fun onPause() {
        super.onPause()
        navigationHolder.removeNavigator()
    }


    override fun onResume() {
        super.onResume()
        PreRate.init(this, getString(R.string.feedback_email), getString(R.string.feedback_title)).showIfNeed()
        requestNewInterstitial()
    }

    /**
     * your activity scope, which constructs from [scopes]
     *
     * and contains installed [modules]
     */
    val scope: Scope by lazy {
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

        billingDelegate = BillingDelegate(this, null, null)
        billingDelegate.startConnection()
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

        //appodeal
        Appodeal.disableLocationPermissionCheck()
        Appodeal.disableWriteExternalStoragePermissionCheck()
//        Appodeal.setTesting(BuildConfig.DEBUG)
//        Appodeal.setLogLevel(if (BuildConfig.DEBUG) Log.LogLevel.debug else Log.LogLevel.none);
//        Appodeal.disableNetwork(this, "vungle")
//        Appodeal.disableNetwork(this, "facebook");
        Appodeal.initialize(
            this,
            getString(R.string.appodeal_app_key),
            Appodeal.REWARDED_VIDEO,
            true
        )

        Appodeal.muteVideosIfCallsMuted(true)
        Appodeal.setRewardedVideoCallbacks(object : MyRewardedVideoCallbacks() {
//            override fun onRewardedVideoFinished(p0: Double, p1: String?) {
//                super.onRewardedVideoFinished(p0, p1)
//                Timber.d("onRewardedVideoFinished: $p0, $p1")
//                presenter.onRewardedVideoFinished()
//            }

            override fun onRewardedVideoClosed(p0: Boolean) {
                super.onRewardedVideoClosed(p0)
                Timber.d("onRewardedVideoClosed: $p0")
                presenter.onRewardedVideoFinished()
            }
        })
    }

    fun showInterstitial(quizId: Long) {
        interstitialAd.adListener = object : AdListener() {
            override fun onAdOpened() {
                super.onAdOpened()
                requestNewInterstitial()
                preferenceManager.setNeedToShowInterstitial(false)
                router.replaceScreen(Constants.Screens.QUIZ, quizId)
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

    protected fun isInterstitialLoaded() = interstitialAd.isLoaded

    override fun showMessage(message: String) = Toast.makeText(this, message, Toast.LENGTH_LONG).show()

    override fun showMessage(message: Int) = showMessage(getString(message))

    override fun onDestroy() {
        super.onDestroy()

        for (scope in scopes) {
            Toothpick.closeScope(scope)
        }

        PreRate.clearDialogIfOpen()
    }

    fun showRewardedVideo() {
        if (Appodeal.isLoaded(Appodeal.REWARDED_VIDEO)) {
            Appodeal.show(this, Appodeal.REWARDED_VIDEO)
        } else {
            showMessage(R.string.reward_not_loaded_yet)
        }
    }
}
