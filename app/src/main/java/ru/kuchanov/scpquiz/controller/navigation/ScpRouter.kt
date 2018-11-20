package ru.kuchanov.scpquiz.controller.navigation

import ru.terrakok.cicerone.Router

class ScpRouter : Router() {
    fun addScreen(screenKey: String, data: Any?) {
        executeCommands(ShowCommand(screenKey, data))
    }
}