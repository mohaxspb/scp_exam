package ru.kuchanov.scpquiz.mvp.view.util

import ru.kuchanov.scpquiz.controller.adapter.viewmodel.UserLeaderboardViewModel
import ru.kuchanov.scpquiz.model.api.NwUser
import ru.kuchanov.scpquiz.mvp.AuthView
import ru.kuchanov.scpquiz.mvp.BaseView

interface LeaderboardView : BaseView, AuthView {
    fun showProgress(show: Boolean)
    fun showLeaderboard(users: List<UserLeaderboardViewModel>)
    fun showUserPosition(user: NwUser, position: Int)
    fun showSwipeProgressBar(showSwipeProgressBar: Boolean)
    fun showBottomProgress(showBottomProgress: Boolean)
    fun enableScrollListener(enableScrollListener: Boolean)
    fun showCurrentUserUI(showCurrentUserUI: Boolean)
}