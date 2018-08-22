package ru.kuchanov.scpquiz.api

import java.net.HttpURLConnection

import javax.inject.Inject

import io.reactivex.Observable
import retrofit2.HttpException
import retrofit2.Retrofit
import ru.kuchanov.scpquiz.BuildConfig
import ru.kuchanov.scpquiz.controller.manager.preference.MyPreferenceManager
import ru.kuchanov.scpquiz.model.api.NwQuiz


class ApiClient @Inject constructor(
        retrofit: Retrofit,
        val preferences: MyPreferenceManager
) {
    private val quizApi: QuizApi = retrofit.create(QuizApi::class.java)

    private fun getAccessToken() =
            quizApi.getAccessToken(okhttp3.Credentials.basic(BuildConfig.USER, BuildConfig.PASSWORD), BuildConfig.GRANT_TYPE)
                    .doOnNext { (accessToken) -> preferences.setAccessToken(accessToken) }

    fun getNwQuizList(): Observable<List<NwQuiz>> = quizApi.getNwQuizList("Bearer " + preferences.getAccessToken())
            .onErrorResumeNext { error: Throwable ->
                if (error is HttpException) {
                    if (error.code() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                        getAccessToken().flatMap<List<NwQuiz>> { getNwQuizList() }
                    }
                }
                Observable.error<List<NwQuiz>>(error)
            }
}
