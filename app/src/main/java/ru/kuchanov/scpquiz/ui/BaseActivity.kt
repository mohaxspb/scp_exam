package ru.kuchanov.scpquiz.ui

import android.os.Bundle
import android.support.annotation.LayoutRes
import android.view.LayoutInflater
import android.widget.Toast
import com.arellomobile.mvp.MvpAppCompatActivity
import com.arellomobile.mvp.MvpPresenter
import ru.kuchanov.scpquiz.di.Di
import ru.kuchanov.scpquiz.mvp.BaseView
import ru.terrakok.cicerone.Navigator
import ru.terrakok.cicerone.NavigatorHolder
import toothpick.Scope
import toothpick.Toothpick
import toothpick.config.Module
import toothpick.smoothie.module.SmoothieSupportActivityModule
import javax.inject.Inject

abstract class BaseActivity<V : BaseView, P : MvpPresenter<V>> : MvpAppCompatActivity(), BaseView {

    abstract val scopes: Array<String>

    abstract val modules: Array<out Module>

    @Inject
    lateinit var myLayoutInflater: LayoutInflater

    @Inject
    lateinit var navigationHolder: NavigatorHolder

    /**
     * initialize it to provide navigation for concrete activity
     */
    abstract var navigator: Navigator

    override fun onResumeFragments() {
        super.onResumeFragments()
        navigationHolder.setNavigator(navigator)
    }

    override fun onPause() {
        super.onPause()
        navigationHolder.removeNavigator()
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
    }

    override fun showMessage(message: String) = Toast.makeText(this, message, Toast.LENGTH_LONG).show()

    override fun showMessage(message: Int) = showMessage(getString(message))

    override fun onDestroy() {
        super.onDestroy()

        for (scope in scopes) {
            Toothpick.closeScope(scope)
        }
    }
}