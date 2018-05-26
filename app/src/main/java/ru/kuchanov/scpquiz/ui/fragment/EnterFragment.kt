package ru.kuchanov.scpquiz.ui.fragment

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import kotlinx.android.synthetic.main.fragment_enter.*
import ru.kuchanov.scpquiz.R
import ru.kuchanov.scpquiz.di.Di
import ru.kuchanov.scpquiz.di.module.EnterModule
import ru.kuchanov.scpquiz.mvp.presenter.EnterPresenter
import ru.kuchanov.scpquiz.mvp.view.EnterView
import ru.kuchanov.scpquiz.ui.BaseFragment
import toothpick.Toothpick
import toothpick.config.Module

class EnterFragment : BaseFragment<EnterView, EnterPresenter>(), EnterView {

    override val scopes: Array<String> = arrayOf(Di.Scope.ENTER_FRAGMENT)

    override val modules: Array<Module> = arrayOf(EnterModule())

    @InjectPresenter
    override lateinit var presenter: EnterPresenter

    @ProvidePresenter
    override fun providePresenter(): EnterPresenter = scope.getInstance(EnterPresenter::class.java)

    override fun inject() = Toothpick.inject(this, scope)

    override fun getLayoutResId() = R.layout.fragment_enter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    override fun showProgressAnimation() {
        val progressAnimator = ObjectAnimator.ofInt(
            progressBar,
            "progress",
            0,
            1000
        )
        progressAnimator.duration = 1000
        progressAnimator.interpolator = AccelerateDecelerateInterpolator()
        progressAnimator.start()
    }

    companion object {
        fun newInstance() = EnterFragment()
    }
}