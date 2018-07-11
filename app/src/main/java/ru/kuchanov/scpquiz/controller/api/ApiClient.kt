package ru.kuchanov.scpquiz.controller.api

import io.reactivex.Single
import retrofit2.Retrofit
import ru.kuchanov.scpquiz.App
import javax.inject.Inject


class ApiClient @Inject constructor(
    private val retrofit: Retrofit
) {

    val vpsApi = retrofit.create(VpsApi::class.java)

    fun validateInApp(sku: String, purchaseToken: String): Single<Int> = vpsApi.validatePurchase(
        false,
        App.INSTANCE.packageName,
        sku,
        purchaseToken
    ).map { it.status }
}