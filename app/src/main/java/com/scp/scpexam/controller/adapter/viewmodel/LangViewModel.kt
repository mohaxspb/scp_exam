package com.scp.scpexam.controller.adapter.viewmodel

import com.scp.scpexam.controller.adapter.MyListItem

data class LangViewModel(
    val lang: String,
    val selected: Boolean = false
) : MyListItem