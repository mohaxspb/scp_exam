package ru.kuchanov.scpquiz.ui.fragment

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import jp.wasabeef.blurry.Blurry
import kotlinx.android.synthetic.main.fragment_settings.*
import ru.kuchanov.scpquiz.R
import ru.kuchanov.scpquiz.di.Di
import ru.kuchanov.scpquiz.di.module.EnterModule
import ru.kuchanov.scpquiz.mvp.presenter.util.SettingsPresenter
import ru.kuchanov.scpquiz.mvp.view.SettingsView
import ru.kuchanov.scpquiz.ui.BaseFragment
import toothpick.Toothpick
import toothpick.config.Module


class SettingsFragment : BaseFragment<SettingsView, SettingsPresenter>(), SettingsView {

    companion object {

        const val ARG_BACKGROUND = "ARG_BACKGROUND"

        fun newInstance(background: Bitmap): SettingsFragment {
            val fragment = SettingsFragment()
            val args = Bundle()
            args.putParcelable(ARG_BACKGROUND, background)
            fragment.arguments = args
            return fragment
        }
    }

    override val translucent = true

    override val scopes: Array<String> = arrayOf(Di.Scope.ENTER_FRAGMENT)

    override val modules: Array<Module> = arrayOf(EnterModule())

    @InjectPresenter
    override lateinit var presenter: SettingsPresenter

    @ProvidePresenter
    override fun providePresenter(): SettingsPresenter = scope.getInstance(SettingsPresenter::class.java)

    override fun inject() = Toothpick.inject(this, scope)

    override fun getLayoutResId() = R.layout.fragment_settings

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        backgroundImageView.post{
            Blurry.with(context)
                    .async()
                    .animate(500)
                    .from(arguments!![ARG_BACKGROUND] as Bitmap)
                    .into(backgroundImageView);
        }
    }

    override fun showLang(langString: String) {
        currentLangTextView.text = langString
    }
}