package com.scp.scpexam.controller.interactor

import com.scp.scpexam.Constants
import com.scp.scpexam.controller.api.ApiClient
import com.scp.scpexam.model.api.NwQuiz
import io.reactivex.Single
import io.reactivex.subjects.BehaviorSubject
import javax.inject.Inject

class LevelsInteractor @Inject constructor(
        private val apiClient: ApiClient
) {

//    fun downloadQuizzesPaging(): Single<List<NwQuiz>> =
//            Observable
//                    .range(0, Int.MAX_VALUE - 1)
//                    .concatMapSingle {
//                        apiClient.getNwQuizListPaging(it * Constants.LIMIT_PAGE_QUIZ, Constants.LIMIT_PAGE_QUIZ)
//                    }
//                    .takeUntil { t: List<NwQuiz> -> t.size / Constants.LIMIT_PAGE_QUIZ < 1 }
//                    .toList()
//                    .map { it.flatten() }

    fun downloadQuizzesPaging(): Single<List<NwQuiz>> {
        val pagSubject: BehaviorSubject<Int> = BehaviorSubject.createDefault(0)
        return pagSubject
                .switchMapSingle { offset ->
                    apiClient.getNwQuizListPaging(offset, Constants.LIMIT_PAGE_QUIZ)
                }
                .doOnNext {
                    if (it.isEmpty()) {
                        pagSubject.onComplete()
                    } else {
                        pagSubject.onNext(pagSubject.value!! + Constants.LIMIT_PAGE_QUIZ)
                    }
                }
                .toList()
                .map { it.flatten() }
    }
}
