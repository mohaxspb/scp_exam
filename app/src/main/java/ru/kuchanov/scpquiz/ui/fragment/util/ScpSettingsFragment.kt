package ru.kuchanov.scpquiz.ui.fragment.util

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.PopupWindow
import com.afollestad.materialdialogs.MaterialDialog
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.bumptech.glide.request.RequestOptions
import com.hannesdorfmann.adapterdelegates3.AdapterDelegatesManager
import com.hannesdorfmann.adapterdelegates3.ListDelegationAdapter
import jp.wasabeef.blurry.Blurry
import kotlinx.android.synthetic.main.dialog_logout.view.*
import kotlinx.android.synthetic.main.fragment_settings.*
import ru.kuchanov.scpquiz.Constants
import ru.kuchanov.scpquiz.R
import ru.kuchanov.scpquiz.controller.adapter.MyListItem
import ru.kuchanov.scpquiz.controller.adapter.delegate.DelegateLang
import ru.kuchanov.scpquiz.controller.adapter.viewmodel.LangViewModel
import ru.kuchanov.scpquiz.controller.manager.preference.MyPreferenceManager
import ru.kuchanov.scpquiz.di.Di
import ru.kuchanov.scpquiz.di.module.SettingsModule
import ru.kuchanov.scpquiz.mvp.presenter.util.ScpSettingsPresenter
import ru.kuchanov.scpquiz.mvp.view.util.SettingsView
import ru.kuchanov.scpquiz.ui.BaseFragment
import ru.kuchanov.scpquiz.ui.utils.AuthDelegate
import ru.kuchanov.scpquiz.ui.utils.DialogUtils
import ru.kuchanov.scpquiz.ui.utils.GlideApp
import ru.kuchanov.scpquiz.utils.BitmapUtils
import ru.kuchanov.scpquiz.utils.SystemUtils
import ru.kuchanov.scpquiz.utils.security.FingerprintUtils
import timber.log.Timber
import toothpick.Toothpick
import toothpick.config.Module
import javax.inject.Inject


class ScpSettingsFragment : BaseFragment<SettingsView, ScpSettingsPresenter>(), SettingsView {

    companion object {

        fun newInstance() = ScpSettingsFragment()

        fun getIconForLang(langString: String) = when (langString) {
            "ru" -> R.drawable.ic_ru
            else -> R.drawable.ic_en
        }
    }

    @Inject
    lateinit var myPreferenceManager: MyPreferenceManager

    override val translucent = true

    override val scopes: Array<String> = arrayOf(Di.Scope.SETTINGS_FRAGMENT)

    override val modules: Array<Module> = arrayOf(SettingsModule())

    private lateinit var authDelegate: AuthDelegate<ScpSettingsFragment>

    @InjectPresenter
    override lateinit var presenter: ScpSettingsPresenter

    @ProvidePresenter
    override fun providePresenter(): ScpSettingsPresenter = scope.getInstance(ScpSettingsPresenter::class.java)

    override fun inject() = Toothpick.inject(this, scope)

    override fun getLayoutResId() = R.layout.fragment_settings

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        authDelegate = AuthDelegate(
                this,
                presenter,
                presenter.apiClient,
                presenter.preferences
        )
        presenter.authDelegate = authDelegate
        activity?.let { authDelegate.onViewCreated(it) }

        //todo move to delegate
        val bitmap = BitmapUtils.fileToBitmap("${activity?.cacheDir}/${Constants.SETTINGS_BACKGROUND_FILE_NAME}.png")

        backgroundImageView.post {
            Blurry.with(context)
                    .async()
                    .animate(500)
                    .from(bitmap)
                    .into(backgroundImageView)
        }

        coinsLabelTextView.setOnClickListener { presenter.onCoinsClicked() }
        coinsImageView.setOnClickListener { presenter.onCoinsClicked() }

        val onLangClickListener: (View) -> Unit = { presenter.onLangClicked() }
        languageLabelTextView.setOnClickListener(onLangClickListener)
        languageImageView.setOnClickListener(onLangClickListener)

        soundSwitch.setOnCheckedChangeListener { _, isChecked -> presenter.onSoundEnabled(isChecked) }
        vibrateSwitch.setOnCheckedChangeListener { _, isChecked -> presenter.onVibrationEnabled(isChecked) }
        Timber.d("FingerprintUtils.isFingerprintSupported(): ${FingerprintUtils.isFingerprintSupported()}")
        if (FingerprintUtils.isFingerprintSupported()) {
            fingerprintLabelTextView.visibility = VISIBLE
            fingerprintSwitch.visibility = VISIBLE
            fingerprintSwitch.setOnCheckedChangeListener { _, isChecked -> presenter.onFingerPrintEnabled(isChecked) }
        } else {
            fingerprintLabelTextView.visibility = GONE
            fingerprintSwitch.visibility = GONE
        }
        val onShareClickListener: (View) -> Unit = { presenter.onShareClicked() }
        shareImageView.setOnClickListener(onShareClickListener)
        shareLabelTextView.setOnClickListener(onShareClickListener)

