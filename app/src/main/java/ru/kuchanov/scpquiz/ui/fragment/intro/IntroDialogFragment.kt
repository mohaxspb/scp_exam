package ru.kuchanov.scpquiz.ui.fragment.intro

import android.animation.LayoutTransition
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import jp.wasabeef.blurry.Blurry
import kotlinx.android.synthetic.main.fragment_intro_dialog.*
import ru.kuchanov.scpquiz.Constants
import ru.kuchanov.scpquiz.R
import ru.kuchanov.scpquiz.controller.manager.preference.MyPreferenceManager
import ru.kuchanov.scpquiz.di.Di
import ru.kuchanov.scpquiz.di.module.IntroDialogModule
import ru.kuchanov.scpquiz.model.db.User
import ru.kuchanov.scpquiz.model.ui.ChatAction
import ru.kuchanov.scpquiz.model.ui.ChatActionsGroupType
import ru.kuchanov.scpquiz.mvp.presenter.intro.IntroDialogPresenter
import ru.kuchanov.scpquiz.mvp.view.intro.IntroDialogView
import ru.kuchanov.scpquiz.ui.BaseFragment
import ru.kuchanov.scpquiz.ui.utils.AuthDelegate
import ru.kuchanov.scpquiz.ui.utils.ChatDelegate
import ru.kuchanov.scpquiz.utils.BitmapUtils
import toothpick.Toothpick
import toothpick.config.Module
import javax.inject.Inject


class IntroDialogFragment : BaseFragment<IntroDialogView, IntroDialogPresenter>(), IntroDialogView {

    companion object {
        fun newInstance() = IntroDialogFragment()
    }

    @Inject
    lateinit var myPreferenceManager: MyPreferenceManager

    private lateinit var chatDelegate: ChatDelegate

    private lateinit var authDelegate: AuthDelegate<IntroDialogFragment>

    override val translucent = true

    override val scopes: Array<String> = arrayOf(Di.Scope.INTRO_DIALOG_FRAGMENT)

    override val modules: Array<Module> = arrayOf(IntroDialogModule())

    @InjectPresenter
    override lateinit var presenter: IntroDialogPresenter

    @ProvidePresenter
    override fun providePresenter(): IntroDialogPresenter = scope.getInstance(IntroDialogPresenter::class.java)

    override fun inject() = Toothpick.inject(this, scope)

    override fun getLayoutResId() = R.layout.fragment_intro_dialog

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

        chatDelegate = ChatDelegate(
                chatView,
                scrollView,
                myPreferenceManager
        )



        //todo move to delegate
        val bitmap = BitmapUtils.fileToBitmap("${activity?.cacheDir}/${Constants.INTRO_DIALOG_BACKGROUND_FILE_NAME}.png")

        context?.let {
            backgroundImageView.post {
                if (isAdded) {
                    Blurry.with(it)
                            .async()
                            .animate(500)
                            .from(bitmap)
                            .into(backgroundImageView)
                }
            }
        }

        chatView.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
    }

    override fun showChatMessage(message: String, user: User) =
            chatDelegate.showChatMessage(
                    message,
                    user,
                    android.R.color.white
            )

    override fun showChatActions(chatActions: List<ChatAction>, chatActionsGroupType: ChatActionsGroupType) =
            chatDelegate.showChatActions(chatActions, chatActionsGroupType)

    override fun removeChatAction(indexInParent: Int) =
            chatDelegate.removeChatAction(indexInParent)

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        presenter.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onPause() {
        super.onPause()
        authDelegate.onPause()
    }
}