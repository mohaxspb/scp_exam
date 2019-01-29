package ru.kuchanov.scpquiz.di.module

import ru.kuchanov.scpquiz.mvp.presenter.util.ScpSettingsPresenter
import toothpick.config.Module

class SettingsModule : Module() {
    init {
        bind(ScpSettingsPresenter::class.java).singletonInScope()
    }
}
