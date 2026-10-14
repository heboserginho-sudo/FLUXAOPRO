package com.example.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils

/**
 * Escala tonal completa (50..950) para uma cor da marca,
 * onde 500 representa a tonalidade de referência principal.
 */
data class ColorScale(
    val c50: Color,
    val c100: Color,
    val c200: Color,
    val c300: Color,
    val c400: Color,
    val c500: Color, // Cor principal da marca
    val c600: Color,
    val c700: Color,
    val c800: Color,
    val c900: Color,
    val c950: Color
) {
    operator fun get(step: Int): Color = when (step) {
        50 -> c50
        100 -> c100
        200 -> c200
        300 -> c300
        400 -> c400
        500 -> c500
        600 -> c600
        700 -> c700
        800 -> c800
        900 -> c900
        950 -> c950
        else -> c500
    }
}

/**
 * Utilitários para geração algorítmica de escalas tonais baseadas em HSL/LAB.
 */
object ColorScaleGenerator {

    /**
     * Gera uma escala de 50 a 950 a partir de uma cor base (usada como 500).
     */
    fun createScale(baseColor: Color): ColorScale {
        val hsl = FloatArray(3)
        ColorUtils.colorToHSL(baseColor.toArgb(), hsl)
        val h = hsl[0]
        val s = hsl[1]
        val l = hsl[2]

        fun hslColor(lightness: Float): Color {
            val clampedL = lightness.coerceIn(0.02f, 0.98f)
            val argb = ColorUtils.HSLToColor(floatArrayOf(h, s, clampedL))
            return Color(argb)
        }

        return ColorScale(
            c50 = hslColor(0.96f),
            c100 = hslColor(0.91f),
            c200 = hslColor(0.82f),
            c300 = hslColor(0.71f),
            c400 = hslColor(0.58f),
            c500 = baseColor,
            c600 = hslColor(l * 0.85f),
            c700 = hslColor(l * 0.70f),
            c800 = hslColor(l * 0.52f),
            c900 = hslColor(l * 0.35f),
            c950 = hslColor(l * 0.18f)
        )
    }

    /**
     * Gera uma escala neutra (cinzas/obsidiana) com sutil saturação da cor da marca.
     */
    fun createNeutralScale(baseHue: Float = 42f, baseSaturation: Float = 0.08f): ColorScale {
        fun neutralHsl(lightness: Float): Color {
            val argb = ColorUtils.HSLToColor(floatArrayOf(baseHue, baseSaturation, lightness.coerceIn(0.01f, 0.99f)))
            return Color(argb)
        }

        return ColorScale(
            c50 = neutralHsl(0.98f),
            c100 = neutralHsl(0.95f),
            c200 = neutralHsl(0.90f),
            c300 = neutralHsl(0.82f),
            c400 = neutralHsl(0.64f),
            c500 = neutralHsl(0.48f),
            c600 = neutralHsl(0.35f),
            c700 = neutralHsl(0.24f),
            c800 = neutralHsl(0.14f),
            c900 = neutralHsl(0.08f),
            c950 = neutralHsl(0.04f)
        )
    }
}
