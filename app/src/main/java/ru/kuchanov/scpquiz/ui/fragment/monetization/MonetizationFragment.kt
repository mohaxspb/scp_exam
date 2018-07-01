package ru.kuchanov.scpquiz.ui.fragment.monetization

import android.os.Bundle
import android.view.View
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import jp.wasabeef.blurry.Blurry
import kotlinx.android.synthetic.main.fragment_monetization.*
import ru.kuchanov.scpquiz.Constants
import ru.kuchanov.scpquiz.R
import ru.kuchanov.scpquiz.di.Di
import ru.kuchanov.scpquiz.di.module.MonetizationModule
import ru.kuchanov.scpquiz.mvp.presenter.monetization.MonetizationPresenter
import ru.kuchanov.scpquiz.mvp.view.monetization.MonetizationView
import ru.kuchanov.scpquiz.ui.BaseFragment
import ru.kuchanov.scpquiz.utils.BitmapUtils
import toothpick.Toothpick
import toothpick.config.Module


class MonetizationFragment : BaseFragment<MonetizationView, MonetizationPresenter>(), MonetizationView {

    companion object {

        fun newInstance() = MonetizationFragment()
    }

    override val translucent = true

    override val scopes: Array<String> = arrayOf(Di.Scope.MONETIZATION_FRAGMENT)

    override val modules: Array<Module> = arrayOf(MonetizationModule())

    @InjectPresenter
    override lateinit var presenter: MonetizationPresenter

    @ProvidePresenter
    override fun providePresenter(): MonetizationPresenter = scope.getInstance(MonetizationPresenter::class.java)

    override fun inject() = Toothpick.inject(this, scope)

    override fun getLayoutResId() = R.layout.fragment_monetization

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
    }
}