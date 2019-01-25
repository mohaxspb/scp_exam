package ru.kuchanov.scpquiz.controller.adapter.viewmodel

import ru.kuchanov.scpquiz.controller.adapter.MyListItem
import ru.kuchanov.scpquiz.model.db.Quiz

data class LevelViewModel(
        var quiz: Quiz,
        var scpNameFilled: Boolean = false,
        var scpNumberFilled: Boolean = false,
        var isLevelAvailable: Boolean = false,
        var showProgress: Boolean = false
) : MyListItem