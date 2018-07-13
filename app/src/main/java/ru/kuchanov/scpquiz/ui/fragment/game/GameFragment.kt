package ru.kuchanov.scpquiz.ui.fragment.game

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.widget.TextView
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
import ru.kuchanov.scpquiz.ui.utils.ChatDelegate
import ru.kuchanov.scpquiz.ui.utils.GlideApp
import ru.kuchanov.scpquiz.ui.utils.getImageUrl
import ru.kuchanov.scpquiz.utils.AdsUtils
import ru.kuchanov.scpquiz.utils.BitmapUtils
import ru.kuchanov.scpquiz.utils.SystemUtils
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
        super.onViewCreated(view, savedInstanceState)

        chatDelegate = ChatDelegate(
            chatMessagesView,
            gameScrollView,
            myPreferenceManager
        )

        keyboardView.keyPressListener = { char, charView ->
            val isScpNameCompleted = presenter.quizLevelInfo.finishedLevel.scpNameFilled
            val inputFlexBox = if (isScpNameCompleted) scpNumberFlexBoxLayout else scpNameFlexBoxLayout
            addCharToFlexBox(char, inputFlexBox, if (isScpNameCompleted) TEXT_SIZE_NUMBER else TEXT_SIZE_NAME) {
                if (isScpNameCompleted) {
                    presenter.quizLevelInfo.finishedLevel.scpNumberFilled
                } else {
                    presenter.quizLevelInfo.finishedLevel.scpNameFilled
                }
            }
            presenter.onCharClicked(char)
            keyboardView.removeCharView(charView)
        }

        coinsButton.setOnClickListener { presenter.onCoinsClicked() }

        hamburgerButton.setOnClickListener { presenter.onHamburgerMenuClicked() }

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

    override fun showLevelNumber(levelNumber: Int) {
        levelNumberTextView.text = getString(R.string.level, levelNumber)
    }

    override fun showImage(quiz: Quiz) {
        GlideApp
                .with(imageView.context)
                .load(Uri.parse("file:///android_asset/quizImages/${quiz.getImageUrl()}"))
                .fitCenter()
                .into(imageView)
    }

    override fun animateKeyboard() {
        keyboardScrollView.postDelayed({
            ObjectAnimator
                    .ofInt(keyboardScrollView, "scrollX", keyboardScrollView.right)
                    .setDuration(500)
                    .start()
            val animBack = ObjectAnimator
                    .ofInt(keyboardScrollView, "scrollX", 0)
                    .setDuration(500)

            animBack.startDelay = 500
            animBack.start()
        }, 100)
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
            addCharToFlexBox(it, this, TEXT_SIZE_NUMBER) { presenter.quizLevelInfo.finishedLevel.scpNumberFilled }
        }
    }

    override fun showName(name: List<Char>) = with(scpNameFlexBoxLayout) {
        removeAllViews()
        name.forEach { char ->
            addCharToFlexBox(char, this) { presenter.quizLevelInfo.finishedLevel.scpNameFilled }
        }
    }

    override fun onResume() {
        super.onResume()
        presenter.checkLang()
    }

    private fun addCharToFlexBox(
        char: Char,
        flexBoxContainer: FlexboxLayout,
        textSize: Float = TEXT_SIZE_NAME,
        shouldIgnoreClick: () -> Boolean
    ) {
        val characterView = LayoutInflater
                .from(flexBoxContainer.context)
                .inflate(R.layout.view_entered_char, flexBoxContainer, false) as TextView
        characterView.text = char.toString()
        characterView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize)

        characterView.setOnClickListener {
            if (!shouldIgnoreClick.invoke()) {
                if (myPreferenceManager.isVibrationEnabled()) {
                    SystemUtils.vibrate()
                }
                presenter.onCharRemoved(char, flexBoxContainer.indexOfChild(it))
                flexBoxContainer.removeView(it)
                keyboardView.addCharView((it as TextView).text[0])
            }
        }

        flexBoxContainer.addView(characterView)

        //todo create logic for words wrapping
//        val params = characterView.layoutParams as FlexboxLayout.LayoutParams
//        params.isWrapBefore = //here we can calculate width and spaces
//        characterView.layoutParams = params
    }

    override fun showChatActions(chatActions: List<ChatAction>) = chatDelegate.showChatActions(chatActions)

    override fun removeChatAction(indexInParent: Int) = chatDelegate.removeChatAction(indexInParent)

    override fun showKeyboard(show: Boolean) {
        keyboardScrollView.visibility = if (show) VISIBLE else GONE
    }

    override fun setKeyboardChars(characters: List<Char>) {
        keyboardView?.postDelayed({ keyboardView?.setCharacters(characters) }, 100)
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
