package com.scp.scpexam.ui

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.viewbinding.ViewBinding
import com.scp.scpexam.di.Di
import com.scp.scpexam.mvp.BaseView
import moxy.MvpAppCompatFragment
import moxy.MvpPresenter
import toothpick.Scope
import toothpick.Toothpick
import toothpick.config.Module

abstract class BaseFragment<V : BaseView, P : MvpPresenter<V>, VB : ViewBinding> :
    MvpAppCompatFragment(), BaseView {

    abstract val scopes: Array<String>

    abstract val modules: Array<out Module>

    private var _binding: VB? = null
    abstract val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> VB
    protected val binding: VB
        get() = _binding as VB

    /**
     * your fragment scope, which constructs from [scopes]
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = bindingInflater(inflater, container, false)
        return binding.root
    }


    abstract val translucent: Boolean

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        applyStatusBarTranslucence(translucent)
    }

    override fun showMessage(message: String) {
        if (isAdded) {
            activity?.let { Toast.makeText(it, message, Toast.LENGTH_LONG).show() }
        }
    }

    override fun showMessage(message: Int) {
        if (isAdded) {
            showMessage(getString(message))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        scopes.forEach { Toothpick.closeScope(it) }
        _binding = null
    }

    private fun applyStatusBarTranslucence(translucent: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val w = activity!!.window
            if (translucent) {
                w.setFlags(
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                )
            } else {
                w.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            }
        }
    }

    fun getBaseActivity() = activity as BaseActivity<*, *>
}