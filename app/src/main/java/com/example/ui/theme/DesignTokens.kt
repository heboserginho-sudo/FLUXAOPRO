package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Design Tokens centralizados para o Design System do FluxAO.
 * Elimina cores hardcoded e garante conformidade entre Light e Dark mode.
 */
data class FluxAoColors(
    val primary: Color,
    val primaryLight: Color,
    val primaryDark: Color,
    val primaryContainer: Color,
    val onPrimary: Color,
    val onPrimaryContainer: Color,

    val secondary: Color,
    val secondaryLight: Color,
    val secondaryDark: Color,
    val secondaryContainer: Color,
    val onSecondary: Color,

    val accent: Color,

    val background: Color,
    val onBackground: Color,
    val surface: Color,
    val onSurface: Color,
    val card: Color,
    val cardElevated: Color,

    val text: Color,
    val textSecondary: Color,
    val textMuted: Color,

    val border: Color,
    val borderSubtle: Color,

    val success: Color,
    val onSuccess: Color,
    val warning: Color,
    val onWarning: Color,
    val error: Color,
    val onError: Color,

    val primaryScale: ColorScale,
    val secondaryScale: ColorScale,
    val neutralScale: ColorScale
)

data class FluxAoRadius(
    val xs: Dp = 4.dp,
    val sm: Dp = 8.dp,
    val md: Dp = 12.dp,
    val lg: Dp = 16.dp,
    val xl: Dp = 20.dp,
    val full: Dp = 999.dp
)

data class FluxAoSpacing(
    val xs: Dp = 4.dp,
    val sm: Dp = 8.dp,
    val md: Dp = 12.dp,
    val lg: Dp = 16.dp,
    val xl: Dp = 24.dp,
    val xxl: Dp = 32.dp
)

data class FluxAoElevation(
    val none: Dp = 0.dp,
    val sm: Dp = 2.dp,
    val md: Dp = 4.dp,
    val lg: Dp = 8.dp
)

data class FluxAoDesignTokens(
    val colors: FluxAoColors,
    val radius: FluxAoRadius = FluxAoRadius(),
    val spacing: FluxAoSpacing = FluxAoSpacing(),
    val elevation: FluxAoElevation = FluxAoElevation(),
    val isDark: Boolean = true
)

val LocalFluxAoDesignTokens = staticCompositionLocalOf<FluxAoDesignTokens> {
    error("Nenhum FluxAoDesignTokens fornecido no escopo da aplicação.")
}

/**
 * Ponto de acesso centralizado ao Design System do FluxAO em qualquer Composable:
 *
 * Exemplo de uso:
 * val colors = FluxAoTheme.colors
 * val spacing = FluxAoTheme.spacing
 * val radius = FluxAoTheme.radius
 */
object FluxAoTheme {
    val tokens: FluxAoDesignTokens
        @Composable
        @ReadOnlyComposable
        get() = LocalFluxAoDesignTokens.current

    val colors: FluxAoColors
        @Composable
        @ReadOnlyComposable
        get() = tokens.colors

    val radius: FluxAoRadius
        @Composable
        @ReadOnlyComposable
        get() = tokens.radius

    val spacing: FluxAoSpacing
        @Composable
        @ReadOnlyComposable
        get() = tokens.spacing

    val elevation: FluxAoElevation
        @Composable
        @ReadOnlyComposable
        get() = tokens.elevation
}
