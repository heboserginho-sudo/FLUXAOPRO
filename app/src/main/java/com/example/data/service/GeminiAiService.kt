package com.example.data.service

import android.util.Log
import com.example.BuildConfig
import com.example.data.local.entity.BudgetEntity
import com.example.data.local.entity.ClientEntity
import com.example.data.local.entity.InvoiceEntity
import com.example.data.model.AiChatMessage
import com.example.data.model.AiSender
import com.example.data.model.AppCurrency
import com.example.data.model.CompanySubscription
import com.example.domain.ai.LocalAiFinancialEngine
import com.example.ui.viewmodel.CashFlowUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiAiService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    companion object {
        private const val TAG = "GeminiAiService"
        private const val MODEL = "gemini-3.5-flash"
        private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"

        val MASTER_SYSTEM_INSTRUCTION = """
            Você é o FluxAO AI, o assistente financeiro inteligente integrado ao aplicativo FluxAO.

            Sua função é ajudar o utilizador a controlar, compreender e analisar o seu fluxo de caixa, receitas, despesas, lucros, contas a pagar, contas a receber, saldos e movimentações financeiras.

            OBJETIVO PRINCIPAL:
            Transformar os dados financeiros registrados no FluxAO em informações simples, úteis e acionáveis para o utilizador.

            DIRETRIZES FUNDAMENTAIS:
            1. ANALISAR O FLUXO DE CAIXA: Calcular entradas e saídas, saldo atual, comparar receitas e despesas, tendências.
            2. ANALISAR RECEITAS: Mostrar total de receitas por dia, semana ou mês; identificar principais fontes e variações.
            3. ANALISAR DESPESAS: Despesas por categoria, maiores gastos, detecção de aumentos e recorrências.
            4. CALCULAR RESULTADOS: Receita total, despesa total, resultado líquido (receitas - despesas), margem líquida, médias diárias.
            5. CONTAS A PAGAR E RECEBER: Informar valores pendentes, vencimentos próximos, atrasados e quem ainda deve dinheiro.
            6. RELATÓRIOS: Produzir relatórios executivos claros (diário, semanal, mensal, por categoria, cliente ou projeto).
            7. ALERTAS E INSIGHTS: Identificar aumentos anormais de despesas, redução de receitas, saldo baixo ou clientes em atraso.
            8. PREVISÕES: Estimativas baseadas exclusivamente nos dados históricos disponíveis. IMPORTANTE: Previsões são apenas estimativas matemáticas; nunca apresente uma previsão como certeza.
            9. SEGURANÇA FINANCEIRA: Nunca invente valores. Se uma informação não estiver disponível nos dados do FluxAO, diga claramente: "Não encontrei esse dado nos seus registros." Nunca altere, exclua ou crie movimentações financeiras via chat.
            10. PRIVACIDADE: Nunca solicite senhas, PINs, códigos Multicaixa Express, dados de cartão ou credenciais sensíveis.
            11. FORMA DE RESPONDER: Seja claro, objetivo e profissional. Use Kz como moeda padrão (ou a moeda informada no contexto). Utilize tabelas quando apropriado. Faça os cálculos diretamente.
            12. IDENTIDADE: Seu nome é FluxAO AI.
        """.trimIndent()
    }

    suspend fun queryAssistant(
        userMessage: String,
        chatHistory: List<AiChatMessage>,
        uiState: CashFlowUiState,
        invoices: List<InvoiceEntity>,
        clients: List<ClientEntity>,
        budgets: List<BudgetEntity>,
        subscription: CompanySubscription,
        currency: AppCurrency
    ): String = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Throwable) {
            ""
        }

        val hasValidApiKey = apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY"

        if (!hasValidApiKey) {
            Log.d(TAG, "No remote Gemini API key configured. Executing query with LocalAiFinancialEngine.")
            return@withContext LocalAiFinancialEngine.processQuery(
                query = userMessage,
                uiState = uiState,
                invoices = invoices,
                clients = clients,
                budgets = budgets,
                subscription = subscription,
                currency = currency
            )
        }

        try {
            val liveContext = LocalAiFinancialEngine.buildFinancialContext(
                uiState = uiState,
                invoices = invoices,
                clients = clients,
                budgets = budgets,
                subscription = subscription,
                currency = currency
            )

            val fullSystemPrompt = "$MASTER_SYSTEM_INSTRUCTION\n\n$liveContext"

            // Construct Gemini REST JSON payload
            val rootJson = JSONObject()

            // System instruction
            val sysInstructionObj = JSONObject()
            val sysParts = JSONArray()
            val sysPartText = JSONObject().put("text", fullSystemPrompt)
            sysParts.put(sysPartText)
            sysInstructionObj.put("parts", sysParts)
            rootJson.put("systemInstruction", sysInstructionObj)

            // Contents array (recent conversation history + current user message)
            val contentsArray = JSONArray()

            // Take up to last 6 messages to keep context concise
            val recentHistory = chatHistory.takeLast(6)
            for (msg in recentHistory) {
                val role = if (msg.sender == AiSender.USER) "user" else "model"
                val contentObj = JSONObject().put("role", role)
                val partsArr = JSONArray().put(JSONObject().put("text", msg.text))
                contentObj.put("parts", partsArr)
                contentsArray.put(contentObj)
            }

            // Current message
            val currentContent = JSONObject().put("role", "user")
            currentContent.put("parts", JSONArray().put(JSONObject().put("text", userMessage)))
            contentsArray.put(currentContent)

            rootJson.put("contents", contentsArray)

            // Generation config
            val genConfig = JSONObject()
            genConfig.put("temperature", 0.4)
            genConfig.put("topP", 0.95)
            rootJson.put("generationConfig", genConfig)

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = rootJson.toString().toRequestBody(mediaType)

            val url = "$BASE_URL/$MODEL:generateContent?key=$apiKey"
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseString = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                Log.w(TAG, "Gemini API returned error code ${response.code}: $responseString. Falling back to local engine.")
                return@withContext LocalAiFinancialEngine.processQuery(
                    query = userMessage,
                    uiState = uiState,
                    invoices = invoices,
                    clients = clients,
                    budgets = budgets,
                    subscription = subscription,
                    currency = currency
                )
            }

            val respJson = JSONObject(responseString)
            val candidates = respJson.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val replyText = parts?.optJSONObject(0)?.optString("text")

            if (!replyText.isNullOrBlank()) {
                replyText
            } else {
                LocalAiFinancialEngine.processQuery(
                    query = userMessage,
                    uiState = uiState,
                    invoices = invoices,
                    clients = clients,
                    budgets = budgets,
                    subscription = subscription,
                    currency = currency
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during Gemini AI call: ${e.message}. Using LocalAiFinancialEngine.", e)
            LocalAiFinancialEngine.processQuery(
                query = userMessage,
                uiState = uiState,
                invoices = invoices,
                clients = clients,
                budgets = budgets,
                subscription = subscription,
                currency = currency
            )
        }
    }
}
