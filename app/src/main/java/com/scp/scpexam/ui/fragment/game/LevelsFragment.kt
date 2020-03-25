package com.scp.scpexam.ui.fragment.game

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import com.hannesdorfmann.adapterdelegates3.AdapterDelegatesManager
import com.hannesdorfmann.adapterdelegates3.ListDelegationAdapter
import com.scp.scpexam.Constants
import kotlinx.android.synthetic.main.fragment_levels.*
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import com.scp.scpexam.R
import com.scp.scpexam.controller.adapter.MyListItem
import com.scp.scpexam.controller.adapter.delegate.LevelDelegate
import com.scp.scpexam.controller.adapter.viewmodel.LevelViewModel
import com.scp.scpexam.controller.manager.preference.MyPreferenceManager
import com.scp.scpexam.mvp.presenter.game.LevelsPresenter
import com.scp.scpexam.mvp.view.activity.MainView
import com.scp.scpexam.mvp.view.game.LevelsView
import com.scp.scpexam.ui.BaseFragment
import com.scp.scpexam.ui.dialog.CC3LicenseDialogFragment
import com.scp.scpexam.utils.BitmapUtils
import kotlinx.android.synthetic.main.fragment_leaderboard.*
import kotlinx.android.synthetic.main.fragment_levels.progressView
import kotlinx.android.synthetic.main.fragment_levels.root
import kotlinx.android.synthetic.main.fragment_levels.swipeRefresher
import toothpick.Toothpick
import toothpick.config.Module
import javax.inject.Inject


class LevelsFragment : BaseFragment<LevelsView, LevelsPresenter>(), LevelsView {

    override val translucent = false

    override val scopes: Array<String> = arrayOf()

    override val modules: Array<Module> = arrayOf()

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

        leaderboardButton.setOnClickListener { presenter.onLeaderboardButtonClicked() }

        coinsButton.setOnClickListener { presenter.onCoinsClicked() }

        hamburgerButton.setOnClickListener { presenter.onHamburgerMenuClicked() }

        getCoinsButton.setOnClickListener {
            if (preferenceManager.isRewardedVideoDescriptionShown()) {
                getBaseActivity().showRewardedVideo()
            } else {
                (getBaseActivity() as MainView).showFirstTimeAppodealAdsDialog()
            }
        }

        levelsTextView.setOnClickListener { presenter.onLevelsClick() }

        swipeRefresher.setOnRefreshListener { presenter.getAllQuizzes() }

        if (!preferenceManager.isPersonalDataAccepted()) {
            val dialogFragment = CC3LicenseDialogFragment.newInstance()
            dialogFragment.show(fragmentManager!!, CC3LicenseDialogFragment.TAG)
        }
    }

    private fun initRecyclerView() {
        recyclerView.layoutManager = androidx.recyclerview.widget.GridLayoutManager(activity, 3)
        val delegateManager = AdapterDelegatesManager<List<MyListItem>>()
        delegateManager.addDelegate(LevelDelegate(
                { levelViewModel -> presenter.onLevelClick(levelViewModel) },
                { levelViewModel, itemPosition -> presenter.onLevelUnlockClicked(levelViewModel, itemPosition) }
        ))
        adapter = ListDelegationAdapter(delegateManager)
        recyclerView.adapter = adapter
    }

    override fun showProgress(show: Boolean) {
        progressView.visibility = if (show) VISIBLE else GONE
    }

    override fun showProgressOnQuizLevel(itemPosition: Int) {
        recyclerView.adapter?.notifyItemChanged(itemPosition)
    }

    override fun showSwipeProgressBar(showSwipeProgressBar: Boolean) {
        swipeRefresher.isRefreshing = showSwipeProgressBar
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

    companion object {
        fun newInstance() = LevelsFragment()
    }
}