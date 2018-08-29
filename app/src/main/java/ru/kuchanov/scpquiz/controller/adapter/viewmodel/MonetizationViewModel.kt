package ru.kuchanov.scpquiz.controller.adapter.viewmodel

import android.support.annotation.DrawableRes
import ru.kuchanov.scpquiz.controller.adapter.MyListItem

data class MonetizationViewModel(
    @DrawableRes val icon: Int,
    val title: String,
    val description: String,
    val price: String,
    val isAlreadyOwned: Boolean,
    val action: (Unit) -> Unit
) : MyListItem