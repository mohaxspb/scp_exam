package ru.kuchanov.scpquiz.ui.fragment

import android.os.Bundle
import android.view.View
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.arellomobile.mvp.presenter.ProvidePresenterTag
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

        keyboardView.keyPressListener = {
            presenter.onCharClicked(it)
        }

        coinsButton.setOnClickListener { presenter.onCoinsClicked() }

        hamburgerButton.setOnClickListener { presenter.onHamburgerMenuClicked() }

        levelNumberTextView.setOnClickListener { presenter.onLevelsClicked() }
    }

    override fun showLevel(quiz: Quiz, randomTranslations: List<QuizTranslation>) {
        //todo
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