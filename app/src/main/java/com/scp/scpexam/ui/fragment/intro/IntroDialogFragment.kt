package com.scp.scpexam.ui.fragment.intro

import android.animation.LayoutTransition
import android.content.Intent
import android.os.Bundle
import android.view.View
import jp.wasabeef.blurry.Blurry
import kotlinx.android.synthetic.main.fragment_intro_dialog.*
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import com.scp.scpexam.Constants
import com.scp.scpexam.R
import com.scp.scpexam.controller.manager.preference.MyPreferenceManager
import com.scp.scpexam.model.db.User
import com.scp.scpexam.model.ui.ChatAction
import com.scp.scpexam.model.ui.ChatActionsGroupType
import com.scp.scpexam.mvp.presenter.intro.IntroDialogPresenter
import com.scp.scpexam.mvp.view.intro.IntroDialogView
import com.scp.scpexam.ui.BaseFragment
import com.scp.scpexam.ui.dialog.CC3LicenseDialogFragment
import com.scp.scpexam.ui.utils.AuthDelegate
import com.scp.scpexam.ui.utils.ChatDelegate
import com.scp.scpexam.utils.BitmapUtils
import toothpick.Toothpick
import toothpick.config.Module
import javax.inject.Inject


class IntroDialogFragment : BaseFragment<IntroDialogView, IntroDialogPresenter>(), IntroDialogView {

    @Inject
    lateinit var myPreferenceManager: MyPreferenceManager

    private lateinit var chatDelegate: ChatDelegate

    private lateinit var authDelegate: AuthDelegate<IntroDialogFragment>

    override val translucent = true

    override val scopes: Array<String> = arrayOf()

    override val modules: Array<Module> = arrayOf()

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
        val bitmap = BitmapUtils.fileToBitmap(
                "${activity?.cacheDir}/${Constants.INTRO_DIALOG_BACKGROUND_FILE_NAME}.png"
        )

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

        if (!myPreferenceManager.isPersonalDataAccepted()) {
            val dialogFragment = CC3LicenseDialogFragment.newInstance()
            dialogFragment.show(fragmentManager!!, CC3LicenseDialogFragment.TAG)
        }
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

    companion object {
        fun newInstance() = IntroDialogFragment()
    }
}