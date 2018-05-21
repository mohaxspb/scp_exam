package ru.kuchanov.scpquiz.di.module

import android.arch.persistence.room.Room
import android.content.Context
import com.squareup.moshi.Moshi
import ru.kuchanov.scpquiz.db.AppDatabase
import toothpick.config.Module


class AppModule(context: Context) : Module() {

    init {
        bind(AppDatabase::class.java).toInstance(
            Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                "database"
            ).build()
        )

        bind(Moshi::class.java).toInstance(Moshi.Builder().build())
    }
}