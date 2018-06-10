package ru.kuchanov.scpquiz.ui.fragment

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
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
import ru.kuchanov.scpquiz.mvp.presenter.GamePresenter
import ru.kuchanov.scpquiz.mvp.view.GameView
import ru.kuchanov.scpquiz.ui.BaseFragment
import ru.kuchanov.scpquiz.ui.utils.GlideApp
import ru.kuchanov.scpquiz.ui.view.CharacterView
import ru.kuchanov.scpquiz.ui.view.KeyboardView
import timber.log.Timber
import toothpick.Toothpick
import toothpick.config.Module

class GameFragment : BaseFragment<GameView, GamePresenter>(), GameView {

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
            presenter.onCharClicked(char)
            keyboardView.removeCharView(charView)
            addCharToFlexBox(char, scpNameFlexBoxLayout)
//            scpNameTextView.text = presenter.enteredName.joinToString("")
        }

        coinsButton.setOnClickListener { presenter.onCoinsClicked() }

        hamburgerButton.setOnClickListener { presenter.onHamburgerMenuClicked() }

        levelNumberTextView.setOnClickListener { presenter.onLevelsClicked() }
    }

    override fun showLevel(quiz: Quiz, randomTranslations: List<QuizTranslation>) {
        //todo show level number
        GlideApp
                .with(imageView.context)
                .load(quiz.imageUrl)
                .fitCenter()
                .into(imageView)

        var chars = quiz.quizTranslations?.let {
            it[0].translation.replace(" ", "").toCharArray().toMutableList()
        }
                ?: throw IllegalStateException("translations is null")
        Timber.d("chars.size: ${chars.size}")
        val availableChars = randomTranslations
                .joinToString(separator = "") { it.translation }
                .replace(" ", "")
                .toCharArray()
                .toList()
        chars = fillCharsList(chars, availableChars).apply { shuffle() }
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

    private fun addCharToFlexBox(char: Char, flexBoxContainer: FlexboxLayout) {
        val characterView = CharacterView(context!!)
        characterView.squareByHeight = false
        characterView.char = char

        characterView.setOnClickListener {
            presenter.onCharRemoved(char)
            flexBoxContainer.removeView(it)
            keyboardView.addCharView((it as CharacterView).char)
        }

        flexBoxContainer.addView(characterView)

        val marginParams = characterView.layoutParams as ViewGroup.MarginLayoutParams
        marginParams.marginEnd = resources.getDimensionPixelSize(R.dimen.defaultMarginSmall)
//        marginParams.bottomMargin = resources.getDimensionPixelSize(R.dimen.defaultMargin)
        marginParams.width = LinearLayout.LayoutParams.WRAP_CONTENT
        marginParams.height = LinearLayout.LayoutParams.WRAP_CONTENT

        characterView.layoutParams = marginParams
    }

    override fun showLevelCompleted() {
        //todo
    }

    private fun fillCharsList(chars: MutableList<Char>, availableChars: List<Char>): MutableList<Char> {
        if (chars.size < KeyboardView.MIN_KEY_COUNT) {
            val charsToAddCount = KeyboardView.MIN_KEY_COUNT - chars.size

            Timber.d("chars: $chars")
            Timber.d("aviableChars: $availableChars")
            val topBorder = if (availableChars.size > charsToAddCount) charsToAddCount else availableChars.size
            chars.addAll(availableChars.subList(0, topBorder))
            Timber.d("chars.size: ${chars.size}")
        }
        if (chars.size < KeyboardView.MIN_KEY_COUNT) {
            return fillCharsList(chars, availableChars)
        } else {
            return chars
        }
    }

    override fun showError(error: Throwable) {
        //todo
    }

    override fun showProgress(show: Boolean) {
        progressView.visibility = if (show) View.VISIBLE else View.GONE
    }

    companion object {
        const val ARG_QUIZ_ID = "ARG_QUIZ_ID"

        fun newInstance(quizId: Long): GameFragment {
            val fragment = GameFragment()
            val args = Bundle()
            args.putLong(ARG_QUIZ_ID, quizId)
            fragment.arguments = args
            return fragment
        }
    }
}