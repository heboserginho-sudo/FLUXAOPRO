package com.example.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Sistema de Cores Oficial do FluxAO — Redesign Premium Fintech & SaaS (Mobile & Web).
 * Baseado na paleta de referência:
 * - Roxo principal: #6425F5
 * - Roxo escuro: #351078
 * - Azul-marinho: #11152F
 * - Branco: #FFFFFF
 * - Fundo claro: #F4F5FC
 * - Cinzento para superfícies: #E9EAF4
 * - Texto principal: #17172B
 * - Texto secundário: #73758A
 * - Verde para receitas e sucesso: #12A879
 * - Vermelho para despesas e erros: #E5485B
 * - Dourado para pontos e recompensas: #F5B82E
 */

// 1. PALETA BASE OFICIAL
val FluxPurplePrimary = Color(0xFF6425F5)       // #6425F5 - Roxo principal (botões, destaques e acentos)
val FluxPurpleDark = Color(0xFF351078)          // #351078 - Roxo escuro (gradiente hero e cabeçalhos)
val FluxPurpleLight = Color(0xFF834DF7)         // Roxo suave para hover/foco
val FluxPurpleContainer = Color(0xFFEFEAFF)     // Container roxo suave para chips e tags em Light Mode
val FluxPurpleContainerDark = Color(0xFF24144A) // Container roxo escuro

val FluxNavy = Color(0xFF11152F)                // #11152F - Azul-marinho profundo
val FluxNavyCard = Color(0xFF191F3E)            // Cards no modo escuro
val FluxNavyElevated = Color(0xFF232A52)        // Superfície elevada no modo escuro
val FluxNavyBorder = Color(0xFF2E386B)          // Bordas no modo escuro

val FluxWhite = Color(0xFFFFFFFF)               // #FFFFFF - Branco puro (cartões e textos)
val FluxLightBackground = Color(0xFFF4F5FC)     // #F4F5FC - Fundo principal claro
val FluxSurfaceGray = Color(0xFFE9EAF4)         // #E9EAF4 - Cinzento de superfícies
val FluxSurfaceBorder = Color(0xFFE2E4F0)       // Divisórias e bordas discretas em Light Mode

val FluxTextPrimary = Color(0xFF17172B)         // #17172B - Texto principal de alto contraste
val FluxTextSecondary = Color(0xFF73758A)       // #73758A - Texto secundário e legendas
val FluxTextMuted = Color(0xFFA0A2B8)           // Texto desativado / placeholder

// ESTADOS FINANCEIROS E ALERTAS
val FluxGreenSuccess = Color(0xFF12A879)        // #12A879 - Verde para receitas e resultado positivo
val FluxGreenContainer = Color(0xFFE7F7F2)      // Container de receitas em Light Mode
val FluxGreenDarkContainer = Color(0xFF093E2D)  // Container de receitas em Dark Mode

val FluxRedExpense = Color(0xFFE5485B)          // #E5485B - Vermelho para despesas e alertas
val FluxRedContainer = Color(0xFFFDEDEF)        // Container de despesas em Light Mode
val FluxRedDarkContainer = Color(0xFF4C151B)    // Container de despesas em Dark Mode

val FluxGoldRewards = Color(0xFFF5B82E)         // #F5B82E - Dourado para pontos e recompensas
val FluxGoldContainer = Color(0xFFFEF8E9)       // Container de recompensas em Light Mode
val FluxGoldDarkContainer = Color(0xFF4A370A)   // Container de recompensas em Dark Mode

// 2. ESCALAS DE DESIGN SYSTEM (Para interoperabilidade total)
val FluxPurpleScale: ColorScale = ColorScaleGenerator.createScale(FluxPurplePrimary)
val FluxNeutralScale: ColorScale = ColorScaleGenerator.createNeutralScale()

// 3. ALIASES DE RETROCOMPATIBILIDADE COM O RESTANTE DO CÓDIGO
val FluxNavyDeep = FluxNavy
val FluxNavyGraphite = FluxNavyCard
val FluxNavyBorderSubtle = Color(0xFF20284D)

