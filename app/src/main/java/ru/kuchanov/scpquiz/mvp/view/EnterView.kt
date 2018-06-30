package ru.kuchanov.scpquiz.mvp.view

import ru.kuchanov.scpquiz.mvp.BaseView

interface EnterView : BaseView {
    fun showProgressAnimation()
    fun showProgressText()
    fun showImage(imageNumber: Int)
    fun onNeedToOpenIntroDialogFragment()
}