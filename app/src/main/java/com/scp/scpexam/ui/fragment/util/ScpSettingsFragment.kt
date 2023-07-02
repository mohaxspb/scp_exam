package com.scp.scpexam.ui.fragment.util

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.bumptech.glide.request.RequestOptions
import com.hannesdorfmann.adapterdelegates3.AdapterDelegatesManager
import com.hannesdorfmann.adapterdelegates3.ListDelegationAdapter
import com.scp.scpexam.Constants
import com.scp.scpexam.R
import com.scp.scpexam.controller.adapter.MyListItem
import com.scp.scpexam.controller.adapter.delegate.DelegateLang
import com.scp.scpexam.controller.adapter.viewmodel.LangViewModel
import com.scp.scpexam.controller.manager.preference.MyPreferenceManager
import com.scp.scpexam.databinding.DialogLogoutBinding
import com.scp.scpexam.databinding.DialogResetProgressBinding
import com.scp.scpexam.databinding.FragmentSettingsBinding
import com.scp.scpexam.mvp.presenter.util.ScpSettingsPresenter
import com.scp.scpexam.mvp.view.util.SettingsView
import com.scp.scpexam.ui.BaseFragment
import com.scp.scpexam.ui.utils.AuthDelegate
import com.scp.scpexam.ui.utils.GlideApp
import com.scp.scpexam.utils.BitmapUtils
import com.scp.scpexam.utils.LocaleUtils
import com.scp.scpexam.utils.SystemUtils
import jp.wasabeef.blurry.Blurry
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import timber.log.Timber
import toothpick.Toothpick
import toothpick.config.Module
import javax.inject.Inject