val FluxTurquoise = FluxPurplePrimary
val FluxTurquoiseLight = FluxPurpleLight
val FluxTurquoiseDark = FluxPurpleDark
val FluxTurquoiseContainer = FluxPurpleContainer
val FluxWhiteSoft = FluxWhite

val FluxSlate50 = FluxLightBackground
val FluxSlate100 = FluxSurfaceGray
val FluxSlate200 = FluxSurfaceBorder
val FluxSlate300 = Color(0xFFD2D5E6)
val FluxSlate400 = FluxTextSecondary
val FluxSlate500 = Color(0xFF5F6175)
val FluxSlate600 = Color(0xFF45475A)
val FluxSlate700 = Color(0xFF303244)
val FluxSlate800 = Color(0xFF1E2033)
val FluxSlate900 = FluxTextPrimary

val FluxEmeraldSuccess = FluxGreenSuccess
val FluxEmeraldContainer = FluxGreenContainer
val FluxEmeraldBorder = FluxGreenSuccess

val FluxCrimsonError = FluxRedExpense
val FluxCrimsonContainer = FluxRedContainer
val FluxCrimsonBorder = FluxRedExpense

val FluxAmberWarning = FluxGoldRewards
val FluxAmberContainer = FluxGoldContainer
val FluxAmberBorder = FluxGoldRewards

val FluxGoldPrimary = FluxPurplePrimary
val FluxGoldVibrant = FluxPurplePrimary
val FluxGoldLight = FluxPurpleLight
val FluxGoldSoft = FluxWhite
val FluxGoldDark = FluxPurpleDark
val FluxGoldDeep = FluxNavy
val FluxGoldGlow = FluxPurplePrimary

val FluxGoldScale: ColorScale = FluxPurpleScale
val FluxNavyScale: ColorScale = FluxPurpleScale
val FluxTurquoiseScale: ColorScale = FluxPurpleScale
val FluxAmberScale: ColorScale = ColorScaleGenerator.createScale(FluxGoldRewards)
val ObsidianScale: ColorScale = FluxPurpleScale

val Obsidian950 = FluxNavy
val Obsidian900 = FluxNavyCard
val Obsidian850 = Color(0xFF151B38)
val Obsidian800 = FluxNavyElevated
val Obsidian700 = FluxNavyBorder
val Obsidian600 = FluxSlate600
val Obsidian500 = FluxSlate500
val Obsidian400 = FluxSlate400

val Champagne50 = FluxLightBackground
val Champagne100 = FluxSurfaceGray
val Champagne200 = FluxSurfaceBorder
val PureWhite = FluxWhite

val Emerald800 = Color(0xFF0B5840)
val Emerald700 = Color(0xFF0E7A59)
val Emerald600 = FluxGreenSuccess
val Emerald500 = FluxGreenSuccess
val Emerald200 = Color(0xFFA3E5CF)
val Emerald100 = FluxGreenContainer
val Emerald50 = Color(0xFFF2FBF8)

val Amber700 = Color(0xFFC78C12)
val Amber600 = Color(0xFFE0A31E)
val Amber500 = FluxGoldRewards
val Amber100 = FluxGoldContainer

val Crimson700 = Color(0xFFB52B3C)
val Crimson600 = Color(0xFFD3364A)
val Crimson500 = FluxRedExpense
val Crimson100 = FluxRedContainer

val Navy900 = FluxNavy
val Navy800 = FluxNavyCard
val Navy700 = FluxNavyElevated
val Navy600 = FluxPurplePrimary
val Navy200 = FluxPurpleLight
val Navy100 = FluxWhite
val Navy50 = FluxLightBackground

val Slate950 = FluxNavy
val Slate900 = FluxNavyCard
val Slate850 = Color(0xFF151B38)
val Slate800 = FluxNavyElevated
val Slate700 = FluxNavyBorder
val Slate600 = FluxSlate600
val Slate400 = FluxSlate400
val Slate300 = FluxSlate300
val Slate200 = FluxSlate200
val Slate100 = FluxSlate100
val Slate50 = FluxSlate50
