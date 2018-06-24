package ru.kuchanov.scpquiz.di.module

import ru.kuchanov.scpquiz.mvp.presenter.game.LevelsPresenter
import toothpick.config.Module

class LevelsModule : Module() {
    init {
        bind(LevelsPresenter::class.java).singletonInScope()
    }
}
