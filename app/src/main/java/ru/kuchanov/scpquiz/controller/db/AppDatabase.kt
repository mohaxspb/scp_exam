package ru.kuchanov.scpquiz.controller.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ru.kuchanov.scpquiz.controller.db.converter.MyDateConverter
import ru.kuchanov.scpquiz.controller.db.converter.TransactionTypeConverter
import ru.kuchanov.scpquiz.controller.db.converter.UserRoleConverter
import ru.kuchanov.scpquiz.controller.db.dao.*
import ru.kuchanov.scpquiz.model.db.*


@Database(
        entities = [
            Quiz::class,
            QuizTranslation::class,
            QuizTranslationPhrase::class,
            User::class,
            FinishedLevel::class,
            QuizTransaction::class
        ],
        version = 5
)
@TypeConverters(
        MyDateConverter::class,
        UserRoleConverter::class,
        TransactionTypeConverter::class
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun quizDao(): QuizDao

    abstract fun quizTranslationsDao(): QuizTranslationDao

    abstract fun userDao(): UserDao

    abstract fun finishedLevelsDao(): FinishedLevelsDao

    abstract fun transactionDao(): TransactionDao

}