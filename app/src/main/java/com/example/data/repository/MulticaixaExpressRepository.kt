package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.api.McxPaymentInitRequest
import com.example.data.api.MulticaixaExpressApi
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.random.Random

data class McxPaymentInitResult(
    val success: Boolean,
    val reference: String,
    val transactionId: String,
    val message: String,
    val targetPhone: String,
    val amount: Double,
    val isSandbox: Boolean,
    val destinationIban: String = MulticaixaExpressRepository.DEFAULT_DESTINATION_IBAN,
    val destinationBank: String = MulticaixaExpressRepository.DESTINATION_BANK_NAME
)

data class McxStatusResult(
    val isPaid: Boolean,
    val status: String,
    val message: String,
    val transactionId: String? = null,
    val destinationIban: String = MulticaixaExpressRepository.DEFAULT_DESTINATION_IBAN
)

class MulticaixaExpressRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("mcx_api_prefs", Context.MODE_PRIVATE)

    var gatewayUrl: String
        get() = prefs.getString(KEY_GATEWAY_URL, DEFAULT_GATEWAY_URL) ?: DEFAULT_GATEWAY_URL
        set(value) = prefs.edit().putString(KEY_GATEWAY_URL, value).apply()

    var apiKey: String
        get() = prefs.getString(KEY_API_KEY, "") ?: ""
        set(value) = prefs.edit().putString(KEY_API_KEY, value).apply()

    var merchantId: String
        get() = prefs.getString(KEY_MERCHANT_ID, "MCX_FLUXO_EXP") ?: "MCX_FLUXO_EXP"
        set(value) = prefs.edit().putString(KEY_MERCHANT_ID, value).apply()

    var destinationIban: String
        get() = prefs.getString(KEY_DESTINATION_IBAN, DEFAULT_DESTINATION_IBAN) ?: DEFAULT_DESTINATION_IBAN
        set(value) = prefs.edit().putString(KEY_DESTINATION_IBAN, value).apply()

    var isSandboxMode: Boolean
        get() = prefs.getBoolean(KEY_SANDBOX, true) // Enabled by default for safe test evaluation
        set(value) = prefs.edit().putBoolean(KEY_SANDBOX, value).apply()

    val destinationBankName: String
        get() = DESTINATION_BANK_NAME

    val formattedDestinationIban: String
        get() = if (destinationIban.contains("AO06")) destinationIban else "AO06 $destinationIban"

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    private fun getApiService(baseUrl: String): MulticaixaExpressApi {
        val cleanUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        return Retrofit.Builder()
            .baseUrl(cleanUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(MulticaixaExpressApi::class.java)
    }

    suspend fun initiatePayment(
        phoneNumber: String,
        amount: Double = 5000.0,
        companyName: String
    ): Result<McxPaymentInitResult> = withContext(Dispatchers.IO) {
        val cleanPhone = phoneNumber.replace("\\s".toRegex(), "").replace("-", "")
        val randomSuffix = Random.nextInt(100000, 999999)
        val reference = "MCX-${System.currentTimeMillis() % 1000000}-$randomSuffix"

        // If sandbox mode is explicitly active or no custom API key is present yet
        if (isSandboxMode || apiKey.isBlank()) {
            delay(1200) // Realistic network roundtrip
            val txId = "TX-MCX-${UUID.randomUUID().toString().take(8).uppercase()}"
            return@withContext Result.success(
                McxPaymentInitResult(
                    success = true,
                    reference = reference,
                    transactionId = txId,
                    message = "Pedido de débito direto de 5.000 Kz para liquidação na conta IBAN $destinationIban ($DESTINATION_BANK_NAME). Confirme no app Multicaixa Express do telemóvel $cleanPhone.",
                    targetPhone = cleanPhone,
                    amount = amount,
                    isSandbox = true,
                    destinationIban = destinationIban,
                    destinationBank = DESTINATION_BANK_NAME
                )
            )
        }

        try {
            val api = getApiService(gatewayUrl)
            val request = McxPaymentInitRequest(
                amount = amount,
                currency = "AOA",
                phoneNumber = cleanPhone,
                reference = reference,
                description = "Assinatura Mensal App Fluxo - $companyName",
                merchantId = merchantId,
                destinationIban = destinationIban
            )
            val response = api.initiateExpressPayment(
                authorization = "Bearer $apiKey",
                request = request
            )

            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                Result.success(
                    McxPaymentInitResult(
                        success = true,
                        reference = body.reference,
                        transactionId = body.transactionId,
                        message = body.message.ifBlank { "Autorização MCX enviada com sucesso ao terminal móvel para crédito na conta IBAN $destinationIban." },
                        targetPhone = cleanPhone,
                        amount = amount,
                        isSandbox = false,
                        destinationIban = body.destinationIban ?: destinationIban,
                        destinationBank = DESTINATION_BANK_NAME
                    )
                )
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Erro na comunicação com Gateway Multicaixa Express (código ${response.code()})"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Falha ao comunicar com o servidor Multicaixa Express: ${e.localizedMessage ?: "Erro de rede"}"))
        }
    }

    suspend fun verifyPaymentStatus(
        reference: String,
        simulatedDelayCount: Int = 0
    ): Result<McxStatusResult> = withContext(Dispatchers.IO) {
        if (isSandboxMode || apiKey.isBlank()) {
            delay(800)
            // In sandbox, after polling 2-3 times (or after 5 seconds), simulate user entered PIN in Multicaixa Express app
            if (simulatedDelayCount >= 2) {
                return@withContext Result.success(
                    McxStatusResult(
                        isPaid = true,
                        status = "PAID",
                        message = "Pagamento de 5.000 Kz liquidado com sucesso na conta destino IBAN $destinationIban ($DESTINATION_BANK_NAME)!",
                        transactionId = "TX-MCX-APPROVED-${System.currentTimeMillis() % 10000}",
                        destinationIban = destinationIban
                    )
                )
            } else {
                return@withContext Result.success(
                    McxStatusResult(
                        isPaid = false,
                        status = "PENDING",
                        message = "Aguardando confirmação do PIN no Multicaixa Express para transferência ao IBAN $destinationIban...",
                        destinationIban = destinationIban
                    )
                )
            }
        }

        try {
            val api = getApiService(gatewayUrl)
            val response = api.checkExpressPaymentStatus(
                authorization = "Bearer $apiKey",
                reference = reference
            )

            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                Result.success(
                    McxStatusResult(
                        isPaid = body.isPaid,
                        status = body.status,
                        message = body.message ?: if (body.isPaid) "Pagamento confirmado com crédito no IBAN $destinationIban" else "Aguardando autorização",
                        transactionId = body.transactionId,
                        destinationIban = body.destinationIban ?: destinationIban
                    )
                )
            } else {
                Result.failure(Exception("Não foi possível consultar o estado da transação MCX: código ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    companion object {
        const val DEFAULT_GATEWAY_URL = "https://api.multicaixa-express.ao/"
        const val DEFAULT_DESTINATION_IBAN = "0006 0000 43818047301 44"
        const val FORMATTED_DESTINATION_IBAN = "AO06 0006 0000 4381 8047 3014 4"
        const val DESTINATION_BANK_NAME = "Banco de Fomento Angola (BFA)"
        const val DESTINATION_BANK_CODE = "0006"

        private const val KEY_GATEWAY_URL = "mcx_gateway_url"
        private const val KEY_API_KEY = "mcx_api_key"
        private const val KEY_MERCHANT_ID = "mcx_merchant_id"
        private const val KEY_SANDBOX = "mcx_sandbox_mode"
        private const val KEY_DESTINATION_IBAN = "mcx_destination_iban"
    }
}
