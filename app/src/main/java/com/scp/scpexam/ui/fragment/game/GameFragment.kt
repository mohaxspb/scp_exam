package com.scp.scpexam.ui.fragment.game

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.google.android.flexbox.FlexboxLayout
import com.google.android.material.snackbar.Snackbar
import com.hannesdorfmann.adapterdelegates3.ListDelegationAdapter
import com.scp.scpexam.R
import com.scp.scpexam.controller.adapter.MyListItem
import com.scp.scpexam.controller.manager.preference.MyPreferenceManager
import com.scp.scpexam.databinding.FragmentGameBinding
import com.scp.scpexam.di.Di
import com.scp.scpexam.di.module.GameModule
import com.scp.scpexam.model.db.Quiz
import com.scp.scpexam.model.db.User
import com.scp.scpexam.model.ui.ChatAction
import com.scp.scpexam.model.ui.ChatActionsGroupType
import com.scp.scpexam.mvp.presenter.game.GamePresenter
import com.scp.scpexam.mvp.view.game.GameView
import com.scp.scpexam.ui.BaseActivity
import com.scp.scpexam.ui.BaseFragment
import com.scp.scpexam.ui.utils.AuthDelegate
import com.scp.scpexam.ui.utils.ChatDelegate
import com.scp.scpexam.ui.utils.GlideApp
import com.scp.scpexam.ui.utils.getImageUrl
import com.scp.scpexam.ui.view.CharacterView
import com.scp.scpexam.utils.BitmapUtils
import com.scp.scpexam.utils.StorageUtils
import com.scp.scpexam.utils.SystemUtils
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import moxy.presenter.ProvidePresenterTag
import ru.kuchanov.rate.PreRate
import timber.log.Timber
import toothpick.Toothpick
import toothpick.config.Module
import javax.inject.Inject


class GameFragment : BaseFragment<GameView, GamePresenter, FragmentGameBinding>(), GameView {

    companion object {
        const val ARG_QUIZ_ID = "ARG_QUIZ_ID"

        const val TEXT_SIZE_NAME = 20f
        const val TEXT_SIZE_NUMBER = 16f

        fun newInstance(quizId: Long): GameFragment {
            Timber.d("newInstance: $quizId")
            val fragment = GameFragment()
            val args = Bundle()
            args.putLong(ARG_QUIZ_ID, quizId)
            fragment.arguments = args
            return fragment
        }
    }

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentGameBinding
        get() = FragmentGameBinding::inflate

    @Inject
    lateinit var myPreferenceManager: MyPreferenceManager

    override val translucent = false

    override val scopes: Array<String> = arrayOf(Di.Scope.GAME_FRAGMENT)

    override val modules: Array<Module> = arrayOf(GameModule())

    @InjectPresenter
    override lateinit var presenter: GamePresenter

    @ProvidePresenter
    override fun providePresenter(): GamePresenter {
        Timber.d("providePresenter: ${arguments?.getLong(ARG_QUIZ_ID)}")
        val presenter = scope.getInstance(GamePresenter::class.java)
        presenter.quizId = arguments?.getLong(ARG_QUIZ_ID)
            ?: throw IllegalStateException("cant create presenter without quizId in fragment args!")
        return presenter
    }

    @ProvidePresenterTag(presenterClass = GamePresenter::class)
    fun provideRepositoryPresenterTag(): String = arguments?.getLong(ARG_QUIZ_ID)?.toString()
        ?: throw IllegalStateException("cant create presenter without quizId in fragment args!")

    override fun inject() = Toothpick.inject(this, scope)

    override fun getLayoutResId() = R.layout.fragment_game

    lateinit var adapter: ListDelegationAdapter<List<MyListItem>>

    private lateinit var chatDelegate: ChatDelegate

    private lateinit var authDelegate: AuthDelegate<GameFragment>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Timber.d("onViewCreated")
        super.onViewCreated(view, savedInstanceState)

