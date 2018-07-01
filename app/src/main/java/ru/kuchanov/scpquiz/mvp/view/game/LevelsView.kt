package ru.kuchanov.scpquiz.mvp.view.game

import ru.kuchanov.scpquiz.controller.adapter.viewmodel.LevelViewModel
import ru.kuchanov.scpquiz.mvp.BaseView

interface LevelsView : BaseView {
    fun showProgress(show: Boolean)
    fun showLevels(quizes: List<LevelViewModel>)
}