package ru.kuchanov.scpquiz.ui

import android.os.Bundle
import android.support.annotation.LayoutRes
import android.view.LayoutInflater
import android.widget.Toast
import com.arellomobile.mvp.MvpAppCompatActivity
import com.arellomobile.mvp.MvpPresenter
import com.arellomobile.mvp.MvpView
import ru.kuchanov.scpquiz.mvp.BaseView
import toothpick.Scope
import toothpick.Toothpick
import toothpick.config.Module
import toothpick.smoothie.module.SmoothieActivityModule
import javax.inject.Inject

abstract class BaseActivity<V : MvpView, P : MvpPresenter<V>> : MvpAppCompatActivity(), BaseView {

    abstract val scopes: Array<String>

    abstract val modules: Array<out Module>

    @Inject
    lateinit var myLayoutInflater: LayoutInflater

    /**
     * your activity scope, which constructs from [scopes]
     *
     * and contains installed [modules]
     */
    val scope: Scope by lazy {
        val _scope = Toothpick.openScopes(*scopes)
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
     * call [Toothpick].inject(YorActivityClass.this, [scope]) in concrete realization here
     */
    abstract fun inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        inject()
        super.onCreate(savedInstanceState)
        setContentView(getLayoutResId())
    }

    override fun showMessage(message: String) = Toast.makeText(this, message, Toast.LENGTH_LONG).show()

    override fun showMessage(message: Int) = showMessage(getString(message))
}