        chatDelegate = ChatDelegate(
            binding.chatMessagesView,
            binding.gameScrollView,
            myPreferenceManager
        )

        authDelegate = AuthDelegate(
            this,
            presenter,
            presenter.apiClient,
            presenter.preferences
        )
        presenter.authDelegate = authDelegate
        activity?.let { authDelegate.onViewCreated(it) }

        binding.keyboardView.keyPressListener =
            { char, charId -> presenter.onCharClicked(char, charId) }

        binding.coinsButton.setOnClickListener { presenter.onCoinsClicked() }

        binding.hamburgerButton.setOnClickListener { presenter.onHamburgerMenuClicked() }

        binding.helpButton.setOnClickListener { presenter.onHelpClicked() }

        binding.levelNumberTextView.setOnClickListener { presenter.onLevelsClicked() }

        binding.imageView.setOnClickListener {
            Timber.d("scpNameAndNumber: ${presenter.quizLevelInfo.quiz.scpNumber}/${presenter.quizLevelInfo.quiz.quizTranslations?.first()?.translation}")
        }

        binding.backspaceButton.setOnClickListener {
            val deleteNumberChar = deleteNumberChar@{
                if (binding.scpNumberFlexBoxLayout.childCount == 0) return@deleteNumberChar

                val indexOfChild = binding.scpNumberFlexBoxLayout.childCount - 1
                val charView =
                    binding.scpNumberFlexBoxLayout.getChildAt(indexOfChild) as CharacterView
                presenter.onCharRemovedFromNumber(charView.charId, indexOfChild)
            }

            val deleteNameChar = deleteNameChar@{
                if (binding.scpNameFlexBoxLayout.childCount == 0) return@deleteNameChar

                val indexOfChild = binding.scpNameFlexBoxLayout.childCount - 1
                val charView =
                    binding.scpNameFlexBoxLayout.getChildAt(indexOfChild) as CharacterView
                presenter.onCharRemovedFromName(charView.charId, indexOfChild)
            }

            Timber.d("presenter.currentEnterType: ${presenter.currentEnterType}")
            if (presenter.currentEnterType == GamePresenter.EnterType.NAME) {
                deleteNameChar.invoke()
            } else if (presenter.currentEnterType == GamePresenter.EnterType.NUMBER) {
                deleteNumberChar.invoke()
            }
        }

