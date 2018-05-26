package ru.kuchanov.scpquiz.ui.fragment

import android.content.pm.PackageInfo
import android.os.Bundle
import android.view.View
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import kotlinx.android.synthetic.main.fragment_app_info.*
import ru.kuchanov.scpquiz.R
import ru.kuchanov.scpquiz.di.Di
import ru.kuchanov.scpquiz.di.module.AppInfoModule
import ru.kuchanov.scpquiz.mvp.presenter.AppInfoPresenter
import ru.kuchanov.scpquiz.mvp.view.AppInfoView
import ru.kuchanov.scpquiz.ui.BaseFragment
import toothpick.Toothpick
import javax.inject.Inject

class AppInfoFragment : BaseFragment<AppInfoView, AppInfoPresenter>(), AppInfoView {

    @Inject
    lateinit var packageInfo: PackageInfo

    override val scopes = arrayOf(Di.Scope.APP_INFO_FRAGMENT)

    override val modules = arrayOf(AppInfoModule())

    @InjectPresenter
    override lateinit var presenter: AppInfoPresenter

    @ProvidePresenter
    override fun providePresenter(): AppInfoPresenter = scope.getInstance(AppInfoPresenter::class.java)

    override fun inject() = Toothpick.inject(this, scope)

    override fun getLayoutResId() = R.layout.fragment_app_info

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        appVersionTextView.text = packageInfo.versionName
        appVersionTextView.setOnClickListener { presenter.onSomethingClick() }
    }

    companion object {
        fun newInstance() = AppInfoFragment()
    }
}