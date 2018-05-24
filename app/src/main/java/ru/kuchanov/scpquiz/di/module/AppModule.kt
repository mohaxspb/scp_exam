package ru.kuchanov.scpquiz.di.module

import android.arch.persistence.room.Room
import android.content.Context
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import ru.kuchanov.scpquiz.db.AppDatabase
import ru.kuchanov.scpquiz.model.api.QuizConverter
import toothpick.config.Module
import java.util.*


class AppModule(context: Context) : Module() {

    init {
        bind(AppDatabase::class.java).toInstance(
            Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                "database"
            )
                    //todo create migrations if need
                    .fallbackToDestructiveMigration()
                    .build()
        )

        bind(QuizConverter::class.java).singletonInScope()

        bind(Moshi::class.java).toInstance(
            Moshi.Builder()
                    .add(Date::class.java, Rfc3339DateJsonAdapter().nullSafe())
                    .build())
    }
}