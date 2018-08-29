package ru.kuchanov.scpquiz.controller.api.response

import android.support.annotation.IntDef

data class PurchaseValidateResponse(
    @field:PurchaseStatus
    val status: Int
)

@IntDef(VALID, INVALID, GOOGLE_SERVER_ERROR)
@Retention(AnnotationRetention.SOURCE)
annotation class PurchaseStatus

const val VALID = 0
const val INVALID = 1
const val GOOGLE_SERVER_ERROR = 2