package ru.kuchanov.scpquiz.ui.fragment

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.view.View
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import jp.wasabeef.blurry.Blurry
import kotlinx.android.synthetic.main.fragment_settings.*
import ru.kuchanov.scpquiz.R
import ru.kuchanov.scpquiz.di.Di
import ru.kuchanov.scpquiz.di.module.EnterModule
import ru.kuchanov.scpquiz.mvp.presenter.util.SettingsPresenter
import ru.kuchanov.scpquiz.mvp.view.SettingsView
import ru.kuchanov.scpquiz.ui.BaseFragment
import toothpick.Toothpick
import toothpick.config.Module


class SettingsFragment : BaseFragment<SettingsView, SettingsPresenter>(), SettingsView {
    override val translucent = false

    override val scopes: Array<String> = arrayOf(Di.Scope.ENTER_FRAGMENT)

    override val modules: Array<Module> = arrayOf(EnterModule())

    @InjectPresenter
    override lateinit var presenter: SettingsPresenter

    @ProvidePresenter
    override fun providePresenter(): SettingsPresenter = scope.getInstance(SettingsPresenter::class.java)

    override fun inject() = Toothpick.inject(this, scope)

    override fun getLayoutResId() = R.layout.fragment_settings

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Blurry.with(context)
//                .color(Color.argb(66, 50, 50, 50))
                .from(arguments!!["bitmap"] as Bitmap)
                .into(imageView);

//        root.post {
//            Blurry.with(context)
//                    .radius(25)
//                    .sampling(6)
//                    .async()
//                    .animate(500)
//                    .onto(root)
//        }
    }

    override fun showLang(langString: String) {
        currentLangTextView.text = langString
    }

    companion object {
        fun newInstance(background: Bitmap): SettingsFragment {
            val fragment = SettingsFragment()
            val args = Bundle()
            args.putParcelable("bitmap", background)
            fragment.arguments = args
            return fragment
        }
    }
}