package ru.kuchanov.scpquiz.mvp.view

import ru.kuchanov.scpquiz.mvp.BaseView

interface GameView : BaseView {
    fun showProgress(show: Boolean)
}