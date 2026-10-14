package com.example.ui.theme

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils
import com.example.R
import java.io.InputStream
import kotlin.math.max
import kotlin.math.min

/**
 * Paleta de cores oficial do FluxAO — Redesign Fintech & SaaS.
 */
data class ExtractedLogoPalette(
    val primary: Color,
    val secondary: Color,
    val accent: Color,
    val darkTone: Color,
    val lightTone: Color,
    val onPrimary: Color,
    val backgroundDark: Color,
    val backgroundLight: Color,
    val surfaceDark: Color,
    val surfaceLight: Color,
    val borderDark: Color,
    val borderLight: Color
)

/**
 * Motor central de identidade visual do FluxAO.
 *
 * Aplica o sistema cromático oficial:
 * - Roxo principal: #6425F5
 * - Roxo escuro: #351078
 * - Azul-marinho: #11152F
 * - Branco: #FFFFFF
 * - Fundo claro: #F4F5FC
 * - Cinzento para superfícies: #E9EAF4
 * - Texto principal: #17172B
 * - Texto secundário: #73758A
 * - Verde receitas: #12A879
 * - Vermelho despesas: #E5485B
 * - Dourado recompensas: #F5B82E
 */
object DynamicLogoThemeEngine {

    // Paleta padrão oficial do FluxAO Redesign
    val DEFAULT_FLUXAO_PALETTE = ExtractedLogoPalette(
        primary = FluxPurplePrimary,          // #6425F5 - Roxo principal
        secondary = FluxPurpleDark,           // #351078 - Roxo escuro de apoio
        accent = FluxGreenSuccess,            // #12A879 - Sucesso financeiro
        darkTone = FluxNavy,                  // #11152F - Azul-marinho profundo
        lightTone = FluxWhite,                // #FFFFFF - Branco puro
        onPrimary = FluxWhite,                // Contraste legível sobre roxo
        backgroundDark = FluxNavy,            // #11152F - Fundo principal dark
        backgroundLight = FluxLightBackground,// #F4F5FC - Fundo principal light
        surfaceDark = FluxNavyCard,           // #191F3E - Superfície de cartões dark
        surfaceLight = FluxWhite,             // #FFFFFF - Superfície clara
        borderDark = FluxNavyBorder,          // #2E386B - Linhas de precisão dark
        borderLight = FluxSurfaceBorder       // #E2E4F0 - Linhas claras
    )

    fun analyzeLogoBitmap(bitmap: Bitmap): ExtractedLogoPalette {
        return DEFAULT_FLUXAO_PALETTE
    }

    fun fromHexColors(primaryHex: String, secondaryHex: String, accentHex: String): ExtractedLogoPalette {
        fun parseColor(hex: String, fallback: Color): Color {
            return try {
                val clean = hex.removePrefix("#").trim()
                if (clean.length == 6 || clean.length == 8) {
                    Color(android.graphics.Color.parseColor("#$clean"))
                } else fallback
            } catch (_: Exception) {
                fallback
            }
        }

        val primary = parseColor(primaryHex, FluxPurplePrimary)
        val secondary = parseColor(secondaryHex, FluxPurpleDark)
        val accent = parseColor(accentHex, FluxGreenSuccess)
        val onPrimary = getAccessibleTextColor(primary)

        return DEFAULT_FLUXAO_PALETTE.copy(
            primary = primary,
            secondary = secondary,
            accent = accent,
            onPrimary = onPrimary
        )
    }

    fun analyzeLogoResource(context: Context, resId: Int = R.drawable.flux_ao_icon): ExtractedLogoPalette {
        return DEFAULT_FLUXAO_PALETTE
    }

    fun analyzeLogoUri(context: Context, uri: Uri): ExtractedLogoPalette {
        return DEFAULT_FLUXAO_PALETTE
    }

