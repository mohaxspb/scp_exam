package ru.kuchanov.scpquiz.ui.fragment.game

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.util.TypedValue
import android.view.View
import android.view.View.*
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.arellomobile.mvp.presenter.ProvidePresenterTag
import com.google.android.flexbox.FlexboxLayout
import com.hannesdorfmann.adapterdelegates3.ListDelegationAdapter
import kotlinx.android.synthetic.main.fragment_game.*
import ru.kuchanov.rate.PreRate
import ru.kuchanov.scpquiz.R
import ru.kuchanov.scpquiz.controller.adapter.MyListItem
import ru.kuchanov.scpquiz.controller.manager.preference.MyPreferenceManager
import ru.kuchanov.scpquiz.di.Di
import ru.kuchanov.scpquiz.di.module.GameModule
import ru.kuchanov.scpquiz.model.db.Quiz
import ru.kuchanov.scpquiz.model.db.User
import ru.kuchanov.scpquiz.model.ui.ChatAction
import ru.kuchanov.scpquiz.mvp.presenter.game.GamePresenter
import ru.kuchanov.scpquiz.mvp.view.game.GameView
import ru.kuchanov.scpquiz.ui.BaseActivity
import ru.kuchanov.scpquiz.ui.BaseFragment
import ru.kuchanov.scpquiz.ui.utils.*
import ru.kuchanov.scpquiz.ui.view.CharacterView
import ru.kuchanov.scpquiz.utils.AdsUtils
import ru.kuchanov.scpquiz.utils.BitmapUtils
import ru.kuchanov.scpquiz.utils.StorageUtils
import ru.kuchanov.scpquiz.utils.SystemUtils
import timber.log.Timber
import toothpick.Toothpick
import toothpick.config.Module
import javax.inject.Inject


class GameFragment : BaseFragment<GameView, GamePresenter>(), GameView {

    companion object {
        const val ARG_QUIZ_ID = "ARG_QUIZ_ID"
        const val NO_NEXT_QUIZ_ID = -1L

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Timber.d("onViewCreated")
        super.onViewCreated(view, savedInstanceState)

        chatDelegate = ChatDelegate(
                chatMessagesView,
                gameScrollView,
                myPreferenceManager
        )

        keyboardView.keyPressListener = { char, charId -> presenter.onCharClicked(char, charId) }

        coinsButton.setOnClickListener { presenter.onCoinsClicked() }

        hamburgerButton.setOnClickListener { presenter.onHamburgerMenuClicked() }

        helpButton.setOnClickListener { presenter.onHelpClicked() }

        levelNumberTextView.setOnClickListener { presenter.onLevelsClicked() }

        //ads
        if (myPreferenceManager.isAdsDisabled()) {
            adView.isEnabled = false
            adView.pause()
            adView.visibility = GONE
        } else {
            adView.visibility = VISIBLE
            adView.isEnabled = true

            adView.loadAd(AdsUtils.buildAdRequest())
        }
    }

    override fun addCharToNameInput(char: Char, charId: Int) {
        Timber.d("addCharToNameInput: $char, $charId")
        val inputFlexBox = scpNameFlexBoxLayout
        addCharToFlexBox(
                char,
                charId,
                inputFlexBox,
                TEXT_SIZE_NAME
        ) {
            presenter.quizLevelInfo.finishedLevel.scpNameFilled
        }
        keyboardView.removeCharView(charId)
    }

    override fun addCharToNumberInput(char: Char, charId: Int) {
        Timber.d("addCharToNumberInput: $char, $charId")
        val inputFlexBox = scpNumberFlexBoxLayout
        addCharToFlexBox(
                char,
                charId,
                inputFlexBox,
                TEXT_SIZE_NUMBER
        ) {
            presenter.quizLevelInfo.finishedLevel.scpNumberFilled
        }
        keyboardView.removeCharView(charId)
    }

    override fun showLevelNumber(levelNumber: Int) {
        levelNumberTextView.text = getString(R.string.level, levelNumber)
    }

    override fun showImage(quiz: Quiz) {
        val glideRequest = GlideApp.with(imageView.context)
        if (StorageUtils.ifFileExistsInAssets(quiz.getImageUrl(), imageView.context,"quizImages")) {
            glideRequest.load(Uri.parse("file:///android_asset/quizImages/${quiz.getImageUrl()}"))
                    .fitCenter()
                    .into(imageView)
        } else {
            glideRequest.load(quiz.imageUrl)
                    .fitCenter()
                    .into(imageView)
        }
    }

