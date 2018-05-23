package ru.kuchanov.scpquiz.db

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import ru.kuchanov.scpquiz.db.converter.MyDateConverter
import ru.kuchanov.scpquiz.db.dao.QuizDao
import ru.kuchanov.scpquiz.model.db.Quiz
import ru.kuchanov.scpquiz.model.db.QuizTranslation


@Database(entities = [Quiz::class, QuizTranslation::class], version = 1)
@TypeConverters(MyDateConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun quizDao(): QuizDao
}