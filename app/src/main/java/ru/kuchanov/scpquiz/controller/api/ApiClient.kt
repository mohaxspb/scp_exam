package ru.kuchanov.scpquiz.controller.api

import io.reactivex.Single
import retrofit2.HttpException
import ru.kuchanov.scpquiz.App
import ru.kuchanov.scpquiz.BuildConfig
import ru.kuchanov.scpquiz.Constants
import ru.kuchanov.scpquiz.controller.manager.preference.MyPreferenceManager
import ru.kuchanov.scpquiz.model.api.NwQuiz
import java.net.HttpURLConnection
import javax.inject.Inject


class ApiClient @Inject constructor(
        private val toolsApi: ToolsApi,
        private val quizApi: QuizApi,
        private val preferences: MyPreferenceManager
) {

    //todo create method on quiz api (I mean server)
    fun validateInApp(sku: String, purchaseToken: String): Single<Int> =
            toolsApi
                    .validatePurchase(
                            false,
                            App.INSTANCE.packageName,
                            sku,
                            purchaseToken
                    )
                    .map { it.status }

    /**
     * used for getting access token for unlogined user
     */
    private fun getAccessToken() =
            quizApi
                    .getUnloginedUserAccessToken(
                            okhttp3.Credentials.basic(
                                    BuildConfig.USER,
                                    BuildConfig.PASSWORD
                            ),
                            Constants.Api.GRANT_TYPE_CLIENT_CREDENTIALS
                    )
                    .doOnSuccess { (accessToken) -> preferences.setAccessToken(accessToken) }

    fun getNwQuizList(): Single<List<NwQuiz>> =
            quizApi
                    .getNwQuizList(Constants.Api.HEADER_PART_BEARER + preferences.getAccessToken())
                    .onErrorResumeNext { error: Throwable ->
                        if (error is HttpException) {
                            if (error.code() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                                return@onErrorResumeNext getAccessToken().flatMap<List<NwQuiz>> { getNwQuizList() }
                            }
                        }
                        Single.error<List<NwQuiz>>(error)
                    }
}