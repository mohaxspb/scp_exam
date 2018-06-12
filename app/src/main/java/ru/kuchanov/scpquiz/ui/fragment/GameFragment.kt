package ru.kuchanov.scpquiz.ui.fragment

import android.animation.ObjectAnimator
import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewTreeObserver
import android.widget.TextView
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.arellomobile.mvp.presenter.ProvidePresenterTag
import com.google.android.flexbox.FlexboxLayout
import com.hannesdorfmann.adapterdelegates3.ListDelegationAdapter
import kotlinx.android.synthetic.main.fragment_game.*
import ru.kuchanov.scpquiz.R
import ru.kuchanov.scpquiz.controller.adapter.MyListItem
import ru.kuchanov.scpquiz.di.Di
import ru.kuchanov.scpquiz.di.module.GameModule
import ru.kuchanov.scpquiz.model.db.Quiz
import ru.kuchanov.scpquiz.model.db.QuizTranslation
import ru.kuchanov.scpquiz.model.db.User
import ru.kuchanov.scpquiz.model.ui.ChatAction
import ru.kuchanov.scpquiz.mvp.presenter.GamePresenter
import ru.kuchanov.scpquiz.mvp.view.GameView
import ru.kuchanov.scpquiz.ui.BaseFragment
import ru.kuchanov.scpquiz.ui.utils.GlideApp
import ru.kuchanov.scpquiz.ui.view.ChatMessageView
import ru.kuchanov.scpquiz.ui.view.KeyboardView
import timber.log.Timber
import toothpick.Toothpick
import toothpick.config.Module
import android.animation.ValueAnimator


class GameFragment : BaseFragment<GameView, GamePresenter>(), GameView {

    companion object {
        const val ARG_QUIZ_ID = "ARG_QUIZ_ID"
        const val NO_NEXT_QUIZ_ID = -1L

        fun newInstance(quizId: Long): GameFragment {
            val fragment = GameFragment()
            val args = Bundle()
            args.putLong(ARG_QUIZ_ID, quizId)
            fragment.arguments = args
            return fragment
        }
    }

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        keyboardView.keyPressListener = { char, charView ->
            val inputFlexBox = if (presenter.isScpNameCompleted) scpNumberFlexBoxLayout else scpNameFlexBoxLayout
            addCharToFlexBox(char, inputFlexBox)
            presenter.onCharClicked(char)
            keyboardView.removeCharView(charView)
        }

        coinsButton.setOnClickListener { presenter.onCoinsClicked() }

        hamburgerButton.setOnClickListener { presenter.onHamburgerMenuClicked() }

        levelNumberTextView.setOnClickListener { presenter.onLevelsClicked() }
    }

    override fun showLevel(quiz: Quiz, randomTranslations: List<QuizTranslation>) {
        Timber.d("showLevel!")
        //todo show level number
        GlideApp
                .with(imageView.context)
                .load(quiz.imageUrl)
                .fitCenter()
                .into(imageView)

        var chars = quiz.quizTranslations?.let {
            it[0].translation
                    .toCharArray()
                    .toMutableList()
        }
                ?: throw IllegalStateException("translations is null")
        Timber.d("chars.size: ${chars.size}")
        val availableChars = randomTranslations
                .joinToString(separator = "") { it.translation }
                .toCharArray()
                .toList()
        chars = KeyboardView.fillCharsList(chars, availableChars).apply { shuffle() }
        keyboardView.setCharacters(chars)

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

    private fun addCharToFlexBox(char: Char, flexBoxContainer: FlexboxLayout) {
        val characterView = TextView(flexBoxContainer.context)
        characterView.text = char.toString()

        characterView.setAllCaps(true)
        characterView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)

        characterView.setOnClickListener {
            presenter.onCharRemoved(char, flexBoxContainer.indexOfChild(it))
            flexBoxContainer.removeView(it)
            keyboardView.addCharView((it as TextView).text[0])
        }

        flexBoxContainer.addView(characterView)

        //todo create logic for words wrapping
//        val params = characterView.layoutParams as FlexboxLayout.LayoutParams
//        params.isWrapBefore =
//        characterView.layoutParams = params
    }

    override fun showChatActions(chatActions: List<ChatAction>, containerId: Int) {
        Timber.d("chatActions: ${chatActions.joinToString()}")

        val chatActionsFlexBoxLayout = LayoutInflater.from(activity!!)
                .inflate(R.layout.view_chat_actions, chatView, false) as FlexboxLayout
        chatView.addView(chatActionsFlexBoxLayout)

        chatActions.forEach { chatAction ->
            //todo correct design
            val chatActionView = TextView(chatActionsFlexBoxLayout.context)
            chatActionsFlexBoxLayout.addView(chatActionView)
            chatActionView.text = chatAction.actionName
            chatActionView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
            chatActionView.setBackgroundColor(Color.YELLOW)
            chatActionView.setPadding(20, 20, 20, 20)
            chatActionView.setOnClickListener { chatAction.action.invoke(chatView.indexOfChild(chatActionsFlexBoxLayout)) }
        }
    }

    override fun removeChatAction(indexInParent: Int) {
        chatView.removeViewAt(indexInParent)
    }

    override fun showKeyboard(show: Boolean) {
        keyboardScrollView.visibility = if (show) VISIBLE else GONE
    }

    override fun setKeyboardChars(characters: List<Char>) {
        keyboardView.setCharacters(characters)
    }

    override fun showChatMessage(message: String, user: User) {
        val chatMessageView = ChatMessageView(
            context = activity!!,
            user = user,
            message = message
        )

        chatView.addView(chatMessageView)

        chatMessageView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                val width = chatMessageView.width
                val height = chatMessageView.height
                if (width > 0 && height > 0) {
                    chatMessageView.viewTreeObserver.removeOnGlobalLayoutListener(this)

                    ObjectAnimator
                            .ofInt(gameScrollView, "scrollY", chatMessageView.top)
                            .setDuration(500)
                            .start()
                }
            }
        })
    }

    override fun showLevelCompleted() {
        Timber.d("showLevelCompleted")
        //todo
    }

    override fun showError(error: Throwable) = Snackbar.make(
        root,
        error.message ?: getString(R.string.error_unknown),
        Snackbar.LENGTH_LONG
    ).show()

    override fun showProgress(show: Boolean) {
        progressView.visibility = if (show) View.VISIBLE else View.GONE
    }
}