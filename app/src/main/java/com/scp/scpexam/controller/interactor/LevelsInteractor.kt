package com.scp.scpexam.controller.interactor

import com.scp.scpexam.Constants
import com.scp.scpexam.controller.api.ApiClient
import com.scp.scpexam.model.api.NwQuiz
import io.reactivex.Observable
import io.reactivex.Single
import timber.log.Timber
import javax.inject.Inject

class LevelsInteractor @Inject constructor(
        private val apiClient: ApiClient
) {

    val quizList = mutableListOf<NwQuiz>()

    fun downloadQuizzesPaging(): Single<List<NwQuiz>> =
                    Observable
                            .range(0, Int.MAX_VALUE - 1)
                            .concatMap {
                                apiClient.getNwQuizListPaging(it * Constants.LIMIT_PAGE_QUIZ, Constants.LIMIT_PAGE_QUIZ)
                                        .toObservable()
                                        .doOnNext { Timber.d(" doOnNext list nwQuiz: $it") }
                                        .takeUntil {
                                            t: List<NwQuiz> -> t.size % Constants.LIMIT_PAGE_QUIZ < 1
                                        }
                            }
                            .doOnNext { Timber.d("Do on next 2 : $it") }
                            .scan {
                                t1: List<NwQuiz>, t2: List<NwQuiz> ->
                                quizList.addAll(t1)
                                quizList.addAll(t2)
                                return@scan quizList
                            }
                            .doOnNext { Timber.d("Do on next 3 : $it") }
                            .lastOrError()

}

