package ru.kuchanov.scpquiz.controller.db

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import ru.kuchanov.scpquiz.controller.db.converter.MyDateConverter
import ru.kuchanov.scpquiz.controller.db.dao.QuizDao
import ru.kuchanov.scpquiz.controller.db.dao.QuizTranslationDao
import ru.kuchanov.scpquiz.model.db.Quiz
import ru.kuchanov.scpquiz.model.db.QuizTranslation
import ru.kuchanov.scpquiz.model.db.QuizTranslationPhrase


@Database(entities = [Quiz::class, QuizTranslation::class, QuizTranslationPhrase::class], version = 1)
@TypeConverters(MyDateConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun quizDao(): QuizDao

    abstract fun quizTranslationsDao(): QuizTranslationDao
}