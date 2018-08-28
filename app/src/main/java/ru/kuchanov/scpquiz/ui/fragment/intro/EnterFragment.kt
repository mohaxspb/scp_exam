package ru.kuchanov.scpquiz.ui.fragment.intro

import android.animation.ObjectAnimator
import android.view.animation.AccelerateDecelerateInterpolator
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import kotlinx.android.synthetic.main.fragment_enter.*
import ru.kuchanov.scpquiz.R
import ru.kuchanov.scpquiz.di.Di
import ru.kuchanov.scpquiz.di.module.EnterModule
import ru.kuchanov.scpquiz.mvp.presenter.intro.EnterPresenter
import ru.kuchanov.scpquiz.mvp.view.intro.EnterView
import ru.kuchanov.scpquiz.ui.BaseFragment
import ru.kuchanov.scpquiz.utils.BitmapUtils
import timber.log.Timber
import toothpick.Toothpick
import toothpick.config.Module

class EnterFragment : BaseFragment<EnterView, EnterPresenter>(), EnterView {

    companion object {
        fun newInstance() = EnterFragment()
    }

    override val translucent = true

    override val scopes: Array<String> = arrayOf(Di.Scope.ENTER_FRAGMENT)

    override val modules: Array<Module> = arrayOf(EnterModule())

    @InjectPresenter
    override lateinit var presenter: EnterPresenter

    @ProvidePresenter
    override fun providePresenter(): EnterPresenter = scope.getInstance(EnterPresenter::class.java)

    override fun inject() = Toothpick.inject(this, scope)

    override fun getLayoutResId() = R.layout.fragment_enter

    override fun showProgressText(text: String) {
        progressTextView.text = text
        progressTextView.setOnClickListener { presenter.onProgressTextClicked() }
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

    override fun showImage(imageNumber: Int) {
        Timber.d("showImage: $imageNumber")
        val imageView = when (imageNumber) {
            0 -> bottomImageView
            1 -> middleImageView
            2 -> topImageView
            else -> return
        }

        val progressAnimator = ObjectAnimator.ofFloat(
            imageView,
            "alpha",
            0f,
            1f
        )
        progressAnimator.duration = 800
        progressAnimator.interpolator = AccelerateDecelerateInterpolator()
        progressAnimator.start()
    }

    override fun onNeedToOpenIntroDialogFragment() {
        BitmapUtils.loadBitmapFromView(root)?.let { presenter.openIntroDialogScreen(it) }
    }
}