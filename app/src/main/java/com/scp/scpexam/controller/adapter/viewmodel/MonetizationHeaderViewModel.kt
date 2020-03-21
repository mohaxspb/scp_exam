package com.scp.scpexam.controller.adapter.viewmodel

import com.scp.scpexam.controller.adapter.MyListItem
import com.scp.scpexam.model.db.User

data class MonetizationHeaderViewModel(
        val player: User,
        var showAuthButtons: Boolean
) : MyListItem