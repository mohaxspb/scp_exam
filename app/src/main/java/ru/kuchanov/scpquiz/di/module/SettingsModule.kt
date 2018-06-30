package ru.kuchanov.scpquiz.di.module

import ru.kuchanov.scpquiz.mvp.presenter.util.SettingsPresenter
import toothpick.config.Module

class SettingsModule : Module() {
    init {
        bind(SettingsPresenter::class.java).singletonInScope()
    }
}