        //ads
        if (myPreferenceManager.isAdsDisabled()) {
            binding.adView.isEnabled = false
            binding.adView.visibility = GONE
        } else {
            binding.adView.visibility = VISIBLE
            binding.adView.isEnabled = true
//            adView.adUnitId = getString(R.string.ad_unit_id_banner)
//            adView.loadAd()
        }
    }

    override fun addCharToNameInput(char: Char, charId: Int) {
        Timber.d("addCharToNameInput: $char, $charId")
        val inputFlexBox = binding.scpNameFlexBoxLayout
        addCharToFlexBox(
            char,
            charId,
            inputFlexBox,
            TEXT_SIZE_NAME
        ) {
            presenter.quizLevelInfo.finishedLevel.scpNameFilled
        }
        binding.keyboardView.removeCharView(charId)
    }

    override fun addCharToNumberInput(char: Char, charId: Int) {
        Timber.d("addCharToNumberInput: $char, $charId")
        val inputFlexBox = binding.scpNumberFlexBoxLayout
        addCharToFlexBox(
            char,
            charId,
            inputFlexBox,
            TEXT_SIZE_NUMBER
        ) {
            presenter.quizLevelInfo.finishedLevel.scpNumberFilled
        }
        binding.keyboardView.removeCharView(charId)
    }

    override fun showLevelNumber(levelNumber: Int) {
        binding.levelNumberTextView.text = getString(R.string.level, levelNumber)
    }

    override fun showImage(quiz: Quiz) {
        with(GlideApp.with(binding.imageView.context)) {
            if (StorageUtils.ifFileExistsInAssets(
                    quiz.getImageUrl(),
                    binding.imageView.context,
                    "quizImages"
                )
            ) {
                load(Uri.parse("file:///android_asset/quizImages/${quiz.getImageUrl()}"))
            } else {
                load(quiz.imageUrl)
            }
        }
            .fitCenter()
            .into(binding.imageView)
    }

    override fun animateKeyboard() {
        binding.keyboardScrollView?.postDelayed(
            {
                binding.keyboardScrollView?.apply {
                    ObjectAnimator
                        .ofInt(this, "scrollX", this.right)
                        .setDuration(500)
                        .start()
                    val animBack = ObjectAnimator
                        .ofInt(this, "scrollX", 0)
                        .setDuration(500)

                    animBack.startDelay = 500
                    animBack.start()
                }
            },
            100
        )
    }

    override fun setBackgroundDark(showDark: Boolean) = binding.root.setBackgroundResource(
        if (showDark) R.color.backgroundColorLevelCompleted
        else R.color.backgroundColor
    )

    override fun showToolbar(show: Boolean) {
        val visibility = if (show) VISIBLE else INVISIBLE
        binding.hamburgerButton.visibility = visibility
        binding.levelNumberTextView.visibility = visibility
        binding.coinsButton.visibility = visibility
    }

    override fun showHelpButton(show: Boolean) {
        binding.helpButton.visibility = if (show) VISIBLE else GONE
    }

    override fun showCoins(coins: Int) {
        val animator =
            ValueAnimator.ofInt(binding.coinsValueTextView.text.toString().toInt(), coins)
        animator.duration = 1000
        animator.addUpdateListener { animation ->
            binding.coinsValueTextView?.text = animation.animatedValue.toString()
        }
        animator.start()
    }

    override fun showNumber(number: List<Char>) = with(binding.scpNumberFlexBoxLayout) {
        removeAllViews()
        number.forEach {
            addCharToFlexBox(
                char = it,
                flexBoxContainer = this,
                textSize = TEXT_SIZE_NUMBER
            ) { presenter.quizLevelInfo.finishedLevel.scpNumberFilled }
        }
    }

    override fun showName(name: List<Char>) = with(binding.scpNameFlexBoxLayout) {
        removeAllViews()
        name.forEach { char ->
            addCharToFlexBox(
                char,
                flexBoxContainer = this
            ) { presenter.quizLevelInfo.finishedLevel.scpNameFilled }
        }
    }

    override fun onResume() {
        super.onResume()
        presenter.checkLang()
    }

    private fun addCharToFlexBox(
        char: Char,
        charId: Int = NO_ID,
        flexBoxContainer: FlexboxLayout,
        textSize: Float = TEXT_SIZE_NAME,
        shouldIgnoreClick: () -> Boolean
    ) {
        val characterView = CharacterView(flexBoxContainer.context)
        characterView.isSquare = false
        characterView.text = char.toString()
        characterView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize)
        characterView.background =
            ContextCompat.getDrawable(flexBoxContainer.context, android.R.color.transparent)
        characterView.charId = charId

        characterView.setOnClickListener {
            if (!shouldIgnoreClick.invoke()) {
                if (myPreferenceManager.isVibrationEnabled()) {
                    SystemUtils.vibrate()
                }
                Timber.d(
                    """
                    char: $char,
                    flexBoxContainer.indexOfChild(it): ${flexBoxContainer.indexOfChild(it)}
                    flexBoxContainer.childCount: ${flexBoxContainer.childCount}
                """.trimIndent()
                )
                if (flexBoxContainer == binding.scpNameFlexBoxLayout) {
                    presenter.onCharRemovedFromName(charId, flexBoxContainer.indexOfChild(it))
                } else {
                    presenter.onCharRemovedFromNumber(charId, flexBoxContainer.indexOfChild(it))
                }
            }
        }

        flexBoxContainer.addView(characterView)

        //todo create logic for words wrapping
