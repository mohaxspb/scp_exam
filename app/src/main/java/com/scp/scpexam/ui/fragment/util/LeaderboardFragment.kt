package com.scp.scpexam.ui.fragment.util

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.request.RequestOptions
import com.hannesdorfmann.adapterdelegates3.AdapterDelegatesManager
import com.hannesdorfmann.adapterdelegates3.ListDelegationAdapter
import com.scp.scpexam.Constants
import com.scp.scpexam.EndlessRecyclerViewScrollListener
import com.scp.scpexam.R
import com.scp.scpexam.controller.adapter.MyListItem
import com.scp.scpexam.controller.adapter.delegate.LeaderboardDelegate
import com.scp.scpexam.controller.adapter.viewmodel.LeaderboardViewModel
import com.scp.scpexam.controller.manager.preference.MyPreferenceManager
import com.scp.scpexam.databinding.FragmentLeaderboardBinding
import com.scp.scpexam.model.api.NwUser
import com.scp.scpexam.mvp.presenter.util.LeaderboardPresenter
import com.scp.scpexam.mvp.view.util.LeaderboardView
import com.scp.scpexam.ui.BaseFragment
import com.scp.scpexam.ui.utils.AuthDelegate
import com.scp.scpexam.ui.utils.GlideApp
import com.scp.scpexam.utils.DimensionUtils
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import toothpick.Toothpick
import toothpick.config.Module
import javax.inject.Inject


