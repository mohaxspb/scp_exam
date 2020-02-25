package com.scp.scpexam.controller.interactor

import io.reactivex.*
import com.scp.scpexam.controller.api.ApiClient
import com.scp.scpexam.controller.db.AppDatabase
import com.scp.scpexam.controller.manager.preference.MyPreferenceManager
import com.scp.scpexam.model.db.FinishedLevel
import com.scp.scpexam.model.db.QuizTransaction
import com.scp.scpexam.model.db.TransactionType
import com.scp.scpexam.model.db.UserRole
import timber.log.Timber
import javax.inject.Inject

class TransactionInteractor @Inject constructor(
        private val appDatabase: AppDatabase,
        private val apiClient: ApiClient,
        private val preferences: MyPreferenceManager
) {

    fun makeTransaction(quizId: Long?, transactionType: TransactionType, coinsAmount: Int?) =
            Single
                    .fromCallable {
                        val quizTransaction = QuizTransaction(
                                quizId = quizId,
                                transactionType = transactionType,
                                coinsAmount = coinsAmount
                        )
                        appDatabase.transactionDao().insert(quizTransaction)
//                                .also { Timber.d("LOCAL DB TRANSACTION :%s", appDatabase.transactionDao().getOneById(it)) }
                    }
                    .flatMapCompletable { quizTransactionId ->
                        if (preferences.getTrueAccessToken() == null) {
                            Completable.complete()
                        } else {
                            apiClient.addTransaction(
                                    quizId,
                                    transactionType,
                                    coinsAmount
                            )
                                    .doOnSuccess { nwQuizTransaction ->
                                        appDatabase.transactionDao().updateQuizTransactionExternalId(
                                                quizTransactionId = quizTransactionId,
                                                quizTransactionExternalId = nwQuizTransaction.id)
//                                        Timber.d("GET TRANSACTION BY ID : %s", appDatabase.transactionDao().getOneById(quizTransactionId))
                                    }
                                    .ignoreElement()
                                    .onErrorComplete()
                        }
                    }

    fun syncAllProgress() = Maybe.fromCallable {
        if (preferences.getTrueAccessToken() != null) {
            true
        } else {
            null
        }
    }
            .flatMapCompletable { syncScoreWithServer() }
            .andThen(sendProgressToServer())
            .andThen(syncFinishedLevelsMigration())
            .andThen(getProgressFromServer())
            .andThen(
                    apiClient.getNwUser()
                            .doOnSuccess { nwUser ->
                                appDatabase.userDao().getOneByRoleSync(UserRole.PLAYER)
                                        ?.apply {
                                            name = nwUser.fullName!!
                                            avatarUrl = nwUser.avatar
                                            score = nwUser.score
                                            appDatabase.userDao().update(this)
                                        }
                            }
                            .ignoreElement()
            )

    private fun sendProgressToServer() =
            appDatabase.transactionDao().getAllWithoutExternalIdByTypeWhenSendProgress(typesToSend)
                    .flatMap { listWithoutExtId ->
                        Observable.fromIterable(listWithoutExtId)
                                .flatMapSingle { transaction ->
                                    apiClient.addTransaction(
                                            transaction.quizId,
                                            transaction.transactionType,
                                            transaction.coinsAmount
                                    )
                                            .doOnSuccess { nwTransaction ->
                                                appDatabase.transactionDao().updateQuizTransactionExternalId(
                                                        transaction.id!!,
                                                        nwTransaction.id
                                                )
                                            }
                                }
                                .toList()
                    }
                    .ignoreElement()

    private fun getProgressFromServer() =
            apiClient.getNwQuizTransactionList()
                    .map { it.filter { nwQuizTransaction -> nwQuizTransaction.quizId != null } }
                    .doOnSuccess { nwTransactionList ->
                        nwTransactionList.forEach { nwQuizTransaction ->
                            val finishedLevel = appDatabase.finishedLevelsDao().getByQuizId(nwQuizTransaction.quizId!!)
                            val quizTransactionFromBd = appDatabase.transactionDao().getOneByQuizIdAndTransactionType(nwQuizTransaction.quizId!!, nwQuizTransaction.quizTransactionType)

                            when (nwQuizTransaction.quizTransactionType) {
                                TransactionType.NAME_WITH_PRICE, TransactionType.NAME_NO_PRICE -> {
                                    if (finishedLevel != null) {
//                                        Timber.d(" LevelFinished:%s", finishedLevel)
                                        appDatabase.finishedLevelsDao().update(finishedLevel.apply {
                                            scpNameFilled = true
                                            isLevelAvailable = true
                                        })
                                    } else {
//                                        Timber.d(" LevelFinished NULL:%s", finishedLevel)
                                        appDatabase.finishedLevelsDao().insert(FinishedLevel(
                                                quizId = nwQuizTransaction.quizId!!,
                                                scpNameFilled = true,
                                                scpNumberFilled = false,
                                                nameRedundantCharsRemoved = false,
                                                numberRedundantCharsRemoved = false,
                                                isLevelAvailable = true
                                        ))
                                    }
                                }

                                TransactionType.NUMBER_WITH_PRICE, TransactionType.NUMBER_NO_PRICE -> {
                                    if (finishedLevel != null) {
//                                        Timber.d(" LevelFinished:%s", finishedLevel)
                                        appDatabase.finishedLevelsDao().update(finishedLevel.apply {
                                            scpNumberFilled = true
                                            isLevelAvailable = true
                                        })
                                    } else {
//                                        Timber.d(" LevelFinished NULL:%s", finishedLevel)
                                        appDatabase.finishedLevelsDao().insert(FinishedLevel(
                                                quizId = nwQuizTransaction.quizId!!,
                                                scpNameFilled = false,
                                                scpNumberFilled = true,
                                                nameRedundantCharsRemoved = false,
                                                numberRedundantCharsRemoved = false,
                                                isLevelAvailable = true
                                        ))
                                    }
                                }

                                TransactionType.NAME_CHARS_REMOVED -> {
                                    if (finishedLevel != null) {
//                                        Timber.d(" LevelFinished:%s", finishedLevel)
                                        appDatabase.finishedLevelsDao().update(finishedLevel.apply {
                                            nameRedundantCharsRemoved = true
                                            isLevelAvailable = true
                                        })
                                    } else {
//                                        Timber.d(" LevelFinished NULL:%s", finishedLevel)
                                        appDatabase.finishedLevelsDao().insert(FinishedLevel(
                                                quizId = nwQuizTransaction.quizId!!,
                                                scpNameFilled = false,
                                                scpNumberFilled = false,
                                                nameRedundantCharsRemoved = true,
                                                numberRedundantCharsRemoved = false,
                                                isLevelAvailable = true
                                        ))
                                    }
                                }

                                TransactionType.NUMBER_CHARS_REMOVED -> {
                                    if (finishedLevel != null) {
//                                        Timber.d(" LevelFinished:%s", finishedLevel)
                                        appDatabase.finishedLevelsDao().update(finishedLevel.apply {
                                            numberRedundantCharsRemoved = true
                                            isLevelAvailable = true
                                        })
                                    } else {
//                                        Timber.d(" LevelFinished NULL:%s", finishedLevel)
                                        appDatabase.finishedLevelsDao().insert(FinishedLevel(
                                                quizId = nwQuizTransaction.quizId!!,
                                                scpNameFilled = false,
                                                scpNumberFilled = false,
                                                nameRedundantCharsRemoved = false,
                                                numberRedundantCharsRemoved = true,
                                                isLevelAvailable = true
                                        ))
                                    }
                                }

                                TransactionType.LEVEL_ENABLE_FOR_COINS -> {
                                    if (finishedLevel != null) {
//                                        Timber.d(" LevelFinished:%s", finishedLevel)
                                        appDatabase.finishedLevelsDao().update(finishedLevel.apply {
                                            isLevelAvailable = true
                                        })
                                    } else {
//                                        Timber.d(" LevelFinished NULL:%s", finishedLevel)
                                        appDatabase.finishedLevelsDao().insert(FinishedLevel(
                                                quizId = nwQuizTransaction.quizId!!,
                                                scpNameFilled = false,
                                                scpNumberFilled = false,
                                                nameRedundantCharsRemoved = false,
                                                numberRedundantCharsRemoved = false,
                                                isLevelAvailable = true
                                        ))
                                    }
                                }

                                else -> Timber.d("Type is not LevelFinished:%s", finishedLevel)
                            }

                            if (quizTransactionFromBd == null) {
                                appDatabase.transactionDao().insert(QuizTransaction(
                                        quizId = nwQuizTransaction.quizId,
                                        externalId = nwQuizTransaction.id,
                                        coinsAmount = nwQuizTransaction.coinsAmount,
                                        createdOnClient = nwQuizTransaction.createdOnClient,
                                        transactionType = nwQuizTransaction.quizTransactionType
                                ))
//                                Timber.d("Inserted transaction :%s", quizTransactionFromBd)
                            } else
                                if (quizTransactionFromBd.externalId == null) {
                                    appDatabase.transactionDao().updateQuizTransactionExternalId(quizTransactionFromBd.id!!, nwQuizTransaction.id)
//                                    Timber.d("Inserted transaction when ExtId == null :%s", quizTransactionFromBd)
                                }
                        }
                    }
                    .ignoreElement()
                    .onErrorComplete()

    private fun syncScoreWithServer() =
            Maybe.fromCallable {
                val updateSyncTransaction = appDatabase.transactionDao().getOneByTypeNoReactive(TransactionType.UPDATE_SYNC)
                Timber.d("appDatabase.transactionDao().getOneByTypeNoReactive(TransactionType.UPDATE_SYNC) : %s", appDatabase.transactionDao().getOneByTypeNoReactive(TransactionType.UPDATE_SYNC))
                Timber.d("All transactions : %s", appDatabase.transactionDao().getAllList())
                return@fromCallable if (updateSyncTransaction?.externalId == null) {
                    updateSyncTransaction
                } else {
                    null
                }
            }
                    .flatMapCompletable { quizTransaction ->
                        apiClient.addTransaction(
                                quizTransaction.quizId,
                                quizTransaction.transactionType,
                                quizTransaction.coinsAmount
                        )
                                .doOnSuccess { nwQuizTransaction ->
                                    appDatabase.transactionDao().updateQuizTransactionExternalId(
                                            quizTransactionId = quizTransaction.id!!,
                                            quizTransactionExternalId = nwQuizTransaction.id)
//                                    Timber.d("GET TRANSACTION BY ID : %s", appDatabase.transactionDao().getOneById(quizTransaction.id))
                                }
                                .ignoreElement()
                                .onErrorComplete()
                    }


    /**
     * получаем все finishedLevel() ,  getAll()
     * отфильтровываем все с levelAvailable = true ,  filter()
     * преобразовываем список finishedLevel в список transactions, map()
     * пишем в БД, map()
     * отправляем на сервер, flatmap()
     * обновляем externalId doOnSuccess()
     */
    private fun syncFinishedLevelsMigration() =
            appDatabase.finishedLevelsDao().getAllSingle()
                    .map { finishedLevels -> finishedLevels.filter { it.isLevelAvailable } }
                    .map { levelsAvailableTrue ->
                        val finishedLevelsToTransactions = mutableListOf<QuizTransaction>()
                        levelsAvailableTrue.forEach { levelAvailableFinishedLevel ->
                            val quizId = levelAvailableFinishedLevel.quizId
                            if (levelAvailableFinishedLevel.nameRedundantCharsRemoved
                                    && appDatabase.transactionDao().getOneByQuizIdAndTransactionType(quizId, TransactionType.NAME_CHARS_REMOVED) == null
                                    && appDatabase.transactionDao().getOneByQuizIdAndTransactionType(quizId, TransactionType.NAME_CHARS_REMOVED_MIGRATION) == null) {
                                finishedLevelsToTransactions.add(quizTransactionForMigration(quizId, TransactionType.NAME_CHARS_REMOVED_MIGRATION))
                            }
                            if (levelAvailableFinishedLevel.numberRedundantCharsRemoved
                                    && appDatabase.transactionDao().getOneByQuizIdAndTransactionType(quizId, TransactionType.NUMBER_CHARS_REMOVED) == null
                                    && appDatabase.transactionDao().getOneByQuizIdAndTransactionType(quizId, TransactionType.NUMBER_CHARS_REMOVED_MIGRATION) == null) {
                                finishedLevelsToTransactions.add(quizTransactionForMigration(quizId, TransactionType.NUMBER_CHARS_REMOVED_MIGRATION))
                            }
                            if (levelAvailableFinishedLevel.scpNameFilled
                                    && appDatabase.transactionDao().getOneByQuizIdAndTransactionType(quizId, TransactionType.NAME_WITH_PRICE) == null
                                    && appDatabase.transactionDao().getOneByQuizIdAndTransactionType(quizId, TransactionType.NAME_NO_PRICE) == null
                                    && appDatabase.transactionDao().getOneByQuizIdAndTransactionType(quizId, TransactionType.NAME_ENTERED_MIGRATION) == null) {
                                finishedLevelsToTransactions.add(quizTransactionForMigration(quizId, TransactionType.NAME_ENTERED_MIGRATION))
                            }
                            if (levelAvailableFinishedLevel.scpNumberFilled
                                    && appDatabase.transactionDao().getOneByQuizIdAndTransactionType(quizId, TransactionType.NUMBER_WITH_PRICE) == null
                                    && appDatabase.transactionDao().getOneByQuizIdAndTransactionType(quizId, TransactionType.NUMBER_NO_PRICE) == null
                                    && appDatabase.transactionDao().getOneByQuizIdAndTransactionType(quizId, TransactionType.NUMBER_ENTERED_MIGRATION) == null) {
                                finishedLevelsToTransactions.add(quizTransactionForMigration(quizId, TransactionType.NUMBER_ENTERED_MIGRATION))
                            }
                            if (appDatabase.transactionDao().getOneByQuizIdAndTransactionType(quizId, TransactionType.LEVEL_ENABLE_FOR_COINS) == null
                                    && appDatabase.transactionDao().getOneByQuizIdAndTransactionType(quizId, TransactionType.LEVEL_AVAILABLE_MIGRATION) == null) {
                                finishedLevelsToTransactions.add(quizTransactionForMigration(quizId, TransactionType.LEVEL_AVAILABLE_MIGRATION))
                            }
                        }
//                        Timber.d("finishedLevelsToTransactions.toList() : %s", finishedLevelsToTransactions.toList())
                        return@map finishedLevelsToTransactions
                    }
                    .map { quizTransactionList ->
                        //                        Timber.d("quizTransactionList :%s", quizTransactionList)
                        appDatabase.transactionDao().insertQuizTransactionList(quizTransactionList)
                    }
                    .flatMap { localIds ->
                        Flowable
                                .fromIterable(localIds)
                                .flatMapSingle {
                                    apiClient
                                            .addTransaction(
                                                    quizId = appDatabase.transactionDao().getOneById(it).quizId,
                                                    typeTransaction = appDatabase.transactionDao().getOneById(it).transactionType,
                                                    coinsAmount = appDatabase.transactionDao().getOneById(it).coinsAmount
                                            )
                                            .doOnSuccess { nwQuizTransaction ->
                                                //Timber.d("OnSuccess :%s", nwQuizTransaction)
                                                appDatabase.transactionDao().updateQuizTransactionExternalId(
                                                        quizTransactionId = it,
                                                        quizTransactionExternalId = nwQuizTransaction.id)
                                                //Timber.d("GET TRANSACTION BY ID : %s", appDatabase.transactionDao().getOneById(it))
                                            }
                                }
                                .toList()
                    }
                    .ignoreElement()
                    .onErrorComplete()

    private fun quizTransactionForMigration(quizId: Long, transactionType: TransactionType) =
            QuizTransaction(
                    coinsAmount = 0,
                    quizId = quizId,
                    transactionType = transactionType
            )

    companion object {
        /**
         * Enum specification for SqlLite(room) dont use arrayList or list or some another collection type
         */
        val typesToSend = arrayOf(
                TransactionType.ADV_WATCHED,
                TransactionType.LEVEL_ENABLE_FOR_COINS,
                TransactionType.NAME_WITH_PRICE,
                TransactionType.NAME_NO_PRICE,
                TransactionType.NAME_CHARS_REMOVED,
                TransactionType.NUMBER_WITH_PRICE,
                TransactionType.NUMBER_NO_PRICE,
                TransactionType.NUMBER_CHARS_REMOVED,
                TransactionType.ADV_BUY_NEVER_SHOW
        )
    }
}