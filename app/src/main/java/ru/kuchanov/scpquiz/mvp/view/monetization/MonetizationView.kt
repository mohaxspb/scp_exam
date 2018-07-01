package ru.kuchanov.scpquiz.mvp.view.monetization

import ru.kuchanov.scpquiz.controller.adapter.viewmodel.MonetizationViewModel
import ru.kuchanov.scpquiz.mvp.BaseView

interface MonetizationView : BaseView {
    fun showMonetizationActions(actions: MutableList<MonetizationViewModel>)
}