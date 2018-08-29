package ru.kuchanov.scpquiz.controller.adapter.viewmodel

import ru.kuchanov.scpquiz.controller.adapter.MyListItem

data class LangViewModel(
    val lang: String,
    val selected: Boolean = false
) : MyListItem