package com.scp.scpexam.controller.interactor

import com.scp.scpexam.Constants
import com.scp.scpexam.controller.api.ApiClient
import com.scp.scpexam.model.api.NwQuiz
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber
import javax.inject.Inject

class LevelsInteractor @Inject constructor(
        private val apiClient: ApiClient
) {

    val pagSubject: BehaviorSubject<Pair<Int, Int>> = BehaviorSubject.create()

    fun downloadQuizzesPaging(): Single<List<NwQuiz>> =
                    Observable
                            .range(0, Int.MAX_VALUE - 1)
                            .doOnNext{Timber.d("count: $it")}
                            .concatMap {
                                apiClient.getNwQuizListPaging(it * Constants.LIMIT_PAGE_QUIZ, Constants.LIMIT_PAGE_QUIZ)
                                        .toObservable()
                                        .doOnNext { Timber.d(" doOnNext list nwQuiz: ${it.size}") }

                            }
                            .takeUntil { t: List<NwQuiz> -> t.size / Constants.LIMIT_PAGE_QUIZ < 1 }
                            .doOnNext { Timber.d("Do on next 2 : ${it.size}") }
                            .toList()
                            .map { it.flatten() }
                            .doOnSuccess { Timber.d("Do on next 3 : ${it.size}") }

//    fun subj(){
//
//        pagSubject
//                .switchMap { t: Pair<Int, Int> ->
//                    apiClient.getNwQuizListPaging(t.first, t.second)
//                            .toObservable()
//                }
//
//                        .subscribeBy (
//                            onNext = ,
//                                onComplete = ,
//                                onError =
//                        )
//                }
//    }

}

