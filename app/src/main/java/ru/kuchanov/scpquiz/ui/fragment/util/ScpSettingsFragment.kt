package ru.kuchanov.scpquiz.ui.fragment.util

import android.annotation.TargetApi
import android.os.Build
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat
import android.support.v4.os.CancellationSignal
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.PopupWindow
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.callbacks.onDismiss
import com.afollestad.materialdialogs.customview.customView
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.hannesdorfmann.adapterdelegates3.AdapterDelegatesManager
import com.hannesdorfmann.adapterdelegates3.ListDelegationAdapter
import jp.wasabeef.blurry.Blurry
import kotlinx.android.synthetic.main.dialog_fingerprint.view.*
import kotlinx.android.synthetic.main.fragment_settings.*
import ru.kuchanov.scpquiz.Constants
import ru.kuchanov.scpquiz.R
import ru.kuchanov.scpquiz.controller.adapter.MyListItem
import ru.kuchanov.scpquiz.controller.adapter.delegate.DelegateLang
import ru.kuchanov.scpquiz.controller.adapter.viewmodel.LangViewModel
import ru.kuchanov.scpquiz.di.Di
import ru.kuchanov.scpquiz.di.module.SettingsModule
import ru.kuchanov.scpquiz.mvp.presenter.util.ScpSettingsPresenter
import ru.kuchanov.scpquiz.mvp.view.util.SettingsView
import ru.kuchanov.scpquiz.ui.BaseFragment
import ru.kuchanov.scpquiz.utils.BitmapUtils
import ru.kuchanov.scpquiz.utils.SystemUtils
import ru.kuchanov.scpquiz.utils.security.FingerprintUtils
import timber.log.Timber
import toothpick.Toothpick
import toothpick.config.Module


class ScpSettingsFragment : BaseFragment<SettingsView, ScpSettingsPresenter>(), SettingsView {

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
    override lateinit var presenter: ScpSettingsPresenter

    @ProvidePresenter
    override fun providePresenter(): ScpSettingsPresenter = scope.getInstance(ScpSettingsPresenter::class.java)

    override fun inject() = Toothpick.inject(this, scope)

    override fun getLayoutResId() = R.layout.fragment_settings

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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

        privacyPolicyLabelTextView.setOnClickListener { presenter.onPrivacyPolicyClicked() }

        toolbar.setNavigationOnClickListener { presenter.onNavigationIconClicked() }
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
    override fun showFingerprintDialog(show: Boolean) {
        if (isAdded) {
            val cancellationSignal = CancellationSignal()
            cancellationSignal.setOnCancelListener { Timber.d("cancellationSignal canceled!") }
            val dismissFingerprintSensor = {
                //todo dismiss sensor in onPause
                cancellationSignal.cancel()
            }

            var materialDialog: MaterialDialog? = null

            val dialogView = LayoutInflater.from(activity!!).inflate(R.layout.dialog_fingerprint, null, false)

            val fingerprintCallback = object : FingerprintManagerCompat.AuthenticationCallback() {
                /**
                 * несколько неудачных попыток считывания (5)
                 *
                 * после этого сенсор станет недоступным на некоторое время (30 сек)
                 */
                override fun onAuthenticationError(errMsgId: Int, errString: CharSequence?) {
                    super.onAuthenticationError(errMsgId, errString)
                    Timber.d("onAuthenticationError: $errMsgId, $errString")
                    showMessage(R.string.error_fingerprint_auth_failed_try_again)
                    materialDialog?.dismiss()
                }

                /**
                 * все прошло успешно
                 */
                override fun onAuthenticationSucceeded(result: FingerprintManagerCompat.AuthenticationResult?) {
                    super.onAuthenticationSucceeded(result)
                    Timber.d("onAuthenticationSucceeded: $result, ${result?.cryptoObject?.cipher?.parameters}")
                    presenter.onFingerprintAuthSucceeded(result?.cryptoObject?.cipher)
                    materialDialog?.dismiss()
                }

                /**
                 * грязные пальчики, недостаточно сильный зажим
                 */
                override fun onAuthenticationHelp(helpMsgId: Int, helpString: CharSequence?) {
                    super.onAuthenticationHelp(helpMsgId, helpString)
                    Timber.d("onAuthenticationHelp: $helpMsgId, $helpString")
                    dialogView.sensorTextView.text = helpString?.toString() ?: getString(R.string.try_again)
                    dialogView.sensorImageView.setImageResource(R.drawable.ic_info_outline_black_24dp)
                    dialogView.sensorImageView.setColorFilter(ContextCompat.getColor(activity!!, android.R.color.black))
                }

                /**
                 * отпечаток считался, но не распознался
                 */
                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Timber.d("onAuthenticationFailed")
                    dialogView.sensorTextView.setText(R.string.error_fingerprint_auth_failed)
                    dialogView.sensorImageView.setImageResource(R.drawable.ic_warning_black_24dp)
                    dialogView.sensorImageView.setColorFilter(ContextCompat.getColor(activity!!, R.color.colorRed))
                }
            }

            materialDialog = MaterialDialog(activity!!)
                    .title(R.string.dialog_fingerprints_title)
                    .negativeButton(android.R.string.cancel) { dismissFingerprintSensor.invoke() }
                    .customView(view = dialogView)
                    .onDismiss { dismissFingerprintSensor.invoke() }
                    .show {
                        FingerprintUtils.useFingerprintSensor(
                            cancellationSignal,
                            fingerprintCallback
                        )
                    }
        }
    }
}