package ru.kuchanov.scpquiz.ui.activity

import android.os.Bundle
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import kotlinx.android.synthetic.main.activity_main.*
import ru.kuchanov.scpquiz.R
import ru.kuchanov.scpquiz.di.Di
import ru.kuchanov.scpquiz.di.module.MainActivityModule
import ru.kuchanov.scpquiz.mvp.presenter.MainPresenter
import ru.kuchanov.scpquiz.mvp.view.MainView
import ru.kuchanov.scpquiz.ui.BaseActivity
import toothpick.Toothpick

class MainActivity : BaseActivity<MainView, MainPresenter>(), MainView {

    override val scopes = arrayOf(Di.Scope.APP, Di.Scope.MAIN_ACTIVITY)

    override val modules = arrayOf(MainActivityModule())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        textView.setOnClickListener {
            presenter.onSomethingClick()
        }
    }

    @InjectPresenter
    override lateinit var presenter: MainPresenter

    @ProvidePresenter
    override fun providePresenter(): MainPresenter = scope.getInstance(MainPresenter::class.java)

    override fun getLayoutResId() = R.layout.activity_main

    override fun inject() = Toothpick.inject(this, scope)
}
