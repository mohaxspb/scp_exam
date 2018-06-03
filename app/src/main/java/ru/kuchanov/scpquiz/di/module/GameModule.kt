package ru.kuchanov.scpquiz.di.module

import ru.kuchanov.scpquiz.mvp.presenter.GamePresenter
import toothpick.config.Module

class GameModule : Module() {
    init {
        bind(GamePresenter::class.java).singletonInScope()
    }
}
