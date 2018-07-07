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
import ru.kuchanov.scpquiz.BuildConfig
import ru.kuchanov.scpquiz.Constants
import ru.kuchanov.scpquiz.R
import ru.kuchanov.scpquiz.controller.manager.MyPreferenceManager
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

    private lateinit var mInterstitialAd: InterstitialAd

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

    override fun onCreate(savedInstanceState: Bundle?) {
        inject()
        super.onCreate(savedInstanceState)
        setContentView(getLayoutResId())

        initAds()
    }

    private fun initAds() {
        MobileAds.initialize(applicationContext, getString(R.string.ads_app_id))

        mInterstitialAd = InterstitialAd(this)
        mInterstitialAd.adUnitId = getString(R.string.ad_unit_id_interstitial)

        if (!mInterstitialAd.isLoaded) {
            requestNewInterstitial()
        }

        //appodeal
        Appodeal.disableLocationPermissionCheck()
        if (BuildConfig.DEBUG) {
            Appodeal.setTesting(true)
            //            Appodeal.setLogLevel(Log.LogLevel.debug);
        }
//        Appodeal.disableNetwork(this, "vungle")
//        Appodeal.disableNetwork(this, "facebook");
        Appodeal.initialize(
            this,
            getString(R.string.appodeal_app_key),
            Appodeal.REWARDED_VIDEO
        )

        Appodeal.muteVideosIfCallsMuted(true)
        Appodeal.setRewardedVideoCallbacks(object : MyRewardedVideoCallbacks() {
            override fun onRewardedVideoFinished(i: Int, s: String?) {
                super.onRewardedVideoFinished(i, s)
                Timber.d("onRewardedVideoFinished: $i, $s")
                presenter.onRewardedVideoFinished()
            }
        })
    }

    fun showInterstitial(quizId: Long) {
        mInterstitialAd.adListener = object : AdListener() {
            override fun onAdClosed() {
                super.onAdClosed()
                preferenceManager.setNeedToShowInterstitial(false)
                router.replaceScreen(Constants.Screens.QUIZ, quizId)
            }
        }
        mInterstitialAd.show()
    }

    private fun requestNewInterstitial() {
        Timber.d("requestNewInterstitial loading/loaded: %s/%s", mInterstitialAd.isLoading, mInterstitialAd.isLoaded)
        if (mInterstitialAd.isLoading || mInterstitialAd.isLoaded) {
            Timber.d("loading already in progress or already done")
        } else {
            mInterstitialAd.loadAd(AdsUtils.buildAdRequest())
        }
    }

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