    override fun animateKeyboard() {
        keyboardScrollView?.postDelayed(
                {
                    keyboardScrollView?.apply {
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

    override fun setBackgroundDark(showDark: Boolean) = root.setBackgroundResource(
            if (showDark) R.color.backgroundColorLevelCompleted
            else R.color.backgroundColor
    )

    override fun showToolbar(show: Boolean) {
        val visibility = if (show) VISIBLE else INVISIBLE
        hamburgerButton.visibility = visibility
        levelNumberTextView.visibility = visibility
        coinsButton.visibility = visibility
        helpButton.visibility = visibility
    }

    override fun showCoins(coins: Int) {
        val animator = ValueAnimator.ofInt(coinsValueTextView.text.toString().toInt(), coins)
        animator.duration = 1000
        animator.addUpdateListener { animation -> coinsValueTextView?.text = animation.animatedValue.toString() }
        animator.start()
    }

    override fun showNumber(number: List<Char>) = with(scpNumberFlexBoxLayout) {
        removeAllViews()
        number.forEach {
            addCharToFlexBox(
                    char = it,
                    flexBoxContainer = this,
                    textSize = TEXT_SIZE_NUMBER) { presenter.quizLevelInfo.finishedLevel.scpNumberFilled }
        }
    }

    override fun showName(name: List<Char>) = with(scpNameFlexBoxLayout) {
        removeAllViews()
        name.forEach { char ->
            addCharToFlexBox(char, flexBoxContainer = this) { presenter.quizLevelInfo.finishedLevel.scpNameFilled }
        }
    }

    override fun onResume() {
        super.onResume()
        presenter.checkLang()
    }

    private fun addCharToFlexBox(
            char: Char,
            charId: Int = View.NO_ID,
            flexBoxContainer: FlexboxLayout,
            textSize: Float = TEXT_SIZE_NAME,
            shouldIgnoreClick: () -> Boolean
    ) {
        val characterView = CharacterView(flexBoxContainer.context)
        characterView.isSquare = false
        characterView.text = char.toString()
        characterView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize)
        characterView.background = ContextCompat.getDrawable(flexBoxContainer.context, android.R.color.transparent)
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
                """.trimIndent())
                if (flexBoxContainer == scpNameFlexBoxLayout) {
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
        val inputFlexBox = scpNameFlexBoxLayout
        Timber.d("inputFlexBox: ${inputFlexBox == null}")
        Timber.d("inputFlexBox childs: ${inputFlexBox.childCount}")
        Timber.d("inputFlexBox childs: ${inputFlexBox.childCount}")
        if (inputFlexBox.getChildAt(indexOfChild) != null) {
            inputFlexBox.removeViewAt(indexOfChild)
            keyboardView.restoreChar(charId)
        }
    }

    override fun removeCharFromNumberInput(charId: Int, indexOfChild: Int) {
        Timber.d("removeCharFromNameInput: $charId, $indexOfChild")
        val inputFlexBox = scpNumberFlexBoxLayout
        Timber.d("inputFlexBox: ${inputFlexBox == null}")
        Timber.d("inputFlexBox childs: ${inputFlexBox.childCount}")
        Timber.d("inputFlexBox childs: ${inputFlexBox.childCount}")
        if (inputFlexBox.getChildAt(indexOfChild) != null) {
            inputFlexBox.removeViewAt(indexOfChild)
            keyboardView.restoreChar(charId)
        }
    }

    override fun showChatActions(chatActions: List<ChatAction>) = chatDelegate.showChatActions(chatActions)

    override fun removeChatAction(indexInParent: Int) = chatDelegate.removeChatAction(indexInParent)

    override fun showKeyboard(show: Boolean) {
        keyboardScrollView.visibility = if (show) VISIBLE else GONE
    }

    override fun setKeyboardChars(characters: List<Char>) {
        keyboardView?.setCharacters(characters)
    }

    override fun showChatMessage(message: String, user: User) = chatDelegate.showChatMessage(
            message,
            user,
            R.color.textColorGrey
    )

    override fun askForRateApp() = PreRate.init(
            activity,
            getString(R.string.feedback_email),
            getString(R.string.feedback_title)
    ).showRateDialog()

    override fun clearChatMessages() = chatMessagesView.removeAllViews()

    override fun onNeedToOpenSettings() = presenter.openSettings(BitmapUtils.loadBitmapFromView(root))

    override fun onNeedToOpenCoins() = presenter.openCoins(BitmapUtils.loadBitmapFromView(root))

    override fun showError(error: Throwable) = Snackbar.make(
            root,
            error.message ?: getString(R.string.error_unknown),
            Snackbar.LENGTH_LONG
    ).show()

    override fun showProgress(show: Boolean) {
        progressView.visibility = if (show) View.VISIBLE else View.GONE
    }

    override fun onNeedToShowRewardedVideo() = (activity as BaseActivity<*, *>).showRewardedVideo()
}
