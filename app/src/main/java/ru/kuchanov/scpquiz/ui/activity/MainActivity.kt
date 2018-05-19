package ru.kuchanov.scpquiz.ui.activity

import android.content.pm.PackageInfo
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.PresenterType
import com.arellomobile.mvp.presenter.ProvidePresenter
import kotlinx.android.synthetic.main.activity_main.*
import ru.kuchanov.scpquiz.R
import ru.kuchanov.scpquiz.di.Di
import ru.kuchanov.scpquiz.di.SCOPE_APP
import ru.kuchanov.scpquiz.mvp.presenter.MainPresenter
import ru.kuchanov.scpquiz.mvp.view.MainView
import ru.kuchanov.scpquiz.ui.BaseActivity
import toothpick.Scope
import toothpick.Toothpick

import javax.inject.Inject

class MainActivity : BaseActivity<MainView, MainPresenter>() {

    @Inject
    lateinit var packageInfo: PackageInfo

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textView.setOnClickListener { Snackbar.make(root, packageInfo.packageName, Snackbar.LENGTH_LONG).show() }
    }

    @InjectPresenter
    override lateinit var presenter: MainPresenter

    @ProvidePresenter
    override fun providePresenter(): MainPresenter {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getLayoutResId() = R.layout.activity_main

    override fun inject() = Toothpick.inject(this, Toothpick.openScope(Di.Scope.APP))
}
