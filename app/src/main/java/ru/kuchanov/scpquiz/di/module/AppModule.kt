package ru.kuchanov.scpquiz.di.module

import android.arch.persistence.room.Room
import android.content.Context
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import ru.kuchanov.scpquiz.controller.db.AppDatabase
import ru.kuchanov.scpquiz.controller.manager.MyPreferenceManager
import ru.kuchanov.scpquiz.controller.navigation.ScpRouter
import ru.kuchanov.scpquiz.model.api.QuizConverter
import ru.terrakok.cicerone.Cicerone
import ru.terrakok.cicerone.NavigatorHolder
import ru.terrakok.cicerone.Router
import toothpick.config.Module
import java.util.*


class AppModule(context: Context) : Module() {

    init {
        bind(Context::class.java).toInstance(context)
        //preferences
        bind(MyPreferenceManager::class.java).singletonInScope()
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
                    .build()
        )

        //routing
        val cicerone: Cicerone<ScpRouter> = Cicerone.create(ScpRouter())
        bind(Cicerone::class.java).toInstance(cicerone)
        bind(ScpRouter::class.java).toInstance(cicerone.router)
        bind(NavigatorHolder::class.java).toInstance(cicerone.navigatorHolder)
    }
}