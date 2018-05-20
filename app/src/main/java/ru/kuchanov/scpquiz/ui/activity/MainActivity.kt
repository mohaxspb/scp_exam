package ru.kuchanov.scpquiz.ui.activity

import android.os.Bundle
import android.support.v4.app.FragmentManager
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import kotlinx.android.synthetic.main.activity_main.*
import ru.kuchanov.scpquiz.R
import ru.kuchanov.scpquiz.di.Di
import ru.kuchanov.scpquiz.di.module.MainActivityModule
import ru.kuchanov.scpquiz.mvp.presenter.MainPresenter
import ru.kuchanov.scpquiz.mvp.view.MainView
import ru.kuchanov.scpquiz.ui.BaseActivity
import ru.kuchanov.scpquiz.ui.fragment.AppInfoFragment
import toothpick.Toothpick
import javax.inject.Inject

class MainActivity : BaseActivity<MainView, MainPresenter>(), MainView {

    override val scopes = arrayOf(Di.Scope.MAIN_ACTIVITY)

    override val modules = arrayOf(MainActivityModule())

    @Inject
    lateinit var fragmentManager: FragmentManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        textView.setOnClickListener {
            presenter.onSomethingClick()

            fragmentManager
                    .beginTransaction()
                    .replace(R.id.root, AppInfoFragment())
                    .addToBackStack(null)
                    .commit()
        }
    }

    @InjectPresenter
    override lateinit var presenter: MainPresenter

    @ProvidePresenter
    override fun providePresenter(): MainPresenter = scope.getInstance(MainPresenter::class.java)

    override fun getLayoutResId() = R.layout.activity_main

    override fun inject() = Toothpick.inject(this, scope)
}
