package ru.kuchanov.scpquiz.di.module

import ru.kuchanov.scpquiz.mvp.presenter.util.AppInfoPresenter
import toothpick.config.Module

class AppInfoModule : Module() {
    init {
        bind(AppInfoPresenter::class.java).singletonInScope()
    }
}
