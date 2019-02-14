package ru.kuchanov.scpquiz.controller.adapter.viewmodel

import ru.kuchanov.scpquiz.controller.adapter.MyListItem

data class LeaderboardViewModel(
        var name: String? = null,
        var avatarUrl: String? = null,
        var score: Int = 0,
        val fullCompleteLevels: Int = 0,
        val partCompleteLevels: Int = 0
) : MyListItem
