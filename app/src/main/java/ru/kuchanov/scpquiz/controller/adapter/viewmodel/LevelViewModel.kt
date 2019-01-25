package ru.kuchanov.scpquiz.controller.adapter.viewmodel

import ru.kuchanov.scpquiz.controller.adapter.MyListItem
import ru.kuchanov.scpquiz.model.db.Quiz

data class LevelViewModel(
        val quiz: Quiz,
        val scpNameFilled: Boolean = false,
        val scpNumberFilled: Boolean = false,
        val isLevelAvailable: Boolean = false,
        var showProgress: Boolean = false
) : MyListItem