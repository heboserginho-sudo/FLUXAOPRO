package com.example.ui.theme

import android.net.Uri
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.example.R
import com.example.data.local.entity.CompanyBrandingEntity
import com.example.data.model.AppThemeMode

@Composable
fun MyApplicationTheme(
    themeMode: AppThemeMode = AppThemeMode.LIGHT,
    darkTheme: Boolean = when (themeMode) {
        AppThemeMode.DARK -> true
        AppThemeMode.LIGHT -> false
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
    },
    customLogoUri: Uri? = null,
    companyBranding: CompanyBrandingEntity? = null,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current

    // Analisa a paleta da marca da empresa, logo customizado ou logotipo oficial do FluxAO
    val extractedPalette = remember(companyBranding, customLogoUri) {
        when {
            companyBranding != null -> {
                DynamicLogoThemeEngine.fromHexColors(
                    companyBranding.primaryColor,
                    companyBranding.secondaryColor,
                    companyBranding.accentColor
                )
            }
            customLogoUri != null -> {
                DynamicLogoThemeEngine.analyzeLogoUri(context, customLogoUri)
            }
            else -> {
                DynamicLogoThemeEngine.analyzeLogoResource(context, R.drawable.flux_ao_icon)
            }
        }
    }

    // Gera os Design Tokens completos (cores, escalas 50..950, espaçamentos, raios)
    val designTokens = remember(extractedPalette, darkTheme) {
        DynamicLogoThemeEngine.generateTokens(
            palette = extractedPalette,
            isDark = darkTheme
        )
    }

    // Converte os tokens para o ColorScheme oficial do Material Design 3
    val colorScheme = remember(designTokens) {
        DynamicLogoThemeEngine.toMaterialColorScheme(designTokens)
    }

    CompositionLocalProvider(
        LocalFluxAoDesignTokens provides designTokens
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
