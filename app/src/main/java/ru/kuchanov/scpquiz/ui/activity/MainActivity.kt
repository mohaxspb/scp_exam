package ru.kuchanov.scpquiz.ui.activity

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.support.annotation.IdRes
import android.support.v4.app.Fragment
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import ru.kuchanov.scpquiz.Constants
import ru.kuchanov.scpquiz.R
import ru.kuchanov.scpquiz.controller.navigation.ShowCommand
import ru.kuchanov.scpquiz.di.Di
import ru.kuchanov.scpquiz.di.module.MainActivityModule
import ru.kuchanov.scpquiz.mvp.presenter.activity.MainPresenter
import ru.kuchanov.scpquiz.mvp.view.MainView
import ru.kuchanov.scpquiz.ui.BaseActivity
import ru.kuchanov.scpquiz.ui.fragment.*
import ru.terrakok.cicerone.Navigator
import ru.terrakok.cicerone.android.SupportAppNavigator
import ru.terrakok.cicerone.commands.Command
import timber.log.Timber
import toothpick.Toothpick


class MainActivity : BaseActivity<MainView, MainPresenter>(), MainView {

    @IdRes
    override val containerId = R.id.container

    override val scopes = arrayOf(Di.Scope.MAIN_ACTIVITY)

    override val modules = arrayOf(MainActivityModule())

    override var navigator: Navigator = object : SupportAppNavigator(this, containerId) {

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
                Constants.Screens.SETTINGS -> ScpSettingsFragment.newInstance(data as Bitmap)
                else -> null
            }
        }

        override fun applyCommand(command: Command?) {
            Timber.d("applyCommand: ${command?.javaClass?.simpleName ?: command}")
            if (command is ShowCommand) {
                supportFragmentManager.beginTransaction()
                        .add(containerId, createFragment(command.screenKey, command.transitionData))
                        .addToBackStack(null)
                        .commit()
            } else {
                super.applyCommand(command)
            }
        }
    }

    @InjectPresenter
    override lateinit var presenter: MainPresenter

    @ProvidePresenter
    override fun providePresenter(): MainPresenter = scope.getInstance(MainPresenter::class.java)

    override fun getLayoutResId() = R.layout.activity_main

    override fun inject() = Toothpick.inject(this, scope)
}