    /**
     * Gera os Design Tokens completos para o modo Claro ou Escuro.
     */
    fun generateTokens(
        palette: ExtractedLogoPalette = DEFAULT_FLUXAO_PALETTE,
        isDark: Boolean = false
    ): FluxAoDesignTokens {
        val primaryScale = ColorScaleGenerator.createScale(palette.primary)
        val secondaryScale = ColorScaleGenerator.createScale(palette.secondary)
        val neutralScale = ColorScaleGenerator.createNeutralScale()

        val colors = if (isDark) {
            FluxAoColors(
                primary = FluxPurpleLight,
                primaryLight = Color(0xFFA57EFF),
                primaryDark = FluxPurplePrimary,
                primaryContainer = FluxPurpleContainerDark,
                onPrimary = FluxWhite,
                onPrimaryContainer = Color(0xFFD6C8FF),

                secondary = palette.secondary,
                secondaryLight = FluxPurpleLight,
                secondaryDark = FluxPurpleDark,
                secondaryContainer = FluxNavyElevated,
                onSecondary = FluxWhite,

                accent = palette.accent,

                background = FluxNavy,
                onBackground = FluxWhite,
                surface = FluxNavyCard,
                onSurface = FluxWhite,
                card = FluxNavyCard,
                cardElevated = FluxNavyElevated,

                text = FluxWhite,
                textSecondary = Color(0xFFA0A2B8),
                textMuted = Color(0xFF707390),

                border = FluxNavyBorder,
                borderSubtle = FluxNavyBorderSubtle,

                success = FluxGreenSuccess,
                onSuccess = FluxNavy,
                warning = FluxGoldRewards,
                onWarning = FluxNavy,
                error = FluxRedExpense,
                onError = FluxWhite,

                primaryScale = primaryScale,
                secondaryScale = secondaryScale,
                neutralScale = neutralScale
            )
        } else {
            FluxAoColors(
                primary = FluxPurplePrimary,
                primaryLight = FluxPurpleLight,
                primaryDark = FluxPurpleDark,
                primaryContainer = FluxPurpleContainer,
                onPrimary = FluxWhite,
                onPrimaryContainer = FluxPurpleDark,

                secondary = FluxPurpleDark,
                secondaryLight = Color(0xFF4C18AB),
                secondaryDark = Color(0xFF220853),
                secondaryContainer = FluxSurfaceGray,
                onSecondary = FluxWhite,

                accent = palette.accent,

                background = FluxLightBackground,
                onBackground = FluxTextPrimary,
                surface = FluxWhite,
                onSurface = FluxTextPrimary,
                card = FluxWhite,
                cardElevated = FluxSurfaceGray,

                text = FluxTextPrimary,
                textSecondary = FluxTextSecondary,
                textMuted = FluxTextMuted,

                border = FluxSurfaceBorder,
                borderSubtle = Color(0xFFEDEDF7),

                success = FluxGreenSuccess,
                onSuccess = FluxWhite,
                warning = FluxGoldRewards,
                onWarning = FluxNavy,
                error = FluxRedExpense,
                onError = FluxWhite,

                primaryScale = primaryScale,
                secondaryScale = secondaryScale,
                neutralScale = neutralScale
            )
        }

        return FluxAoDesignTokens(colors = colors, isDark = isDark)
    }

    /**
     * Converte os tokens de design para o ColorScheme do Material Design 3.
     */
    fun toMaterialColorScheme(tokens: FluxAoDesignTokens): ColorScheme {
        val c = tokens.colors
        return if (tokens.isDark) {
            darkColorScheme(
                primary = c.primary,
                onPrimary = c.onPrimary,
                primaryContainer = c.primaryContainer,
                onPrimaryContainer = c.onPrimaryContainer,
                secondary = c.secondary,
                onSecondary = c.onSecondary,
                secondaryContainer = c.secondaryContainer,
                tertiary = c.accent,
                background = c.background,
                onBackground = c.onBackground,
                surface = c.surface,
                onSurface = c.onSurface,
                surfaceVariant = c.cardElevated,
                onSurfaceVariant = c.textSecondary,
                outline = c.border,
                outlineVariant = c.borderSubtle,
                error = c.error,
                onError = c.onError
            )
        } else {
            lightColorScheme(
                primary = c.primary,
                onPrimary = c.onPrimary,
                primaryContainer = c.primaryContainer,
                onPrimaryContainer = c.onPrimaryContainer,
                secondary = c.secondary,
                onSecondary = c.onSecondary,
                secondaryContainer = c.secondaryContainer,
                tertiary = c.accent,
                background = c.background,
                onBackground = c.onBackground,
                surface = c.surface,
                onSurface = c.onSurface,
                surfaceVariant = c.cardElevated,
                onSurfaceVariant = c.textSecondary,
                outline = c.border,
                outlineVariant = c.borderSubtle,
                error = c.error,
                onError = c.onError
            )
        }
    }

    fun calculateContrast(foreground: Color, background: Color): Double {
        val fgLum = ColorUtils.calculateLuminance(foreground.toArgb())
        val bgLum = ColorUtils.calculateLuminance(background.toArgb())
        return (max(fgLum, bgLum) + 0.05) / (min(fgLum, bgLum) + 0.05)
    }

    fun getAccessibleTextColor(backgroundColor: Color): Color {
        val contrastWithWhite = calculateContrast(FluxWhite, backgroundColor)
        val contrastWithDark = calculateContrast(FluxNavy, backgroundColor)
        return if (contrastWithDark >= contrastWithWhite && contrastWithDark >= 4.5) {
            FluxNavy
        } else {
            FluxWhite
        }
    }
}
