package com.scp.scpexam.di.module

import androidx.room.Room
import android.content.Context
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Credentials
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import com.scp.scpexam.BuildConfig
import com.scp.scpexam.Constants
import com.scp.scpexam.controller.api.*
import com.scp.scpexam.controller.db.AppDatabase
import com.scp.scpexam.controller.db.migrations.Migrations
import com.scp.scpexam.controller.manager.preference.MyPreferenceManager
import com.scp.scpexam.controller.navigation.ScpRouter
import com.scp.scpexam.controller.repository.SettingsRepository
import com.scp.scpexam.model.api.QuizConverter
import com.scp.scpexam.model.util.QuizFilter
import okhttp3.Interceptor
import okhttp3.Response
import ru.terrakok.cicerone.Cicerone
import ru.terrakok.cicerone.NavigatorHolder
import timber.log.Timber
import toothpick.config.Module
import java.net.HttpURLConnection
import java.util.*


class AppModule(context: Context) : Module() {

    init {
        bind(Context::class.java).toInstance(context)
        //preferences
        val preferenceManager = MyPreferenceManager(context)
        bind(MyPreferenceManager::class.java).toInstance(preferenceManager)
        //database
        bind(AppDatabase::class.java).toInstance(
                Room.databaseBuilder(
                        context,
                        AppDatabase::class.java,
                        "database"
                )
                        .addMigrations(Migrations.MIGRATION_1_2, Migrations.MIGRATION_2_3, Migrations.MIGRATION_3_4, Migrations.MIGRATION_4_5)
                        .build()
        )

        //models utils
        bind(QuizConverter::class.java)
        bind(QuizFilter::class.java)

        //json
        val moshi = Moshi.Builder()
                .add(Date::class.java, Rfc3339DateJsonAdapter().nullSafe())
                .add(KotlinJsonAdapterFactory())
                .build()
        bind(Moshi::class.java).toInstance(moshi)

        //routing
        val cicerone: Cicerone<ScpRouter> = Cicerone.create(ScpRouter())
        bind(Cicerone::class.java).toInstance(cicerone)
        bind(ScpRouter::class.java).toInstance(cicerone.router)
        bind(NavigatorHolder::class.java).toInstance(cicerone.navigatorHolder)

        //api
        val okHttpClientCommon = OkHttpClient.Builder()
                .addInterceptor(
                        HttpLoggingInterceptor(object :HttpLoggingInterceptor.Logger{
                            override fun log(message: String) {
                                Timber.tag("OkHttp").d(message)
                            }
                        })
                                .setLevel(HttpLoggingInterceptor.Level.BODY)
                )
                .build()
        val authRetrofit = Retrofit.Builder()
                .baseUrl(BuildConfig.QUIZ_API_URL)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .client(okHttpClientCommon)
                .build()
        val authApi = authRetrofit.create(AuthApi::class.java)
        bind(AuthApi::class.java).toInstance(authApi)

        val authorizedOkHttpClient = OkHttpClient.Builder()
                .addInterceptor(
                        HttpLoggingInterceptor(object :HttpLoggingInterceptor.Logger{
                            override fun log(message: String) {
                                Timber.tag("OkHttp").d(message)
                            }
                        })
                                .setLevel(HttpLoggingInterceptor.Level.BODY)
                )
                .addInterceptor ( object : Interceptor{
                    override fun intercept(chain: Interceptor.Chain): Response {
                        var request = chain.request()
                        request = request
                                .newBuilder()
                                .header(
                                        Constants.Api.HEADER_AUTHORIZATION,
                                        Constants.Api.HEADER_PART_BEARER + preferenceManager.getTrueAccessToken()
                                )
                                .build()
                        return chain.proceed(request)
                    }
                }
                )
                .addInterceptor ( object :Interceptor {
                    override fun intercept(chain: Interceptor.Chain): Response {
                        var request = chain.request()
                        var response = chain.proceed(request)
                        if (response.code == HttpURLConnection.HTTP_UNAUTHORIZED) {
                            val tokenResponse = authApi
                                    .getAccessTokenByRefreshToken(
                                            Credentials.basic(BuildConfig.CLIENT_ID, BuildConfig.CLIENT_SECRET),
                                            Constants.Api.GRANT_TYPE_REFRESH_TOKEN,
                                            preferenceManager.getRefreshToken()!!
                                    )
                                    .blockingGet()
                            preferenceManager.setTrueAccessToken(tokenResponse.accessToken)
                            preferenceManager.setRefreshToken(tokenResponse.refreshToken)

                            request = request
                                    .newBuilder()
                                    .header(
                                            Constants.Api.HEADER_AUTHORIZATION,
                                            Constants.Api.HEADER_PART_BEARER + tokenResponse.accessToken
                                    )
                                    .build()
                            response = chain.proceed(request)
                        }
                        return response
                    }
                }
                )
                .build()

        val quizOkHttpClient = OkHttpClient.Builder()
                .addInterceptor(
                        HttpLoggingInterceptor ( object : HttpLoggingInterceptor.Logger{
                            override fun log(message: String) {
                                Timber.tag("OkHttp").d(message)
                            }
                        })
                                .setLevel(HttpLoggingInterceptor.Level.BODY)
                )
                .addInterceptor ( object : Interceptor {
                    override fun intercept(chain: Interceptor.Chain): Response {
                        var request = chain.request()
                        request = request
                                .newBuilder()
                                .header(
                                        Constants.Api.HEADER_AUTHORIZATION,
                                        Constants.Api.HEADER_PART_BEARER + preferenceManager.getAccessToken()
                                )
                                .build()
                        return chain.proceed(request)
                    }
                }
                )
                .addInterceptor ( object : Interceptor{
                    override fun intercept(chain: Interceptor.Chain): Response {
                        var request = chain.request()
                        var response = chain.proceed(request)
                        if (response.code == HttpURLConnection.HTTP_UNAUTHORIZED) {
                            //use implicit grant flow if there is no refresh token
                            val tokenResponse = authApi
                                    .getAccessToken(
                                            Credentials.basic(BuildConfig.USER, BuildConfig.PASSWORD),
                                            Constants.Api.GRANT_TYPE_CLIENT_CREDENTIALS
                                    )
                                    .blockingGet()

                            preferenceManager.setAccessToken(tokenResponse.accessToken)

                            request = request
                                    .newBuilder()
                                    .header(
                                            Constants.Api.HEADER_AUTHORIZATION,
                                            Constants.Api.HEADER_PART_BEARER + tokenResponse.accessToken
                                    )
                                    .build()
                            response = chain.proceed(request)
                        }
                        return response
                    }
                }
                )
                .build()

        val quizRetrofit = Retrofit.Builder()
                .baseUrl(BuildConfig.QUIZ_API_URL)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .client(quizOkHttpClient)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
        bind(QuizApi::class.java).toInstance(quizRetrofit.create(QuizApi::class.java))

        val toolsRetrofit = Retrofit.Builder()
                .baseUrl(BuildConfig.VPS_API_URL)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .client(okHttpClientCommon)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
        bind(ToolsApi::class.java).toInstance(toolsRetrofit.create(ToolsApi::class.java))

        val transactionRetrofit = Retrofit.Builder()
                .baseUrl(BuildConfig.QUIZ_API_URL)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .client(authorizedOkHttpClient)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
        bind(TransactionApi::class.java).toInstance(transactionRetrofit.create(TransactionApi::class.java))

        bind(ApiClient::class.java)

        bind(SettingsRepository::class.java).singletonInScope()
    }
}