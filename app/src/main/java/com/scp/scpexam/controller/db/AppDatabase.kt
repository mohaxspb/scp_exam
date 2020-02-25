package com.scp.scpexam.controller.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.scp.scpexam.controller.db.converter.MyDateConverter
import com.scp.scpexam.controller.db.converter.TransactionTypeConverter
import com.scp.scpexam.controller.db.converter.UserRoleConverter
import com.scp.scpexam.controller.db.dao.*
import com.scp.scpexam.model.db.*


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