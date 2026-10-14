package com.example.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

@JsonClass(generateAdapter = true)
data class McxPaymentInitRequest(
    @Json(name = "amount") val amount: Double,
    @Json(name = "currency") val currency: String = "AOA",
    @Json(name = "phone_number") val phoneNumber: String,
    @Json(name = "reference") val reference: String,
    @Json(name = "description") val description: String,
    @Json(name = "merchant_id") val merchantId: String = "MCX_MERCHANT_DEFAULT",
    @Json(name = "destination_iban") val destinationIban: String = "0006 0000 43818047301 44"
)

@JsonClass(generateAdapter = true)
data class McxPaymentInitResponse(
    @Json(name = "status") val status: String,
    @Json(name = "transaction_id") val transactionId: String,
    @Json(name = "reference") val reference: String,
    @Json(name = "message") val message: String,
    @Json(name = "expires_in_seconds") val expiresInSeconds: Int = 120,
    @Json(name = "destination_iban") val destinationIban: String? = null
)

@JsonClass(generateAdapter = true)
data class McxPaymentStatusResponse(
    @Json(name = "status") val status: String,
    @Json(name = "reference") val reference: String,
    @Json(name = "is_paid") val isPaid: Boolean,
    @Json(name = "transaction_id") val transactionId: String?,
    @Json(name = "message") val message: String?,
    @Json(name = "paid_at") val paidAt: Long?,
    @Json(name = "destination_iban") val destinationIban: String? = null
)

interface MulticaixaExpressApi {

    @POST("api/v1/payments/express")
    suspend fun initiateExpressPayment(
        @Header("Authorization") authorization: String,
        @Body request: McxPaymentInitRequest
    ): Response<McxPaymentInitResponse>

    @GET("api/v1/payments/express/{reference}")
    suspend fun checkExpressPaymentStatus(
        @Header("Authorization") authorization: String,
        @Path("reference") reference: String
    ): Response<McxPaymentStatusResponse>
}
