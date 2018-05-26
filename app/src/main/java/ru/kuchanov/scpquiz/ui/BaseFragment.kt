package ru.kuchanov.scpquiz.ui

import android.os.Bundle
import android.support.annotation.LayoutRes
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import com.arellomobile.mvp.MvpAppCompatFragment
import com.arellomobile.mvp.MvpPresenter
import ru.kuchanov.scpquiz.di.Di
import ru.kuchanov.scpquiz.mvp.BaseView
import toothpick.Scope
import toothpick.Toothpick
import toothpick.config.Module

abstract class BaseFragment<V : BaseView, P : MvpPresenter<V>> : MvpAppCompatFragment(), BaseView {

    abstract val scopes: Array<String>

    abstract val modules: Array<out Module>

    /**
     * your activity scope, which constructs from [scopes]
     *
     * and contains installed [modules]
     */
    val scope: Scope by lazy {
        val _scope = Toothpick.openScopes(Di.Scope.APP, *scopes)
        _scope.installModules(*modules)
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

    /**
     * call [Toothpick].inject(YourActivityClass.this, [scope]) in concrete realization here
     */
    abstract fun inject()

    @LayoutRes
    abstract fun getLayoutResId(): Int

    override fun onCreate(savedInstanceState: Bundle?) {
        inject()
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
            inflater.inflate(getLayoutResId(), container, false)

    override fun showMessage(message: String) = Toast.makeText(activity, message, Toast.LENGTH_LONG).show()

    override fun showMessage(message: Int) = showMessage(getString(message))

    override fun onDestroyView() {
        super.onDestroyView()
        for (scope in scopes) {
            Toothpick.closeScope(scope)
        }
    }
}