//        val params = characterView.layoutParams as FlexboxLayout.LayoutParams
//        params.isWrapBefore = //here we can calculate width and spaces
//        characterView.layoutParams = params
    }

    override fun removeCharFromNameInput(charId: Int, indexOfChild: Int) {
        Timber.d("removeCharFromNameInput: $charId, $indexOfChild")
        val inputFlexBox = binding.scpNameFlexBoxLayout
        Timber.d("inputFlexBox: ${inputFlexBox == null}")
        Timber.d("inputFlexBox childs: ${inputFlexBox.childCount}")
        Timber.d("inputFlexBox childs: ${inputFlexBox.childCount}")
        if (inputFlexBox.getChildAt(indexOfChild) != null) {
            inputFlexBox.removeViewAt(indexOfChild)
            binding.keyboardView.restoreChar(charId)
        }
    }

    override fun removeCharFromNumberInput(charId: Int, indexOfChild: Int) {
        Timber.d("removeCharFromNameInput: $charId, $indexOfChild")
        val inputFlexBox = binding.scpNumberFlexBoxLayout
        Timber.d("inputFlexBox: ${inputFlexBox == null}")
        Timber.d("inputFlexBox childs: ${inputFlexBox.childCount}")
        Timber.d("inputFlexBox childs: ${inputFlexBox.childCount}")
        if (inputFlexBox.getChildAt(indexOfChild) != null) {
            inputFlexBox.removeViewAt(indexOfChild)
            binding.keyboardView.restoreChar(charId)
        }
    }

    override fun showChatActions(
        chatActions: List<ChatAction>,
        chatActionsGroupType: ChatActionsGroupType
    ) {
        if (!isAdded) {
            return
        }
        chatDelegate.showChatActions(chatActions, chatActionsGroupType)
    }

    override fun removeChatAction(indexInParent: Int) {
        if (!isAdded) {
            return
        }
        chatDelegate.removeChatAction(indexInParent)
    }

    override fun showKeyboard(show: Boolean) {
        binding.keyboardScrollView.visibility = if (show) VISIBLE else GONE
    }

    override fun setKeyboardChars(characters: List<Char>) {
        binding.keyboardView?.setCharacters(characters)
    }

    override fun showChatMessage(message: String, user: User) {
        if (!isAdded) {
            return
        }
        chatDelegate.showChatMessage(
            message,
            user,
            R.color.textColorGrey
        )
    }

    override fun askForRateApp() = PreRate.init(
        activity,
        getString(R.string.feedback_email),
        getString(R.string.feedback_title)
    ).showRateDialog()

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        presenter.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onPause() {
        super.onPause()
        authDelegate.onPause()
    }

    override fun clearChatMessages() = binding.chatMessagesView.removeAllViews()

    override fun onNeedToOpenSettings() {
        BitmapUtils.loadBitmapFromView(binding.root)?.let { presenter.openSettings(it) }
    }

    override fun onNeedToOpenCoins() {
        BitmapUtils.loadBitmapFromView(binding.root)?.let { presenter.openCoins(it) }
    }

    override fun showBackspaceButton(show: Boolean) {
        Timber.d("showBackspaceButton: $show")
        if (show) {
            binding.backspaceButton.show()
        } else {
            binding.backspaceButton.hide()
        }
    }

    override fun showError(error: Throwable) = Snackbar.make(
        binding.root,
        error.message ?: getString(R.string.error_unknown),
        Snackbar.LENGTH_LONG
    ).show()

    override fun showProgress(show: Boolean) {
        binding.progressView.visibility = if (show) VISIBLE else GONE
    }

    override fun onNeedToShowRewardedVideo() = (activity as BaseActivity<*, *>).showRewardedVideo()

    override fun onNeedToBuyCoins(skuId: String) = (activity as BaseActivity<*, *>).buyCoins(skuId)

}
