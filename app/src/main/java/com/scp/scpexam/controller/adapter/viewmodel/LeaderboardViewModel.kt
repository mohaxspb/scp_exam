package com.scp.scpexam.controller.adapter.viewmodel

import com.scp.scpexam.controller.adapter.MyListItem

data class LeaderboardViewModel(
        var name: String? = null,
        var avatarUrl: String? = null,
        var score: Int = 0,
        val fullCompleteLevels: Int = 0,
        val partCompleteLevels: Int = 0
) : MyListItem
