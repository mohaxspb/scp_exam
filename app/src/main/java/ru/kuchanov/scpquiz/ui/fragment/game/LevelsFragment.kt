package ru.kuchanov.scpquiz.ui.fragment.game

import android.animation.ValueAnimator
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.hannesdorfmann.adapterdelegates3.AdapterDelegatesManager
import com.hannesdorfmann.adapterdelegates3.ListDelegationAdapter
import kotlinx.android.synthetic.main.fragment_levels.*
import ru.kuchanov.scpquiz.R
import ru.kuchanov.scpquiz.controller.adapter.MyListItem
import ru.kuchanov.scpquiz.controller.adapter.delegate.LevelDelegate
import ru.kuchanov.scpquiz.controller.adapter.viewmodel.LevelViewModel
import ru.kuchanov.scpquiz.controller.manager.preference.MyPreferenceManager
import ru.kuchanov.scpquiz.di.Di
import ru.kuchanov.scpquiz.di.module.LevelsModule
import ru.kuchanov.scpquiz.mvp.presenter.game.LevelsPresenter
import ru.kuchanov.scpquiz.mvp.view.activity.MainView
import ru.kuchanov.scpquiz.mvp.view.game.LevelsView
import ru.kuchanov.scpquiz.ui.BaseFragment
import ru.kuchanov.scpquiz.utils.BitmapUtils
import timber.log.Timber
import toothpick.Toothpick
import toothpick.config.Module
import javax.inject.Inject


class LevelsFragment : BaseFragment<LevelsView, LevelsPresenter>(), LevelsView {


    companion object {
        fun newInstance() = LevelsFragment()
    }

    override val translucent = false

    override val scopes: Array<String> = arrayOf(Di.Scope.LEVELS_FRAGMENT)

    override val modules: Array<Module> = arrayOf(LevelsModule())

    @InjectPresenter
    override lateinit var presenter: LevelsPresenter

    @ProvidePresenter
    override fun providePresenter(): LevelsPresenter = scope.getInstance(LevelsPresenter::class.java)

    @Inject
    lateinit var preferenceManager: MyPreferenceManager

    override fun inject() = Toothpick.inject(this, scope)

    override fun getLayoutResId() = R.layout.fragment_levels

    lateinit var adapter: ListDelegationAdapter<List<MyListItem>>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initRecyclerView()

        coinsButton.setOnClickListener { presenter.onCoinsClicked() }

        hamburgerButton.setOnClickListener { presenter.onHamburgerMenuClicked() }

        getCoinsButton.setOnClickListener {
            if (preferenceManager.isAppodealDescriptionShown()) {
                getBaseActivity().showRewardedVideo()
            } else {
                (getBaseActivity() as MainView).showFirstTimeAppodealAdsDialog()
            }
        }

        levelsTextView.setOnClickListener { presenter.onLevelsClick() }
    }

    private fun initRecyclerView() {
        recyclerView.layoutManager = GridLayoutManager(activity, 3)
        val delegateManager = AdapterDelegatesManager<List<MyListItem>>()
        delegateManager.addDelegate(LevelDelegate(
                { levelViewModel -> presenter.onLevelClick(levelViewModel) },
                { levelViewModel, itemPosition -> presenter.onLevelUnlockClicked(levelViewModel, itemPosition) }
        ))
        adapter = ListDelegationAdapter(delegateManager)
        recyclerView.adapter = adapter
    }

    override fun showProgress(show: Boolean) {
        progressView.visibility = if (show) View.VISIBLE else View.GONE
    }

    override fun showProgressOnQuizLevel(itemPosition: Int) {
        Timber.d("Item Position:%s", itemPosition)
        recyclerView.adapter?.notifyItemChanged(itemPosition)
    }

    override fun showLevels(quizes: List<LevelViewModel>) {
        adapter.items = quizes
        adapter.notifyDataSetChanged()
    }

    override fun showAllLevelsFinishedPanel(show: Boolean) {
        nextLevelsTextView.visibility = if (show) VISIBLE else GONE
    }

    override fun onNeedToOpenSettings() {
        BitmapUtils.loadBitmapFromView(root)?.let { presenter.openSettings(it) }
    }

    override fun onNeedToOpenCoins() {
        BitmapUtils.loadBitmapFromView(root)?.let { presenter.openCoins(it) }
    }

    override fun showCoins(coins: Int) {
        val animator = ValueAnimator.ofInt(coinsValueTextView.text.toString().toInt(), coins)
        animator.duration = 1000
        animator.addUpdateListener { animation -> coinsValueTextView?.text = animation.animatedValue.toString() }
        animator.start()
    }
}