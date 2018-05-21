package ru.kuchanov.scpquiz.db

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import ru.kuchanov.scpquiz.db.dao.QuizDao
import ru.kuchanov.scpquiz.model.db.Quiz


@Database(entities = [Quiz::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun quizDao(): QuizDao
}