package com.scp.scpexam.ui.fragment.monetization

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import com.hannesdorfmann.adapterdelegates3.AdapterDelegatesManager
import com.hannesdorfmann.adapterdelegates3.ListDelegationAdapter
import jp.wasabeef.blurry.Blurry
import kotlinx.android.synthetic.main.fragment_monetization.*
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import com.scp.scpexam.Constants
import com.scp.scpexam.R
import com.scp.scpexam.controller.adapter.MyListItem
import com.scp.scpexam.controller.adapter.delegate.MonetizationDelegate
import com.scp.scpexam.controller.adapter.delegate.MonetizationHeaderDelegate
import com.scp.scpexam.controller.manager.monetization.BillingDelegate
import com.scp.scpexam.mvp.presenter.monetization.MonetizationPresenter
import com.scp.scpexam.mvp.view.monetization.MonetizationView
import com.scp.scpexam.ui.BaseActivity
import com.scp.scpexam.ui.BaseFragment
import com.scp.scpexam.utils.BitmapUtils
import toothpick.Toothpick
import toothpick.config.Module


class MonetizationFragment : BaseFragment<MonetizationView, MonetizationPresenter>(), MonetizationView {

    companion object {

        fun newInstance() = MonetizationFragment()
    }

    override val translucent = true

    override val scopes: Array<String> = arrayOf()

    override val modules: Array<Module> = arrayOf()

    @InjectPresenter
    override lateinit var presenter: MonetizationPresenter

    @ProvidePresenter
    override fun providePresenter(): MonetizationPresenter = scope.getInstance(MonetizationPresenter::class.java)

    override fun inject() = Toothpick.inject(this, scope)

    override fun getLayoutResId() = R.layout.fragment_monetization

    lateinit var adapter: ListDelegationAdapter<List<MyListItem>>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //todo move to delegate
        val bitmap = BitmapUtils.fileToBitmap(
                "${activity?.cacheDir}/${Constants.SETTINGS_BACKGROUND_FILE_NAME}.png"
        )

        backgroundImageView.post {
            Blurry.with(context)
                    .async()
                    .animate(500)
                    .from(bitmap)
                    .into(backgroundImageView)
        }

        initRecyclerView()

        toolbar.setNavigationOnClickListener { presenter.onNavigationIconClicked() }

        renewFab.setOnClickListener { presenter.loadInAppsToBuy(true) }

        presenter.billingDelegate = BillingDelegate(activity as AppCompatActivity, this, presenter)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        presenter.billingDelegate = null
    }

    override fun showMonetizationActions(actions: MutableList<MyListItem>) {
        adapter.items = actions
        adapter.notifyDataSetChanged()
    }

    private fun initRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(activity)
        val delegateManager = AdapterDelegatesManager<List<MyListItem>>()
        delegateManager.addDelegate(MonetizationHeaderDelegate())
        delegateManager.addDelegate(MonetizationDelegate { presenter.onOwnedItemClicked(it) })
        adapter = ListDelegationAdapter(delegateManager)
        recyclerView.adapter = adapter
    }

    override fun onNeedToShowRewardedVideo() = (activity as BaseActivity<*, *>).showRewardedVideo()

    override fun showProgress(show: Boolean) {
        progressView.visibility = if (show) VISIBLE else GONE
    }

    override fun showRefreshFab(show: Boolean) {
        renewFab.visibility = if (show) VISIBLE else GONE
    }
}