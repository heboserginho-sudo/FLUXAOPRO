package com.example.data.i18n

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import com.example.data.i18n.locales.EnTranslations
import com.example.data.i18n.locales.EsTranslations
import com.example.data.i18n.locales.FrTranslations
import com.example.data.i18n.locales.PtTranslations
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.MessageFormat

data class TranslationAuditReport(
    val totalKeys: Int,
    val ptCount: Int,
    val enCount: Int,
    val frCount: Int,
    val esCount: Int,
    val missingFrKeys: List<String>,
    val missingEsKeys: List<String>,
    val missingEnKeys: List<String>,
    val missingPtKeys: List<String>,
    val overallCompletenessPercentage: Double
)

object I18nService {

    private val _currentLanguage = MutableStateFlow(AppLanguage.detectDeviceLanguage())
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    private val catalogs: Map<AppLanguage, Map<String, String>> = mapOf(
        AppLanguage.PORTUGUESE to PtTranslations.strings,
        AppLanguage.ENGLISH to EnTranslations.strings,
        AppLanguage.FRENCH to FrTranslations.strings,
        AppLanguage.SPANISH to EsTranslations.strings
    )

    fun setLanguage(language: AppLanguage) {
        _currentLanguage.value = language
    }

    fun t(key: String, vararg args: Any, lang: AppLanguage = _currentLanguage.value): String {
        val targetCatalog = catalogs[lang] ?: catalogs[AppLanguage.ENGLISH] ?: emptyMap()
        val enCatalog = catalogs[AppLanguage.ENGLISH] ?: emptyMap()
        val ptCatalog = catalogs[AppLanguage.PORTUGUESE] ?: emptyMap()

        // Fallback chain: Target -> English -> Portuguese -> formatted key
        val rawText = targetCatalog[key]
            ?: enCatalog[key]
            ?: ptCatalog[key]
            ?: formatFallbackKey(key)

        return if (args.isNotEmpty()) {
            try {
                // Support both {0} index formatting and standard String.format
                MessageFormat.format(rawText, *args)
            } catch (_: Exception) {
                rawText
            }
        } else {
            rawText
        }
    }

    private fun formatFallbackKey(key: String): String {
        val lastSegment = key.substringAfterLast('.')
        return lastSegment.replace('_', ' ').replaceFirstChar { it.uppercase() }
    }

    fun auditMissingTranslations(): TranslationAuditReport {
        val allKeys = mutableSetOf<String>().apply {
            addAll(PtTranslations.strings.keys)
            addAll(EnTranslations.strings.keys)
            addAll(FrTranslations.strings.keys)
            addAll(EsTranslations.strings.keys)
        }

        val total = allKeys.size
        val missingPt = allKeys.filter { !PtTranslations.strings.containsKey(it) }
        val missingEn = allKeys.filter { !EnTranslations.strings.containsKey(it) }
        val missingFr = allKeys.filter { !FrTranslations.strings.containsKey(it) }
        val missingEs = allKeys.filter { !EsTranslations.strings.containsKey(it) }

        val totalTranslated = (PtTranslations.strings.size + EnTranslations.strings.size +
                FrTranslations.strings.size + EsTranslations.strings.size).toDouble()
        val totalExpected = (total * 4).toDouble()
        val percentage = if (totalExpected > 0) (totalTranslated / totalExpected) * 100.0 else 100.0

        return TranslationAuditReport(
            totalKeys = total,
            ptCount = PtTranslations.strings.size,
            enCount = EnTranslations.strings.size,
            frCount = FrTranslations.strings.size,
            esCount = EsTranslations.strings.size,
            missingFrKeys = missingFr,
            missingEsKeys = missingEs,
            missingEnKeys = missingEn,
            missingPtKeys = missingPt,
            overallCompletenessPercentage = percentage
        )
    }
}

val LocalI18nLanguage: ProvidableCompositionLocal<AppLanguage> = compositionLocalOf { AppLanguage.PORTUGUESE }

@Composable
@ReadOnlyComposable
fun stringRes(key: String, vararg args: Any): String {
    val lang = LocalI18nLanguage.current
    return I18nService.t(key, *args, lang = lang)
}
