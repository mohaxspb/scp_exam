package ru.kuchanov.scpquiz.ui.activity

import android.content.Context
import android.content.Intent
import android.support.v4.app.Fragment
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import ru.kuchanov.scpquiz.Constants
import ru.kuchanov.scpquiz.R
import ru.kuchanov.scpquiz.di.Di
import ru.kuchanov.scpquiz.di.module.MainActivityModule
import ru.kuchanov.scpquiz.mvp.presenter.MainPresenter
import ru.kuchanov.scpquiz.mvp.view.MainView
import ru.kuchanov.scpquiz.ui.BaseActivity
import ru.kuchanov.scpquiz.ui.fragment.AppInfoFragment
import ru.kuchanov.scpquiz.ui.fragment.EnterFragment
import ru.kuchanov.scpquiz.ui.fragment.GameFragment
import ru.kuchanov.scpquiz.ui.fragment.LevelsFragment
import ru.terrakok.cicerone.Navigator
import ru.terrakok.cicerone.android.SupportAppNavigator
import ru.terrakok.cicerone.commands.Command
import timber.log.Timber
import toothpick.Toothpick

class MainActivity : BaseActivity<MainView, MainPresenter>(), MainView {

    override val scopes = arrayOf(Di.Scope.MAIN_ACTIVITY)

    override val modules = arrayOf(MainActivityModule())

    override var navigator: Navigator = object : SupportAppNavigator(this, R.id.container) {
        override fun createActivityIntent(context: Context, screenKey: String?, data: Any?): Intent? {
            Timber.d("createActivityIntent key: $screenKey, data: $data")
            return when (screenKey) {
//                Constants.Screens.AUTH -> AuthActivity.newIntent(this@MainActivity)
                else -> null
            }
        }

        override fun createFragment(screenKey: String?, data: Any?): Fragment? {
            Timber.d("createFragment key: $screenKey, data: $data")
            return when (screenKey) {
                Constants.Screens.ENTER -> EnterFragment.newInstance()
                Constants.Screens.APP_INFO -> AppInfoFragment.newInstance()
                Constants.Screens.QUIZ_LIST -> LevelsFragment.newInstance()
                Constants.Screens.QUIZ -> GameFragment.newInstance(data as Long)
                else -> null
            }
        }

        override fun applyCommand(command: Command?) {
            Timber.d("applyCommand: ${command?.javaClass?.simpleName ?: command}")
            super.applyCommand(command)
        }
    }

    @InjectPresenter
    override lateinit var presenter: MainPresenter

    @ProvidePresenter
    override fun providePresenter(): MainPresenter = scope.getInstance(MainPresenter::class.java)

    override fun getLayoutResId() = R.layout.activity_main

    override fun inject() = Toothpick.inject(this, scope)
}
