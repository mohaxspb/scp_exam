package ru.kuchanov.scpquiz.controller.db.migrations

import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.room.migration.Migration
import timber.log.Timber

object Migrations {

    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            Timber.d("executeMigration 1_2")
            //add column for available property with true as default
            try {
                database.execSQL(
                    "ALTER TABLE FinishedLevel "
                            + " ADD COLUMN isLevelAvailable INTEGER NOT NULL DEFAULT 1")
                //disable all except of first 5
                database.execSQL(
                    "UPDATE FinishedLevel SET isLevelAvailable=0 WHERE quizId IN "
                            + "(SELECT id from Quiz ORDER BY id ASC LIMIT -1 OFFSET 5)")
                //enable all, which has name or number entered or some chars removed
                database.execSQL(
                    "UPDATE FinishedLevel SET isLevelAvailable=1 "
                            + "WHERE scpNameFilled=1 OR scpNumberFilled=1 OR nameRedundantCharsRemoved=1 OR numberRedundantCharsRemoved=1")
            } catch (e: Throwable) {
                Timber.e(e)
            }
        }
    }
}