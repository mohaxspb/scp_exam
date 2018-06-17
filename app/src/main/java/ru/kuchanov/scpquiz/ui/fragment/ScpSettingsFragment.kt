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
import timber.log.Timber
import toothpick.Toothpick
import toothpick.config.Module


class ScpSettingsFragment : BaseFragment<SettingsView, SettingsPresenter>(), SettingsView {

    companion object {

        const val ARG_BACKGROUND = "ARG_BACKGROUND"

        fun newInstance(background: Bitmap): ScpSettingsFragment {
            val fragment = ScpSettingsFragment()
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

        backgroundImageView.post {
            Blurry.with(context)
                    .async()
                    .animate(500)
                    .from(arguments!![ARG_BACKGROUND] as Bitmap)
                    .into(backgroundImageView)
        }

        val onLangClickListener: (View) -> Unit = { presenter.onLangClicked() }
        languageLabelTextView.setOnClickListener(onLangClickListener)
        languageImageView.setOnClickListener(onLangClickListener)

        soundSwitch.setOnCheckedChangeListener { _, isChecked -> presenter.onSoundEnabled(isChecked) }
        vibrateSwitch.setOnCheckedChangeListener { _, isChecked -> presenter.onVibrationEnabled(isChecked) }

        val onShareClickListener: (View) -> Unit = { presenter.onShareClicked() }
        shareImageView.setOnClickListener(onShareClickListener)
        shareLabelTextView.setOnClickListener(onShareClickListener)

        privacyPolicyLabelTextView.setOnClickListener { presenter.onPrivacyPolicyClicked() }
    }

    override fun showLang(langString: String) {
        val langRes = when (langString) {
            "ru" -> R.drawable.ic_ru
            else -> R.drawable.ic_en
        }
        languageImageView.setImageResource(langRes)
    }

    override fun showLangsChooser(langs: Set<String>) {
        Timber.d("showLangsChooser: $langs")
        //todo
    }

    override fun showSound(enabled: Boolean) {
        soundSwitch.setOnCheckedChangeListener(null)
        soundSwitch.isChecked = enabled
        soundSwitch.setOnCheckedChangeListener { _, isChecked -> presenter.onSoundEnabled(isChecked) }
    }

    override fun showVibration(enabled: Boolean) {
        vibrateSwitch.setOnCheckedChangeListener(null)
        vibrateSwitch.isChecked = enabled
        vibrateSwitch.setOnCheckedChangeListener { _, isChecked -> presenter.onVibrationEnabled(isChecked) }
    }
}