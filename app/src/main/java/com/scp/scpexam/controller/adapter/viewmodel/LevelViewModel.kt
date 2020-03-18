package com.scp.scpexam.controller.adapter.viewmodel

import com.scp.scpexam.controller.adapter.MyListItem
import com.scp.scpexam.model.db.Quiz

data class LevelViewModel(
        var quiz: Quiz,
        var scpNameFilled: Boolean = false,
        var scpNumberFilled: Boolean = false,
        var isLevelAvailable: Boolean = false,
        var showProgress: Boolean = false
) : MyListItem