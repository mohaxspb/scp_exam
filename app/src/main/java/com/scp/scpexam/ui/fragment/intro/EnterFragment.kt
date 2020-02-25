package com.scp.scpexam.ui.fragment.intro

import android.animation.ObjectAnimator
import android.view.animation.AccelerateDecelerateInterpolator
import kotlinx.android.synthetic.main.fragment_enter.*
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import com.scp.scpexam.R
import com.scp.scpexam.mvp.presenter.intro.EnterPresenter
import com.scp.scpexam.mvp.view.intro.EnterView
import com.scp.scpexam.ui.BaseFragment
import com.scp.scpexam.utils.BitmapUtils
import timber.log.Timber
import toothpick.Toothpick
import toothpick.config.Module

class EnterFragment : BaseFragment<EnterView, EnterPresenter>(), EnterView {

    override val translucent = true

    override val scopes: Array<String> = arrayOf()

    override val modules: Array<Module> = arrayOf()

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

    companion object {
        fun newInstance() = EnterFragment()
    }
}