class ScpSettingsFragment :
    BaseFragment<SettingsView, ScpSettingsPresenter, FragmentSettingsBinding>(), SettingsView {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentSettingsBinding
        get() = FragmentSettingsBinding::inflate

    @Inject
    lateinit var myPreferenceManager: MyPreferenceManager

    override val translucent = true

    override val scopes: Array<String> = arrayOf()

    override val modules: Array<Module> = arrayOf()

    private lateinit var authDelegate: AuthDelegate<ScpSettingsFragment>

    @InjectPresenter
    override lateinit var presenter: ScpSettingsPresenter

    @ProvidePresenter
    override fun providePresenter(): ScpSettingsPresenter =
        scope.getInstance(ScpSettingsPresenter::class.java)

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
        val bitmap =
            BitmapUtils.fileToBitmap("${activity?.cacheDir}/${Constants.SETTINGS_BACKGROUND_FILE_NAME}.png")

        binding.backgroundImageView.post {
            Blurry.with(context)
                .async()
                .animate(500)
                .from(bitmap)
                .into(binding.backgroundImageView)
        }

        binding.coinsLabelTextView.setOnClickListener { presenter.onCoinsClicked() }
        binding.coinsImageView.setOnClickListener { presenter.onCoinsClicked() }

        val onLangClickListener: (View) -> Unit = { presenter.onLangClicked() }
        binding.languageLabelTextView.setOnClickListener(onLangClickListener)
        binding.languageImageView.setOnClickListener(onLangClickListener)

        binding.soundSwitch.setOnCheckedChangeListener { _, isChecked ->
            presenter.onSoundEnabled(
                isChecked
            )
        }
        binding.vibrateSwitch.setOnCheckedChangeListener { _, isChecked ->
            presenter.onVibrationEnabled(
                isChecked
            )
        }

        val onShareClickListener: (View) -> Unit = { presenter.onShareClicked() }
        binding.shareImageView.setOnClickListener(onShareClickListener)
        binding.shareLabelTextView.setOnClickListener(onShareClickListener)

        if (myPreferenceManager.getTrueAccessToken() == null) {
            binding.logoutLabelTextView.visibility = GONE
            binding.logoutImageView.visibility = GONE
            binding.vkImage.visibility = VISIBLE
            binding.faceBookImage.visibility = VISIBLE
            binding.googleImage.visibility = VISIBLE
        } else {
            binding.logoutLabelTextView.visibility = VISIBLE
            binding.logoutImageView.visibility = VISIBLE
            binding.vkImage.visibility = GONE
            binding.faceBookImage.visibility = GONE
            binding.googleImage.visibility = GONE
        }

        val onLogoutClickListener: (View) -> Unit = { showLogoutDialog() }
        binding.logoutLabelTextView.setOnClickListener(onLogoutClickListener)
        binding.logoutImageView.setOnClickListener(onLogoutClickListener)

        val onResetProgressClickListener: (View) -> Unit = { showResetProgressDialog() }
        binding.resetProgressLabelTextView.setOnClickListener(onResetProgressClickListener)
        binding.resetProgressImageView.setOnClickListener(onLogoutClickListener)

        val onVkLoginClickListener: (View) -> Unit = { presenter.onVkLoginClicked() }
        binding.vkImage.setOnClickListener(onVkLoginClickListener)

        val onFacebookLoginClickListener: (View) -> Unit = { presenter.onFacebookLoginClicked() }
        binding.faceBookImage.setOnClickListener(onFacebookLoginClickListener)

        val onGoogleLoginClickListener: (View) -> Unit = { presenter.onGoogleLoginClicked() }
        binding.googleImage.setOnClickListener(onGoogleLoginClickListener)

        binding.privacyPolicyLabelTextView.setOnClickListener { presenter.onPrivacyPolicyClicked() }

        binding.toolbar.setNavigationOnClickListener { presenter.onNavigationIconClicked() }
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
        val dialogView = DialogLogoutBinding.inflate(layoutInflater, null, false)
        GlideApp
            .with(requireActivity())
            .load(R.drawable.ic_doctor)
            .apply(RequestOptions.circleCropTransform())
            .into(dialogView.doctorImageView)
        activity?.let {
            MaterialDialog.Builder(it)
                .customView(dialogView.root, true)
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
        val dialogView =
            DialogResetProgressBinding.inflate(layoutInflater, null, false)
        GlideApp
            .with(requireActivity())
            .load(R.drawable.ic_doctor)
            .apply(RequestOptions.circleCropTransform())
            .into(dialogView.doctorImageView)
        activity?.let {
            MaterialDialog.Builder(it)
                .customView(dialogView.root, true)
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
        binding.progressView.visibility = if (show) View.VISIBLE else View.GONE
    }

    override fun showLang(langCode: String) {
        Timber.d("showLang: $langCode")
        binding.languageImageView.countryCode = LocaleUtils.countryCodeFromLocale(langCode)
    }

    override fun showLangsChooser(langs: Set<String>, lang: String) {
        Timber.d("showLangsChooser: $langs")

        val popupView =
            LayoutInflater.from(activity).inflate(R.layout.popup_window_middle, binding.root, false)

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

        recyclerView.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        val manager = AdapterDelegatesManager<List<MyListItem>>()
        manager.addDelegate(
            DelegateLang {
                presenter.onLangSelected(it)
                popupWindow.dismiss()
            }
        )
        val adapter = ListDelegationAdapter(manager)
        recyclerView.adapter = adapter
        adapter.items = langs.map { LangViewModel(it, it == lang) }

        popupWindow.showAsDropDown(binding.languageImageView)
    }

    override fun showSound(enabled: Boolean) {
        binding.soundSwitch.setOnCheckedChangeListener(null)
        binding.soundSwitch.isChecked = enabled
        binding.soundSwitch.setOnCheckedChangeListener { _, isChecked ->
            presenter.onSoundEnabled(
                isChecked
            )
        }
    }

    override fun showVibration(enabled: Boolean) {
        binding.vibrateSwitch.setOnCheckedChangeListener(null)
        binding.vibrateSwitch.isChecked = enabled
        binding.vibrateSwitch.setOnCheckedChangeListener { _, isChecked ->
            run {
                if (isChecked) {
                    SystemUtils.vibrate()
                }
                presenter.onVibrationEnabled(isChecked)
            }
        }
    }

    companion object {
        fun newInstance() = ScpSettingsFragment()
    }
}