        if (myPreferenceManager.getAccessToken() == null) {
            logoutLabelTextView.visibility = GONE
            logoutImageView.visibility = GONE
            vkImage.visibility = VISIBLE
            faceBookImage.visibility = VISIBLE
            googleImage.visibility = VISIBLE
        } else {
            logoutLabelTextView.visibility = VISIBLE
            logoutImageView.visibility = VISIBLE
            vkImage.visibility = GONE
            faceBookImage.visibility = GONE
            googleImage.visibility = GONE
        }

        val onLogoutClickListener: (View) -> Unit = { showLogoutDialog() }
        logoutLabelTextView.setOnClickListener(onLogoutClickListener)
        logoutImageView.setOnClickListener(onLogoutClickListener)

        val onResetProgressClickListener: (View) -> Unit = { showResetProgressDialog() }
        resetProgressLabelTextView.setOnClickListener(onResetProgressClickListener)
        resetProgressImageView.setOnClickListener(onLogoutClickListener)

        val onVkLoginClickListener: (View) -> Unit = { presenter.onVkLoginClicked() }
        vkImage.setOnClickListener(onVkLoginClickListener)

        val onFacebookLoginClickListener: (View) -> Unit = { presenter.onFacebookLoginClicked() }
        faceBookImage.setOnClickListener(onFacebookLoginClickListener)

        val onGoogleLoginClickListener: (View) -> Unit = { presenter.onGoogleLoginClicked() }
        googleImage.setOnClickListener(onGoogleLoginClickListener)

        privacyPolicyLabelTextView.setOnClickListener { presenter.onPrivacyPolicyClicked() }

        toolbar.setNavigationOnClickListener { presenter.onNavigationIconClicked() }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        presenter.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onPause() {
        super.onPause()
        authDelegate.onPause()
    }

    @SuppressLint("InflateParams")
    private fun showLogoutDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_logout, null, false)
        GlideApp
                .with(activity!!)
                .load(R.drawable.ic_doctor)
                .apply(RequestOptions.circleCropTransform())
                .into(dialogView.doctorImageView)
        activity?.let {
            MaterialDialog.Builder(it)
                    .customView(dialogView, true)
                    .positiveText(R.string.OK)
                    .onPositive { dialog, _ ->
                        presenter.onLogoutClicked()
                        dialog.cancel()
                    }
                    .negativeText(R.string.cancel)
                    .onNegative { dialog, _ -> dialog.cancel() }
                    .canceledOnTouchOutside(true)
                    .cancelable(true)
                    .build()
                    .show()
        }
    }

    @SuppressLint("InflateParams")
    private fun showResetProgressDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_reset_progress, null, false)
        GlideApp
                .with(activity!!)
                .load(R.drawable.ic_doctor)
                .apply(RequestOptions.circleCropTransform())
                .into(dialogView.doctorImageView)
        activity?.let {
            MaterialDialog.Builder(it)
                    .customView(dialogView, true)
                    .positiveText(R.string.OK)
                    .onPositive { dialog, _ ->
                        presenter.onResetProgressClicked()
                        dialog.cancel()
                    }
                    .negativeText(R.string.cancel)
                    .onNegative { dialog, _ -> dialog.cancel() }
                    .canceledOnTouchOutside(true)
                    .cancelable(true)
                    .build()
                    .show()
        }
    }

    override fun showProgress(show: Boolean) {
        progressView.visibility = if (show) View.VISIBLE else View.GONE
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

    override fun showFingerprint(enabled: Boolean) {
        fingerprintSwitch.setOnCheckedChangeListener(null)
        fingerprintSwitch.isChecked = enabled
        fingerprintSwitch.setOnCheckedChangeListener { _, isChecked -> presenter.onFingerPrintEnabled(isChecked) }
    }

    @TargetApi(Build.VERSION_CODES.M)
    override fun showFingerprintDialog(enableFingerprintLogin: Boolean) {
        if (isAdded) {
            DialogUtils.showFingerprintDialog(
                    context = activity!!,
                    title = if (enableFingerprintLogin) R.string.dialog_fingerprint_enable_title else R.string.dialog_fingerprint_disable_title,
                    onErrorAction = { showMessage(R.string.error_fingerprint_auth_failed_try_again) },
                    onCipherErrorAction = { showMessage(R.string.error_get_chipher) },
                    onSuccessAction = { presenter.onFingerprintAuthSucceeded(enableFingerprintLogin, it) }
            )
        }
    }
}