class LeaderboardFragment :
    BaseFragment<LeaderboardView, LeaderboardPresenter, FragmentLeaderboardBinding>(),
    LeaderboardView {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentLeaderboardBinding
        get() = FragmentLeaderboardBinding::inflate

    override val translucent = false

    override val scopes: Array<String> = arrayOf()

    override val modules: Array<Module> = arrayOf()

    private lateinit var authDelegate: AuthDelegate<LeaderboardFragment>

    @Inject
    lateinit var preferenceManager: MyPreferenceManager

    @InjectPresenter
    override lateinit var presenter: LeaderboardPresenter

    @ProvidePresenter
    override fun providePresenter(): LeaderboardPresenter =
        scope.getInstance(LeaderboardPresenter::class.java)

    override fun inject() = Toothpick.inject(this, scope)

    override fun getLayoutResId() = R.layout.fragment_leaderboard

    lateinit var adapter: ListDelegationAdapter<List<MyListItem>>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(activity as AppCompatActivity) {
            setSupportActionBar(binding.toolbar)
            supportActionBar?.let {
                it.setDisplayHomeAsUpEnabled(true)
                it.setHomeButtonEnabled(true)
            }
        }
        binding.toolbar.setNavigationOnClickListener { presenter.onBackClicked() }

        if (preferenceManager.getTrueAccessToken() == null) {
            binding.itemUserInLeaderboardView.visibility = View.GONE
        } else {
            binding.googleImage.visibility = View.GONE
            binding.vkImage.visibility = View.GONE
            binding.faceBookImage.visibility = View.GONE
        }

        binding.retryGetCurrentUserImage.setOnClickListener { presenter.getCurrentPositionInLeaderboard() }
        initRecyclerView()
        binding.swipeRefresher.setOnRefreshListener { presenter.showLeaderboard(Constants.OFFSET_ZERO) }

        authDelegate = AuthDelegate(
            this,
            presenter,
            presenter.apiClient,
            presenter.preferences
        )

        val onVkLoginClickListener: (View) -> Unit = { presenter.onVkLoginClicked() }
        binding.vkImage.setOnClickListener(onVkLoginClickListener)

        val onFacebookLoginClickListener: (View) -> Unit = { presenter.onFacebookLoginClicked() }
        binding.faceBookImage.setOnClickListener(onFacebookLoginClickListener)

        val onGoogleLoginClickListener: (View) -> Unit = { presenter.onGoogleLoginClicked() }
        binding.googleImage.setOnClickListener(onGoogleLoginClickListener)

        presenter.authDelegate = authDelegate
        activity?.let { authDelegate.onViewCreated(it) }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (data != null) {
            presenter.onActivityResult(requestCode, resultCode, data)
        } else {
            showMessage("Intent is null $requestCode")
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onPause() {
        super.onPause()
        authDelegate.onPause()
    }

    override fun showProgress(show: Boolean) {
        binding.progressView.visibility = if (show) View.VISIBLE else View.GONE
    }

    override fun showCurrentUserProgressBar(showCurrentUserProgressBar: Boolean) {
        binding.currentUserProgressBar.visibility =
            if (showCurrentUserProgressBar) View.VISIBLE else View.GONE
    }

    override fun showCurrentUserUI(showCurrentUserUI: Boolean) {
        binding.itemUserInLeaderboardView.visibility =
            if (showCurrentUserUI) View.VISIBLE else View.GONE
    }

    override fun showRetryButton(showRetryButton: Boolean) {
        binding.retryGetCurrentUserImage.visibility =
            if (showRetryButton) View.VISIBLE else View.GONE
    }

    override fun showSwipeProgressBar(showSwipeProgressBar: Boolean) {
        binding.swipeRefresher.setProgressViewEndTarget(
            false,
            DimensionUtils.getActionBarHeight(requireActivity())
        )
        binding.swipeRefresher.isRefreshing = showSwipeProgressBar
    }

    override fun showLeaderboard(users: List<LeaderboardViewModel>) {
        adapter.items = users
        adapter.notifyDataSetChanged()
    }

    override fun showBottomProgress(showBottomProgress: Boolean) {
        binding.swipeRefresher.setProgressViewEndTarget(
            false,
            DimensionUtils.getScreenHeight() - DimensionUtils.getActionBarHeight(requireActivity()) * 3
        )
        binding.swipeRefresher.isRefreshing = showBottomProgress
    }

    override fun enableScrollListener(enableScrollListener: Boolean) {
        binding.recyclerViewLeaderboard.clearOnScrollListeners()
        if (enableScrollListener) {
            binding.recyclerViewLeaderboard.addOnScrollListener(object :
                EndlessRecyclerViewScrollListener() {
                override fun onLoadMore(page: Int, totalItemsCount: Int) {
                    if (adapter.itemCount % Constants.LIMIT_PAGE != 0) {
                        enableScrollListener(false)
                    } else {
                        presenter.getLeaderboard(adapter.itemCount)
                    }
                }
            })
        }
    }

    override fun showUserPosition(user: NwUser, position: Int) {
        binding.itemLeaderboard.userPositionTextView.text = position.toString()
        binding.itemLeaderboard.userFullNameTextView.text = user.fullName
        binding.itemLeaderboard.userScoreTextView.text = user.score.toString()
        binding.itemLeaderboard.userFullCompleteLevelsTextView.text =
            context?.getString(R.string.complete_levels_text, user.fullCompleteLevels)
        binding.itemLeaderboard.userPartCompleteLevelsTextView.text =
            context?.getString(R.string.part_complete_levels_text, user.partCompleteLevels)

        val glideRequest = GlideApp.with(binding.itemLeaderboard.userAvatarImageView.context)
        when (user.avatar) {
            null -> glideRequest.load(R.drawable.ic_player)
            else -> glideRequest.load(user.avatar)
        }
            .apply(RequestOptions.circleCropTransform())
            .into(binding.itemLeaderboard.userAvatarImageView)
    }

    private fun initRecyclerView() {
        binding.recyclerViewLeaderboard.layoutManager = LinearLayoutManager(activity)
        val delegateManager = AdapterDelegatesManager<List<MyListItem>>()
        delegateManager.addDelegate(LeaderboardDelegate())
        adapter = ListDelegationAdapter(delegateManager)
        binding.recyclerViewLeaderboard.adapter = adapter
    }

    companion object {
        fun newInstance() = LeaderboardFragment()
    }
}