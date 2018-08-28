package ru.kuchanov.scpquiz.di.module

import android.arch.persistence.room.Room
import android.content.Context
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import ru.kuchanov.scpquiz.BuildConfig
import ru.kuchanov.scpquiz.controller.api.ApiClient
import ru.kuchanov.scpquiz.controller.db.AppDatabase
import ru.kuchanov.scpquiz.controller.manager.preference.MyPreferenceManager
import ru.kuchanov.scpquiz.controller.navigation.ScpRouter
import ru.kuchanov.scpquiz.di.qualifier.VpsQuizApi
import ru.kuchanov.scpquiz.di.qualifier.VpsToolsApi
import ru.kuchanov.scpquiz.model.api.QuizConverter
import ru.terrakok.cicerone.Cicerone
import ru.terrakok.cicerone.NavigatorHolder
import timber.log.Timber
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
        val moshi = Moshi.Builder()
                .add(Date::class.java, Rfc3339DateJsonAdapter().nullSafe())
                .build()
        bind(Moshi::class.java).toInstance(moshi)

        //routing
        val cicerone: Cicerone<ScpRouter> = Cicerone.create(ScpRouter())
        bind(Cicerone::class.java).toInstance(cicerone)
        bind(ScpRouter::class.java).toInstance(cicerone.router)
        bind(NavigatorHolder::class.java).toInstance(cicerone.navigatorHolder)

        //api
        val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(
                        HttpLoggingInterceptor { log -> Timber.d(log) }
                                .setLevel(HttpLoggingInterceptor.Level.BODY)
                )
                .build()

        bind(Retrofit::class.java).withName(VpsToolsApi::class.java).toInstance(
            Retrofit.Builder()
                    .baseUrl(BuildConfig.VPS_API_URL)
                    .addConverterFactory(MoshiConverterFactory.create(moshi))
                    .client(okHttpClient)
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build()
        )

        bind(Retrofit::class.java).withName(VpsQuizApi::class.java).toInstance(
                Retrofit.Builder()
                        .baseUrl(BuildConfig.QUIZ_API_URL)
                        .addConverterFactory(MoshiConverterFactory.create(moshi))
                        .client(okHttpClient)
                        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                        .build()
        )
        bind(ApiClient::class.java).singletonInScope()
    }
}