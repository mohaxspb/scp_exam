package com.scp.scpexam.di.module

import com.scp.scpexam.controller.interactor.GameInteractor
import com.scp.scpexam.mvp.presenter.game.GamePresenter
import toothpick.config.Module

class GameModule : Module() {

    init {
        bind(GameInteractor::class.java).singletonInScope()
        //todo check if we need it
        bind(GamePresenter::class.java)
    }
}
