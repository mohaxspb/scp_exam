package ru.kuchanov.scpquiz.ui.fragment

import android.animation.ObjectAnimator
import android.view.animation.AccelerateDecelerateInterpolator
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import kotlinx.android.synthetic.main.fragment_enter.*
import ru.kuchanov.scpquiz.R
import ru.kuchanov.scpquiz.di.Di
import ru.kuchanov.scpquiz.di.module.EnterModule
import ru.kuchanov.scpquiz.mvp.presenter.intro.EnterPresenter
import ru.kuchanov.scpquiz.mvp.view.EnterView
import ru.kuchanov.scpquiz.ui.BaseFragment
import timber.log.Timber
import toothpick.Toothpick
import toothpick.config.Module
import java.util.*

class EnterFragment : BaseFragment<EnterView, EnterPresenter>(), EnterView {
    override val translucent = true

    override val scopes: Array<String> = arrayOf(Di.Scope.ENTER_FRAGMENT)

    override val modules: Array<Module> = arrayOf(EnterModule())

    @InjectPresenter
    override lateinit var presenter: EnterPresenter

    @ProvidePresenter
    override fun providePresenter(): EnterPresenter = scope.getInstance(EnterPresenter::class.java)

    override fun inject() = Toothpick.inject(this, scope)

    override fun getLayoutResId() = R.layout.fragment_enter

    override fun showProgressText() {
        val progressTexts = resources.getStringArray(R.array.progress_texts)
        progressTextView.text = progressTexts[Random().nextInt(progressTexts.size)]
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
            else -> throw IllegalStateException("unexpected image")
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

    companion object {
        fun newInstance() = EnterFragment()
    }
}