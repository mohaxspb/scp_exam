package ru.kuchanov.scpquiz.di.module

import ru.kuchanov.scpquiz.controller.interactor.GameInteractor
import ru.kuchanov.scpquiz.mvp.presenter.game.GamePresenter
import toothpick.config.Module

class GameModule : Module() {
    init {
        bind(GameInteractor::class.java).singletonInScope()
        bind(GamePresenter::class.java).singletonInScope()
    }
}
