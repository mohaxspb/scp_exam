package ru.kuchanov.scpquiz.ui.fragment.game

import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.view.View
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.hannesdorfmann.adapterdelegates3.AdapterDelegatesManager
import com.hannesdorfmann.adapterdelegates3.ListDelegationAdapter
import kotlinx.android.synthetic.main.fragment_levels.*
import ru.kuchanov.scpquiz.R
import ru.kuchanov.scpquiz.controller.adapter.MyListItem
import ru.kuchanov.scpquiz.controller.adapter.delegate.LevelDelegate
import ru.kuchanov.scpquiz.controller.adapter.viewmodel.LevelViewModel
import ru.kuchanov.scpquiz.di.Di
import ru.kuchanov.scpquiz.di.module.LevelsModule
import ru.kuchanov.scpquiz.mvp.presenter.game.LevelsPresenter
import ru.kuchanov.scpquiz.mvp.view.game.LevelsView
import ru.kuchanov.scpquiz.ui.BaseFragment
import toothpick.Toothpick
import toothpick.config.Module


class LevelsFragment : BaseFragment<LevelsView, LevelsPresenter>(), LevelsView {
    override val translucent = false

    override val scopes: Array<String> = arrayOf(Di.Scope.LEVELS_FRAGMENT)

    override val modules: Array<Module> = arrayOf(LevelsModule())

    @InjectPresenter
    override lateinit var presenter: LevelsPresenter

    @ProvidePresenter
    override fun providePresenter(): LevelsPresenter = scope.getInstance(LevelsPresenter::class.java)

    override fun inject() = Toothpick.inject(this, scope)

    override fun getLayoutResId() = R.layout.fragment_levels

    lateinit var adapter: ListDelegationAdapter<List<MyListItem>>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initRecyclerView()
    }

    private fun initRecyclerView() {
        recyclerView.layoutManager = GridLayoutManager(activity, 3)
        val delegateManager = AdapterDelegatesManager<List<MyListItem>>()
        delegateManager.addDelegate(LevelDelegate { presenter.onLevelClick(it) })
        adapter = ListDelegationAdapter(delegateManager)
        recyclerView.adapter = adapter
    }

    override fun showProgress(show: Boolean) {
        progressView.visibility = if (show) View.VISIBLE else View.GONE
    }

    override fun showLevels(quizes: List<LevelViewModel>) {
        adapter.items = quizes
        adapter.notifyDataSetChanged()
    }

    companion object {
        fun newInstance() = LevelsFragment()
    }
}