package ru.kuchanov.scpquiz.di.module

import ru.kuchanov.scpquiz.mvp.presenter.monetization.MonetizationPresenter
import toothpick.config.Module

class MonetizationModule : Module() {
    init {
        bind(MonetizationPresenter::class.java).singletonInScope()
    }
}
