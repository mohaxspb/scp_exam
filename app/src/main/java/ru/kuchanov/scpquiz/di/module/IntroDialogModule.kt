package ru.kuchanov.scpquiz.di.module

import ru.kuchanov.scpquiz.mvp.presenter.intro.IntroDialogPresenter
import toothpick.config.Module

class IntroDialogModule : Module() {
    init {
        bind(IntroDialogPresenter::class.java).singletonInScope()
    }
}
