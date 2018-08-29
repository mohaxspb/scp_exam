package ru.kuchanov.scpquiz.controller.api

import io.reactivex.Single
import retrofit2.HttpException
import retrofit2.Retrofit
import ru.kuchanov.scpquiz.App
import ru.kuchanov.scpquiz.BuildConfig
import ru.kuchanov.scpquiz.controller.manager.preference.MyPreferenceManager
import ru.kuchanov.scpquiz.di.qualifier.VpsQuizApi
import ru.kuchanov.scpquiz.di.qualifier.VpsToolsApi
import ru.kuchanov.scpquiz.model.api.NwQuiz
import java.net.HttpURLConnection
import javax.inject.Inject


class ApiClient @Inject constructor(
    @VpsToolsApi toolsRetrofit: Retrofit,
    @VpsQuizApi quizRetrofit: Retrofit,
    val preferences: MyPreferenceManager
) {

    private val toolsApi = toolsRetrofit.create(ToolsApi::class.java)

    private val quizApi = quizRetrofit.create(QuizApi::class.java)

    fun validateInApp(sku: String, purchaseToken: String): Single<Int> = toolsApi.validatePurchase(
        false,
        App.INSTANCE.packageName,
        sku,
        purchaseToken
    ).map { it.status }

    private fun getAccessToken() = quizApi.getAccessToken(
        okhttp3.Credentials.basic(
            BuildConfig.USER,
            BuildConfig.PASSWORD
        ),
        BuildConfig.GRANT_TYPE
    ).doOnSuccess { (accessToken) -> preferences.setAccessToken(accessToken) }

    fun getNwQuizList(): Single<List<NwQuiz>> = quizApi.getNwQuizList("Bearer " + preferences.getAccessToken())
            .onErrorResumeNext { error: Throwable ->
                if (error is HttpException) {
                    if (error.code() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                        return@onErrorResumeNext getAccessToken().flatMap<List<NwQuiz>> { getNwQuizList() }
                    }
                }
                Single.error<List<NwQuiz>>(error)
            }
}