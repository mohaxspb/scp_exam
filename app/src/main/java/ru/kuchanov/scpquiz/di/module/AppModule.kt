package ru.kuchanov.scpquiz.di.module

import android.arch.persistence.room.Room
import android.content.Context
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import ru.kuchanov.scpquiz.db.AppDatabase
import ru.kuchanov.scpquiz.model.api.QuizConverter
import ru.terrakok.cicerone.Cicerone
import ru.terrakok.cicerone.NavigatorHolder
import ru.terrakok.cicerone.Router
import toothpick.config.Module
import java.util.*


class AppModule(context: Context) : Module() {

    init {
        //database
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

        //models converter
        bind(QuizConverter::class.java).singletonInScope()

        //json
        bind(Moshi::class.java).toInstance(
            Moshi.Builder()
                    .add(Date::class.java, Rfc3339DateJsonAdapter().nullSafe())
                    .build())

        //routing
        val cicerone: Cicerone<Router> = Cicerone.create()
        bind(Cicerone::class.java).toInstance(cicerone)
        bind(Router::class.java).toInstance(cicerone.router)
        bind(NavigatorHolder::class.java).toInstance(cicerone.navigatorHolder)
    }
}