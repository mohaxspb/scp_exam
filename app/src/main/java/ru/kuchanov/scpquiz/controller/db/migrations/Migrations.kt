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
    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            Timber.d("executeMigration 2_3")
            //add column for available property with true as default
            try {
                database.execSQL(
                        """
                            CREATE TABLE QuizTransaction(
                                 id INTEGER,
                                 quizId INTEGER,
                                 externalId INTEGER,
                                 transactionType TEXT NOT NULL,
                                 coinsAmount INTEGER,
                                 PRIMARY KEY (id)
                            )
                            """)
            } catch (e: Throwable) {
                Timber.e(e)
            }
        }
    }

    val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(database: SupportSQLiteDatabase) {
            Timber.d("executeMigration 3_4")
            try {
                //set quiz.authorId be nullable
                database.execSQL(
                        """
                            CREATE TABLE QuizTemp(
                                id INTEGER NOT NULL,
                                scpNumber TEXT NOT NULL,
                                imageUrl TEXT NOT NULL,
                                authorId INTEGER,
                                approved INTEGER NOT NULL,
                                approverId INTEGER,
                                created INTEGER NOT NULL,
                                updated INTEGER NOT NULL,
                                PRIMARY KEY(id)
                            )
                            """)
                database.execSQL("""
                    INSERT INTO QuizTemp SELECT * FROM Quiz
                    """)
                database.execSQL("""
                    DROP TABLE Quiz;
                    """)
                database.execSQL("""
                    ALTER TABLE QuizTemp RENAME TO Quiz;
                    """)

                //set quizTranslation.authorId be nullable
                database.execSQL(
                        """
                            CREATE TABLE QuizTranslationTemp(
                                id INTEGER NOT NULL,
                                quizId INTEGER NOT NULL,
                                langCode TEXT NOT NULL,
                                translation TEXT NOT NULL,
                                description TEXT NOT NULL,
                                approved INTEGER NOT NULL,
                                authorId INTEGER,
                                approverId INTEGER,
                                created INTEGER NOT NULL,
                                updated INTEGER NOT NULL,
                                PRIMARY KEY(id)
                            )
                            """)
                database.execSQL("""
                    INSERT INTO QuizTranslationTemp SELECT * FROM QuizTranslation
                    """)
                database.execSQL("""
                    DROP TABLE QuizTranslation;
                    """)
                database.execSQL("""
                    ALTER TABLE QuizTranslationTemp RENAME TO QuizTranslation;
                    """)

                //set quizTranslationPhrase.authorId be nullable
                database.execSQL(
                        """
                            CREATE TABLE QuizTranslationPhraseTemp(
                                id INTEGER NOT NULL,
                                quizTranslationId INTEGER NOT NULL,
                                translation TEXT NOT NULL,
                                approved INTEGER NOT NULL,
                                authorId INTEGER,
                                approverId INTEGER,
                                created INTEGER NOT NULL,
                                updated INTEGER NOT NULL,
                                PRIMARY KEY(id)
                            )
                            """)
                database.execSQL("""
                    INSERT INTO QuizTranslationPhraseTemp SELECT * FROM QuizTranslationPhrase
                    """)
                database.execSQL("""
                    DROP TABLE QuizTranslationPhrase;
                    """)
                database.execSQL("""
                    ALTER TABLE QuizTranslationPhraseTemp RENAME TO QuizTranslationPhrase;
                    """)
            } catch (e: Throwable) {
                Timber.e(e)
            }
        }
    }
    val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(database: SupportSQLiteDatabase) {
            Timber.d("executeMigration 4_5")
            //add column createdOnClient into QuizTransaction
            try {
                database.execSQL("""
                        ALTER TABLE QuizTransaction ADD COLUMN createdOnClient INTEGER NOT NULL DEFAULT CURRENT_TIMESTAMP
                                """)
            } catch (e: Throwable) {
                Timber.e(e)
            }
        }
    }
}