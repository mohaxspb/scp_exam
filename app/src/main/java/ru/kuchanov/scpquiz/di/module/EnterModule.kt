package ru.kuchanov.scpquiz.di.module

import ru.kuchanov.scpquiz.mvp.presenter.intro.EnterPresenter
import toothpick.config.Module

class EnterModule : Module() {
    init {
        bind(EnterPresenter::class.java).singletonInScope()
    }
}