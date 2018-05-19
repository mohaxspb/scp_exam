package ru.kuchanov.scpquiz.ui

import android.os.Bundle
import android.support.annotation.LayoutRes
import android.widget.Toast
import com.arellomobile.mvp.MvpAppCompatActivity
import com.arellomobile.mvp.MvpPresenter
import com.arellomobile.mvp.MvpView
import ru.kuchanov.scpquiz.di.Di
import ru.kuchanov.scpquiz.mvp.BaseView
import toothpick.Scope
import toothpick.Toothpick
import javax.inject.Inject
import javax.inject.Provider

abstract class BaseActivity<V : MvpView, P : MvpPresenter<V>> : MvpAppCompatActivity(), BaseView {

    abstract var scopes: Array<String>

    private val scope: Scope by lazy { Toothpick.openScopes(*scopes) }

    /**
     * add @InjectPresenter annotation in realization
     */
    abstract var presenter: P

    /**
     * provides single instance of concrete presenter in realization
     */
    abstract var presenterProvider: P

    /**
     * add @ProvidePresenter annotation in realization
     * @return presenterProvider.get() for provide singe instance of presenter, provided by dagger
     */
    abstract fun providePresenter(): P

    @LayoutRes
    abstract fun getLayoutResId(): Int

    /**
     * call Toothpick#inject in concrete realization here
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