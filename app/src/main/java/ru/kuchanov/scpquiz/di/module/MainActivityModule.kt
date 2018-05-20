package ru.kuchanov.scpquiz.di.module

import ru.kuchanov.scpquiz.mvp.presenter.MainPresenter
import toothpick.config.Module

class MainActivityModule : Module() {

    init {
        bind(MainPresenter::class.java).singletonInScope()
    }
}