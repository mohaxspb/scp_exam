package ru.kuchanov.scpquiz.ui.fragment.util

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.bumptech.glide.request.RequestOptions
import com.hannesdorfmann.adapterdelegates3.AdapterDelegatesManager
import com.hannesdorfmann.adapterdelegates3.ListDelegationAdapter
import kotlinx.android.synthetic.main.fragment_leaderboard.*
import kotlinx.android.synthetic.main.item_leaderboard.view.*
import ru.kuchanov.scpquiz.Constants
import ru.kuchanov.scpquiz.EndlessRecyclerViewScrollListener
import ru.kuchanov.scpquiz.R
import ru.kuchanov.scpquiz.controller.adapter.MyListItem
import ru.kuchanov.scpquiz.controller.adapter.delegate.LeaderboardDelegate
import ru.kuchanov.scpquiz.controller.adapter.viewmodel.LeaderboardViewModel
import ru.kuchanov.scpquiz.controller.manager.preference.MyPreferenceManager
import ru.kuchanov.scpquiz.model.api.NwUser
import ru.kuchanov.scpquiz.mvp.presenter.util.LeaderboardPresenter
import ru.kuchanov.scpquiz.mvp.view.util.LeaderboardView
import ru.kuchanov.scpquiz.ui.BaseFragment
import ru.kuchanov.scpquiz.ui.utils.AuthDelegate
import ru.kuchanov.scpquiz.ui.utils.GlideApp
import ru.kuchanov.scpquiz.utils.DimensionUtils
import toothpick.Toothpick
import toothpick.config.Module
import javax.inject.Inject


class LeaderboardFragment : BaseFragment<LeaderboardView, LeaderboardPresenter>(), LeaderboardView {
    override val translucent = false

    override val scopes: Array<String> = arrayOf()

    override val modules: Array<Module> = arrayOf()

    private lateinit var authDelegate: AuthDelegate<LeaderboardFragment>

    @Inject
    lateinit var preferenceManager: MyPreferenceManager

    @InjectPresenter
    override lateinit var presenter: LeaderboardPresenter

    @ProvidePresenter
    override fun providePresenter(): LeaderboardPresenter = scope.getInstance(LeaderboardPresenter::class.java)

    override fun inject() = Toothpick.inject(this, scope)

    override fun getLayoutResId() = R.layout.fragment_leaderboard

    lateinit var adapter: ListDelegationAdapter<List<MyListItem>>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(activity as AppCompatActivity) {
            setSupportActionBar(toolbar)
            supportActionBar?.let {
                it.setDisplayHomeAsUpEnabled(true)
                it.setHomeButtonEnabled(true)
            }
        }
        toolbar.setNavigationOnClickListener { presenter.onBackClicked() }

        if (preferenceManager.getTrueAccessToken() == null) {
            itemUserInLeaderboardView.visibility = View.GONE
        } else {
            googleImage.visibility = View.GONE
            vkImage.visibility = View.GONE
            faceBookImage.visibility = View.GONE
        }

        retryGetCurrentUserImage.setOnClickListener { presenter.getCurrentPositionInLeaderboard() }
        initRecyclerView()
        swipeRefresher.setOnRefreshListener { presenter.showLeaderboard(Constants.OFFSET_ZERO) }

        authDelegate = AuthDelegate(
                this,
                presenter,
                presenter.apiClient,
                presenter.preferences
        )

        val onVkLoginClickListener: (View) -> Unit = { presenter.onVkLoginClicked() }
        vkImage.setOnClickListener(onVkLoginClickListener)

        val onFacebookLoginClickListener: (View) -> Unit = { presenter.onFacebookLoginClicked() }
        faceBookImage.setOnClickListener(onFacebookLoginClickListener)

        val onGoogleLoginClickListener: (View) -> Unit = { presenter.onGoogleLoginClicked() }
        googleImage.setOnClickListener(onGoogleLoginClickListener)

        presenter.authDelegate = authDelegate
        activity?.let { authDelegate.onViewCreated(it) }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        presenter.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onPause() {
        super.onPause()
        authDelegate.onPause()
    }

    override fun showProgress(show: Boolean) {
        progressView.visibility = if (show) View.VISIBLE else View.GONE
    }

    override fun showCurrentUserProgressBar(showCurrentUserProgressBar: Boolean) {
        currentUserProgressBar.visibility = if (showCurrentUserProgressBar) View.VISIBLE else View.GONE
    }

    override fun showCurrentUserUI(showCurrentUserUI: Boolean) {
        itemUserInLeaderboardView.visibility = if (showCurrentUserUI) View.VISIBLE else View.GONE
    }

    override fun showRetryButton(showRetryButton: Boolean) {
        retryGetCurrentUserImage.visibility = if (showRetryButton) View.VISIBLE else View.GONE
    }

    override fun showSwipeProgressBar(showSwipeProgressBar: Boolean) {
        swipeRefresher.setProgressViewEndTarget(false, DimensionUtils.getActionBarHeight(activity!!))
        swipeRefresher.isRefreshing = showSwipeProgressBar
    }

    override fun showLeaderboard(users: List<LeaderboardViewModel>) {
        adapter.items = users
        adapter.notifyDataSetChanged()
    }

    override fun showBottomProgress(showBottomProgress: Boolean) {
        swipeRefresher.setProgressViewEndTarget(false, DimensionUtils.getScreenHeight() - DimensionUtils.getActionBarHeight(activity!!) * 3)
        swipeRefresher.isRefreshing = showBottomProgress
    }

    override fun enableScrollListener(enableScrollListener: Boolean) {
        recyclerViewLeaderboard.clearOnScrollListeners()
        if (enableScrollListener) {
            recyclerViewLeaderboard.addOnScrollListener(object : EndlessRecyclerViewScrollListener() {
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
        itemUserInLeaderboardView.userPositionTextView.text = position.toString()
        itemUserInLeaderboardView.userFullNameTextView.text = user.fullName
        itemUserInLeaderboardView.userScoreTextView.text = user.score.toString()
        itemUserInLeaderboardView.userFullCompleteLevelsTextView.text = context?.getString(R.string.complete_levels_text, user.fullCompleteLevels)
        itemUserInLeaderboardView.userPartCompleteLevelsTextView.text = context?.getString(R.string.part_complete_levels_text, user.partCompleteLevels)

        val glideRequest = GlideApp.with(itemUserInLeaderboardView.userAvatarImageView.context)
        when (user.avatar) {
            null -> glideRequest.load(R.drawable.ic_player)
            else -> glideRequest.load(user.avatar)
        }
                .apply(RequestOptions.circleCropTransform())
                .into(itemUserInLeaderboardView.userAvatarImageView)
    }

    private fun initRecyclerView() {
        recyclerViewLeaderboard.layoutManager = LinearLayoutManager(activity)
        val delegateManager = AdapterDelegatesManager<List<MyListItem>>()
        delegateManager.addDelegate(LeaderboardDelegate())
        adapter = ListDelegationAdapter(delegateManager)
        recyclerViewLeaderboard.adapter = adapter
    }

    companion object {
        fun newInstance() = LeaderboardFragment()
    }
}