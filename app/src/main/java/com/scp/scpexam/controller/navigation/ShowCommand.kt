package com.scp.scpexam.controller.navigation

import ru.terrakok.cicerone.commands.Command

data class ShowCommand(
    val screenKey: String,
    val transitionData: Any?
) : Command