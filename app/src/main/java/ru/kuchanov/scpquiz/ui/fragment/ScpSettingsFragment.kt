package ru.kuchanov.scpquiz.ui.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.hannesdorfmann.adapterdelegates3.AdapterDelegatesManager
import com.hannesdorfmann.adapterdelegates3.ListDelegationAdapter
import jp.wasabeef.blurry.Blurry
import kotlinx.android.synthetic.main.fragment_settings.*
import ru.kuchanov.scpquiz.Constants
import ru.kuchanov.scpquiz.R
import ru.kuchanov.scpquiz.controller.adapter.MyListItem
import ru.kuchanov.scpquiz.controller.adapter.delegate.DelegateLang
import ru.kuchanov.scpquiz.controller.adapter.viewmodel.LangViewModel
import ru.kuchanov.scpquiz.di.Di
import ru.kuchanov.scpquiz.di.module.EnterModule
import ru.kuchanov.scpquiz.mvp.presenter.util.SettingsPresenter
import ru.kuchanov.scpquiz.mvp.view.SettingsView
import ru.kuchanov.scpquiz.ui.BaseFragment
import ru.kuchanov.scpquiz.utils.BitmapUtils
import ru.kuchanov.scpquiz.utils.DimensionUtils
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
        val wrapContent = ViewGroup.LayoutParams.WRAP_CONTENT

        //закрывать при таче за пределами окна
        val focusable = true
        val popupWindow = PopupWindow(
            createPopupView(langs.toList(), lang),
            wrapContent,
            wrapContent,
            focusable)

        val screenHeight = DimensionUtils.getScreenHeight()

        popupWindow.showAtLocation(
            languageImageView,
            Gravity.START or Gravity.BOTTOM,
            0,
            (screenHeight - getPopupMenuLocation()) / 2
        )
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

    @SuppressLint("InflateParams")
    private fun createPopupView(langs: List<String>, lang: String): View {
        val context = activity
        val inflater = LayoutInflater.from(context)
        val popupView = inflater.inflate(R.layout.popup_window_middle, null, false)
        val recyclerView = popupView.findViewById(R.id.recyclerView) as RecyclerView
//        val cardView = popupView.findViewById(R.id.cardView) as CardView

        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        val manager = AdapterDelegatesManager<List<MyListItem>>()
        manager.addDelegate(DelegateLang{ presenter.onLangSelected(it) })
        val adapter = ListDelegationAdapter(manager)
        recyclerView.adapter = adapter
        adapter.items = langs.map { LangViewModel(it, it == lang) }

//        //make window smaller for less then 3 items
//        when (recyclerView.adapter.itemCount) {
//            1 -> cardView.layoutParams.height /= 3
//            2 -> cardView.layoutParams.height = cardView.layoutParams.height / 3 * 2
//        }

        return popupView
    }

    private fun getPopupMenuLocation(): Int {
        val originalPos = IntArray(2)
        languageImageView.getLocationOnScreen(originalPos)
        return originalPos[1]
    }
}