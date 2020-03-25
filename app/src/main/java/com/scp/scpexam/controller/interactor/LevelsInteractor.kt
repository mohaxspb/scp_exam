package com.scp.scpexam.controller.interactor

import com.scp.scpexam.controller.api.ApiClient
import com.scp.scpexam.model.api.NwQuiz
import io.reactivex.Single
import javax.inject.Inject

class LevelsInteractor @Inject constructor(
        private val apiClient: ApiClient
) {

    fun downloadQuizzesPaging(): Single<List<NwQuiz>> {


    }

}