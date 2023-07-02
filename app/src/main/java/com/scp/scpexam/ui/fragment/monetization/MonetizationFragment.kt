package com.scp.scpexam.ui.fragment.monetization

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.hannesdorfmann.adapterdelegates3.AdapterDelegatesManager
import com.hannesdorfmann.adapterdelegates3.ListDelegationAdapter
import com.scp.scpexam.Constants
import com.scp.scpexam.R
import com.scp.scpexam.controller.adapter.MyListItem
import com.scp.scpexam.controller.adapter.delegate.MonetizationDelegate
import com.scp.scpexam.controller.adapter.delegate.MonetizationHeaderDelegate
import com.scp.scpexam.controller.manager.monetization.BillingDelegate
import com.scp.scpexam.databinding.FragmentMonetizationBinding
import com.scp.scpexam.mvp.presenter.monetization.MonetizationPresenter
import com.scp.scpexam.mvp.view.monetization.MonetizationView
import com.scp.scpexam.ui.BaseActivity
import com.scp.scpexam.ui.BaseFragment
import com.scp.scpexam.ui.utils.AuthDelegate
import com.scp.scpexam.utils.BitmapUtils
import jp.wasabeef.blurry.Blurry
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import timber.log.Timber
import toothpick.Toothpick
import toothpick.config.Module


class MonetizationFragment :
    BaseFragment<MonetizationView, MonetizationPresenter, FragmentMonetizationBinding>(),
    MonetizationView {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentMonetizationBinding
        get() = FragmentMonetizationBinding::inflate

    companion object {
        fun newInstance() = MonetizationFragment()
    }

    override val translucent = true

    override val scopes: Array<String> = arrayOf()

    override val modules: Array<Module> = arrayOf()

    @InjectPresenter
    override lateinit var presenter: MonetizationPresenter

    @ProvidePresenter
    override fun providePresenter(): MonetizationPresenter =
        scope.getInstance(MonetizationPresenter::class.java)

    override fun inject() = Toothpick.inject(this, scope)

    override fun getLayoutResId() = R.layout.fragment_monetization

    lateinit var adapter: ListDelegationAdapter<List<MyListItem>>

    private lateinit var authDelegate: AuthDelegate<MonetizationFragment>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //todo move to delegate
        val bitmap = BitmapUtils.fileToBitmap(
            "${activity?.cacheDir}/${Constants.SETTINGS_BACKGROUND_FILE_NAME}.png"
        )

        binding.backgroundImageView.post {
            Blurry.with(context)
                .async()
                .animate(500)
                .from(bitmap)
                .into(binding.backgroundImageView)
        }

        initRecyclerView()

        binding.toolbar.setNavigationOnClickListener { presenter.onNavigationIconClicked() }

        binding.swipeRefresher.setOnRefreshListener {
            binding.swipeRefresher.isRefreshing = false
            presenter.loadInAppsToBuy(true)
        }

        presenter.billingDelegate = BillingDelegate(activity as AppCompatActivity, this, presenter)

        authDelegate = AuthDelegate(
            this,
            presenter,
            presenter.apiClient,
            presenter.preferences
        )
        presenter.authDelegate = authDelegate
        activity?.let { authDelegate.onViewCreated(it) }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        presenter.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        presenter.billingDelegate = null

    }

    override fun onPause() {
        super.onPause()
        authDelegate.onPause()
    }

    override fun showMonetizationActions(actions: MutableList<MyListItem>) {
        adapter.items = actions
        adapter.notifyDataSetChanged()
    }

    private fun initRecyclerView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(activity)
        val delegateManager = AdapterDelegatesManager<List<MyListItem>>()
        delegateManager.addDelegate(MonetizationHeaderDelegate {
            Timber.d("$it")
            when (it) {
                Constants.Social.VK -> presenter.onVkLoginClicked()
                Constants.Social.FACEBOOK -> presenter.onFacebookLoginClicked()
                Constants.Social.GOOGLE -> presenter.onGoogleLoginClicked()
            }
        })
        delegateManager.addDelegate(MonetizationDelegate { presenter.onOwnedItemClicked(it) })
        adapter = ListDelegationAdapter(delegateManager)
        binding.recyclerView.adapter = adapter
    }

    override fun onNeedToShowRewardedVideo() = (activity as BaseActivity<*, *>).showRewardedVideo()

    override fun showProgress(show: Boolean) {
        binding.progressView.visibility = if (show) VISIBLE else GONE
    }

    override fun scrollToTop() = binding.recyclerView.scrollToPosition(0)

}