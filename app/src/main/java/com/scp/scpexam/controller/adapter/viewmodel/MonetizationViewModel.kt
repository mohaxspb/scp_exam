package com.scp.scpexam.controller.adapter.viewmodel

import androidx.annotation.DrawableRes
import com.scp.scpexam.controller.adapter.MyListItem

data class MonetizationViewModel(
        @DrawableRes val icon: Int,
        val title: String,
        val description: String,
        val price: String,
        val sku: String? = null,
        val isAlreadyOwned: Boolean,
        val action: (String?) -> Unit
) : MyListItem