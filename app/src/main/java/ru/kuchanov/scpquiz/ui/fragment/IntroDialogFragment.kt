package ru.kuchanov.scpquiz.ui.fragment

import android.os.Bundle
import android.view.View
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import jp.wasabeef.blurry.Blurry
import kotlinx.android.synthetic.main.fragment_settings.*
import ru.kuchanov.scpquiz.Constants
import ru.kuchanov.scpquiz.R
import ru.kuchanov.scpquiz.di.Di
import ru.kuchanov.scpquiz.di.module.IntroDialogModule
import ru.kuchanov.scpquiz.mvp.presenter.intro.IntroDialogPresenter
import ru.kuchanov.scpquiz.mvp.view.IntroDialogView
import ru.kuchanov.scpquiz.ui.BaseFragment
import ru.kuchanov.scpquiz.utils.BitmapUtils
import toothpick.Toothpick
import toothpick.config.Module


class IntroDialogFragment : BaseFragment<IntroDialogView, IntroDialogPresenter>(), IntroDialogView {

    companion object {

        fun newInstance() = IntroDialogFragment()
    }

    override val translucent = true

    override val scopes: Array<String> = arrayOf(Di.Scope.INTRO_DIALOG_FRAGMENT)

    override val modules: Array<Module> = arrayOf(IntroDialogModule())

    @InjectPresenter
    override lateinit var presenter: IntroDialogPresenter

    @ProvidePresenter
    override fun providePresenter(): IntroDialogPresenter = scope.getInstance(IntroDialogPresenter::class.java)

    override fun inject() = Toothpick.inject(this, scope)

    override fun getLayoutResId() = R.layout.fragment_intro_dialog

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bitmap = BitmapUtils.fileToBitmap("${activity?.cacheDir}/${Constants.INTRO_DIALOG_BACKGROUND_FILE_NAME}.png")

        backgroundImageView.post {
            Blurry.with(context)
                    .async()
                    .animate(500)
                    .from(bitmap)
                    .into(backgroundImageView)
        }

        //todo
    }

}