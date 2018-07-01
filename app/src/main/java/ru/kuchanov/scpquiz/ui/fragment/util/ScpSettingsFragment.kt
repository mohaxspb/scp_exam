package ru.kuchanov.scpquiz.ui.fragment.util

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.hannesdorfmann.adapterdelegates3.AdapterDelegatesManager
import com.hannesdorfmann.adapterdelegates3.ListDelegationAdapter
import jp.wasabeef.blurry.Blurry
import ru.kuchanov.scpquiz.Constants
import ru.kuchanov.scpquiz.R
import ru.kuchanov.scpquiz.controller.adapter.MyListItem
import ru.kuchanov.scpquiz.controller.adapter.delegate.DelegateLang
import ru.kuchanov.scpquiz.controller.adapter.viewmodel.LangViewModel
import ru.kuchanov.scpquiz.di.Di
import ru.kuchanov.scpquiz.di.module.SettingsModule
import ru.kuchanov.scpquiz.mvp.presenter.util.SettingsPresenter
import ru.kuchanov.scpquiz.mvp.view.util.SettingsView
import ru.kuchanov.scpquiz.ui.BaseFragment
import ru.kuchanov.scpquiz.utils.BitmapUtils
import ru.kuchanov.scpquiz.utils.SystemUtils
import timber.log.Timber
import toothpick.Toothpick
import toothpick.config.Module


class ScpSettingsFragment : BaseFragment<SettingsView, SettingsPresenter>(), SettingsView {

    companion object {

        fun newInstance() = ScpSettingsFragment()

        fun getIconForLang(langString: String) = when (langString) {
            "ru" -> R.drawable.ic_ru
            else -> R.drawable.ic_en
        }
    }

    override val translucent = true

    override val scopes: Array<String> = arrayOf(Di.Scope.SETTINGS_FRAGMENT)

    override val modules: Array<Module> = arrayOf(SettingsModule())

    @InjectPresenter
    override lateinit var presenter: SettingsPresenter

    @ProvidePresenter
    override fun providePresenter(): SettingsPresenter = scope.getInstance(SettingsPresenter::class.java)

    override fun inject() = Toothpick.inject(this, scope)

    override fun getLayoutResId() = R.layout.fragment_settings

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bitmap = BitmapUtils.fileToBitmap("${activity?.cacheDir}/${Constants.SETTINGS_BACKGROUND_FILE_NAME}.png")

        backgroundImageView.post {
            Blurry.with(context)
                    .async()
                    .animate(500)
                    .from(bitmap)
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
        val langRes = getIconForLang(langString)
        languageImageView.setImageResource(langRes)
    }

    override fun showLangsChooser(langs: Set<String>, lang: String) {
        Timber.d("showLangsChooser: $langs")

        val popupView = LayoutInflater.from(activity).inflate(R.layout.popup_window_middle, root, false)

        //закрывать при таче за пределами окна
        val focusable = true
        val wrapContent = ViewGroup.LayoutParams.WRAP_CONTENT
        val popupWindow = PopupWindow(
            popupView,
            wrapContent,
            wrapContent,
            focusable
        )

        val recyclerView = popupView.findViewById(R.id.recyclerView) as RecyclerView

        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        val manager = AdapterDelegatesManager<List<MyListItem>>()
        manager.addDelegate(DelegateLang {
            presenter.onLangSelected(it)
            popupWindow.dismiss()
        })
        val adapter = ListDelegationAdapter(manager)
        recyclerView.adapter = adapter
        adapter.items = langs.map { LangViewModel(it, it == lang) }

        popupWindow.showAsDropDown(languageImageView)
    }

    override fun showSound(enabled: Boolean) {
        soundSwitch.setOnCheckedChangeListener(null)
        soundSwitch.isChecked = enabled
        soundSwitch.setOnCheckedChangeListener { _, isChecked -> presenter.onSoundEnabled(isChecked) }
    }

    override fun showVibration(enabled: Boolean) {
        vibrateSwitch.setOnCheckedChangeListener(null)
        vibrateSwitch.isChecked = enabled
        vibrateSwitch.setOnCheckedChangeListener { _, isChecked ->
            run {
                if (isChecked) {
                    SystemUtils.vibrate()
                }
                presenter.onVibrationEnabled(isChecked)
            }
        }
    }
}