package ru.kuchanov.scpquiz.controller.adapter.viewmodel

import ru.kuchanov.scpquiz.controller.adapter.MyListItem
import ru.kuchanov.scpquiz.model.db.User

data class MonetizationHeaderViewModel(
    val player: User
) : MyListItem