package com.scp.scpexam.ui.fragment.game

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import com.hannesdorfmann.adapterdelegates3.AdapterDelegatesManager
import com.hannesdorfmann.adapterdelegates3.ListDelegationAdapter
import com.scp.scpexam.R
import com.scp.scpexam.controller.adapter.MyListItem
import com.scp.scpexam.controller.adapter.delegate.LevelDelegate
import com.scp.scpexam.controller.adapter.viewmodel.LevelViewModel
import com.scp.scpexam.controller.manager.preference.MyPreferenceManager
import com.scp.scpexam.databinding.FragmentLevelsBinding
import com.scp.scpexam.mvp.presenter.game.LevelsPresenter
import com.scp.scpexam.mvp.view.activity.MainView
import com.scp.scpexam.mvp.view.game.LevelsView
import com.scp.scpexam.ui.BaseFragment
import com.scp.scpexam.ui.dialog.CC3LicenseDialogFragment
import com.scp.scpexam.utils.BitmapUtils
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import toothpick.Toothpick
import toothpick.config.Module
import javax.inject.Inject


class LevelsFragment : BaseFragment<LevelsView, LevelsPresenter, FragmentLevelsBinding>(),
    LevelsView {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentLevelsBinding
        get() = FragmentLevelsBinding::inflate

    override val translucent = false

    override val scopes: Array<String> = arrayOf()

    override val modules: Array<Module> = arrayOf()

    @InjectPresenter
    override lateinit var presenter: LevelsPresenter

    @ProvidePresenter
    override fun providePresenter(): LevelsPresenter =
        scope.getInstance(LevelsPresenter::class.java)

    @Inject
    lateinit var preferenceManager: MyPreferenceManager

    override fun inject() = Toothpick.inject(this, scope)

    override fun getLayoutResId() = R.layout.fragment_levels

    lateinit var adapter: ListDelegationAdapter<List<MyListItem>>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initRecyclerView()

        binding.leaderboardButton.setOnClickListener { presenter.onLeaderboardButtonClicked() }

        binding.coinsButton.setOnClickListener { presenter.onCoinsClicked() }

        binding.hamburgerButton.setOnClickListener { presenter.onHamburgerMenuClicked() }

        binding.getCoinsButton.setOnClickListener {
            if (preferenceManager.isRewardedVideoDescriptionShown()) {
                getBaseActivity().showRewardedVideo()
            } else {
                (getBaseActivity() as MainView).showFirstTimeAppodealAdsDialog()
            }
        }

        binding.levelsTextView.setOnClickListener { presenter.onLevelsClick() }

        binding.swipeRefresher.setOnRefreshListener { presenter.getAllQuizzes() }

        if (!preferenceManager.isPersonalDataAccepted()) {
            val dialogFragment = CC3LicenseDialogFragment.newInstance()
            dialogFragment.show(requireFragmentManager(), CC3LicenseDialogFragment.TAG)
        }
    }

    private fun initRecyclerView() {
        binding.recyclerView.layoutManager =
            androidx.recyclerview.widget.GridLayoutManager(activity, 3)
        val delegateManager = AdapterDelegatesManager<List<MyListItem>>()
        delegateManager.addDelegate(LevelDelegate(
            { levelViewModel -> presenter.onLevelClick(levelViewModel) },
            { levelViewModel, itemPosition ->
                presenter.onLevelUnlockClicked(
                    levelViewModel,
                    itemPosition
                )
            }
        ))
        adapter = ListDelegationAdapter(delegateManager)
        binding.recyclerView.adapter = adapter
    }

    override fun showProgress(show: Boolean) {
        binding.progressView.visibility = if (show) VISIBLE else GONE
    }

    override fun showProgressOnQuizLevel(itemPosition: Int) {
        binding.recyclerView.adapter?.notifyItemChanged(itemPosition)
    }

    override fun showSwipeProgressBar(showSwipeProgressBar: Boolean) {
        binding.swipeRefresher.isRefreshing = showSwipeProgressBar
    }

    override fun showLevels(quizes: List<LevelViewModel>) {
        adapter.items = quizes
        adapter.notifyDataSetChanged()
    }

    override fun showAllLevelsFinishedPanel(show: Boolean) {
        binding.nextLevelsTextView.visibility = if (show) VISIBLE else GONE
    }

    override fun onNeedToOpenSettings() {
        BitmapUtils.loadBitmapFromView(binding.root)?.let { presenter.openSettings(it) }
    }

    override fun onNeedToOpenCoins() {
        BitmapUtils.loadBitmapFromView(binding.root)?.let { presenter.openCoins(it) }
    }

    override fun showCoins(coins: Int) {
        val animator =
            ValueAnimator.ofInt(binding.coinsValueTextView.text.toString().toInt(), coins)
        animator.duration = 1000
        animator.addUpdateListener { animation ->
            if (isAdded) {
                binding.coinsValueTextView.text = animation.animatedValue.toString()
            }
        }
        animator.start()
    }

    companion object {
        fun newInstance() = LevelsFragment()
    }
}