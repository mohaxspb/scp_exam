package ru.kuchanov.scpquiz.controller.adapter.viewmodel

import ru.kuchanov.scpquiz.controller.adapter.MyListItem
import ru.kuchanov.scpquiz.model.db.Quiz

class LevelViewModel(val quiz: Quiz, val levelCompleted: Boolean = false) : MyListItem