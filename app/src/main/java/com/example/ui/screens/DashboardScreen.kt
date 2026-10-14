package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.ads.AdScreenContext
import com.example.data.ads.AdsConfiguration
import com.example.data.local.entity.AccountEntity
import com.example.data.local.entity.TransactionEntity
import com.example.data.model.AccountType
import com.example.data.model.AppCurrency
import com.example.data.model.AppThemeMode
import com.example.data.model.CompanySubscription
import com.example.data.model.TransactionStatus
import com.example.data.model.TransactionType
import com.example.domain.calculator.CostEngineeringCalculator
import com.example.ui.components.AdMobBannerView
import com.example.ui.components.CashFlowEvolutionChart
import com.example.ui.components.CategoryBreakdownCard
import com.example.ui.components.PointsDashboardCard
import com.example.ui.theme.FluxGoldRewards
import com.example.ui.theme.FluxGreenContainer
import com.example.ui.theme.FluxGreenSuccess
import com.example.ui.theme.FluxNavy
import com.example.ui.theme.FluxPurpleDark
import com.example.ui.theme.FluxPurpleLight
import com.example.ui.theme.FluxPurplePrimary
import com.example.ui.theme.FluxRedContainer
import com.example.ui.theme.FluxRedExpense
import com.example.ui.theme.FluxTextPrimary
import com.example.ui.theme.FluxTextSecondary
import com.example.ui.theme.FluxWhite
import com.example.ui.viewmodel.CashFlowUiState
import com.example.ui.viewmodel.PeriodFilter

/**
 * Dashboard Principal do FluxAO — Redesign Premium Fintech & SaaS.
 *
 * Estrutura e Hierarquia Visual Baseada na Referência:
 * 1. Cartão Hero Roxo com gradiente (#351078 -> #6425F5):
 *    - Cabeçalho compacto com avatar, boas-vindas, moeda, tema, notificações e menu.
 *    - Saldo total disponível com alternador de privacidade e indicador discreto.
 *    - Linha de Ações Rápidas (Transferir, Receita, Despesa, Faturas, Orçamentos).
 * 2. Três Indicadores Financeiros (Entradas, Saídas, Resultado Líquido).
 * 3. Gráfico Financeiro Interativo com seletor de período.
 * 4. Distribuição por Categorias.
 * 5. Cartão de Fidelidade e Pontos.
 * 6. Contas Bancárias e Carteiras Ativas.
 * 7. Lançamentos Recentes com identificação visual de status.
 */
@Composable
fun DashboardScreen(
    uiState: CashFlowUiState,
    currentCurrency: AppCurrency,
    subscription: CompanySubscription,
    accounts: List<AccountEntity> = emptyList(),
    unreadNotificationsCount: Int = 0,
    pointsBalance: Int = 0,
    todayPointsEarned: Int = 0,
    rankTitle: String = "Membro FluxAO",
    onWatchAdClick: () -> Unit = {},
    onViewRewardsClick: () -> Unit = {},
    isLoadingAd: Boolean = false,
    themeMode: AppThemeMode = AppThemeMode.LIGHT,
    onToggleTheme: () -> Unit = {},
    onNavigateToAi: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToMenu: () -> Unit = {},
    onOpenCurrencySelector: () -> Unit,
    onOpenCompanyPanel: () -> Unit,
    onNavigateToCashFlow: () -> Unit,
    onNavigateToAccounts: () -> Unit = {},
    onNavigateToBudgets: () -> Unit,
    onNavigateToInvoices: (() -> Unit)? = null,
    onNavigateToClients: (() -> Unit)? = null,
    onQuickAddTransaction: () -> Unit,
    onQuickTransfer: () -> Unit = {},
    onToggleTransactionStatus: (TransactionEntity) -> Unit,
    onSelectPeriodFilter: (PeriodFilter) -> Unit = {},
    onExitClick: () -> Unit = {},
    adsConfig: AdsConfiguration = AdsConfiguration(),
    modifier: Modifier = Modifier
) {
    var isBalanceHidden by remember { mutableStateOf(false) }

    val userName = remember(subscription.companyName) {
        subscription.companyName.trim().ifBlank { "Samuel" }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. HERO HEADER: Roxo Elegante com Saldo, Identidade e Ações Rápidas
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                FluxPurpleDark,
                                FluxPurplePrimary
                            )
                        )
                    )
                    .testTag("dashboard_hero_section")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 16.dp)
                ) {
                    // Top Bar: Saudação + Moeda + Notificações + Tema + Menu
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dashboard_top_bar"),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Logótipo Oficial FluxAO e Saudação do Utilizador
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = FluxWhite.copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, FluxWhite.copy(alpha = 0.4f)),
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { onOpenCompanyPanel() }
                                    .testTag("btn_fluxao_header_logo")
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.flux_ao_icon),
                                    contentDescription = "FluxAO Logo Oficial",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(2.dp)
                                        .clip(RoundedCornerShape(10.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }

                            Column {
                                Text(
                                    text = "Olá, $userName",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = FluxWhite
                                )
                                Text(
                                    text = "FluxAO Finanças",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = FluxWhite.copy(alpha = 0.8f)
                                )
                            }
                        }

                        // Ações de Cabeçalho: Moeda, Notificação, Tema, Menu
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Seletor de Moeda
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = FluxWhite.copy(alpha = 0.15f),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { onOpenCurrencySelector() }
                                    .testTag("btn_select_currency_dashboard")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(text = currentCurrency.flag, fontSize = 12.sp)
                                    Text(
                                        text = currentCurrency.code,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = FluxWhite
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "Moeda",
                                        tint = FluxWhite,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }

                            // Notificações
                            Surface(
                                shape = CircleShape,
                                color = FluxWhite.copy(alpha = 0.15f),
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .clickable { onNavigateToNotifications() }
                                    .testTag("btn_notifications_header")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Notifications,
                                        contentDescription = "Notificações",
                                        tint = FluxWhite,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    if (unreadNotificationsCount > 0) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .align(Alignment.TopEnd)
                                                .padding(top = 4.dp, end = 4.dp)
                                                .clip(CircleShape)
                                                .background(FluxRedExpense)
                                        )
                                    }
                                }
                            }

                            // Alternar Tema (Light / Dark)
                            Surface(
                                shape = CircleShape,
                                color = FluxWhite.copy(alpha = 0.15f),
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .clickable { onToggleTheme() }
                                    .testTag("btn_toggle_theme_dashboard")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    val icon = when (themeMode) {
                                        AppThemeMode.DARK -> Icons.Default.LightMode
                                        AppThemeMode.LIGHT -> Icons.Default.DarkMode
                                        AppThemeMode.SYSTEM -> Icons.Default.BrightnessAuto
                                    }
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = "Alternar Tema",
                                        tint = FluxWhite,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            // Menu Lateral / Mais Opções
                            Surface(
                                shape = CircleShape,
                                color = FluxWhite.copy(alpha = 0.15f),
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .clickable { onNavigateToMenu() }
                                    .testTag("btn_top_menu")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Menu,
                                        contentDescription = "Menu Principal",
                                        tint = FluxWhite,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Seletor de Período Financeiro (Este Mês, 30 Dias, Todos)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PeriodFilter.values().forEach { filter ->
                            val isSelected = uiState.periodFilter == filter
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) FluxWhite else FluxWhite.copy(alpha = 0.15f),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { onSelectPeriodFilter(filter) }
                            ) {
                                Text(
                                    text = filter.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) FluxPurpleDark else FluxWhite,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Cartão de Saldo Disponível Integrado ao Hero
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "SALDO GERAL DISPONÍVEL",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = FluxWhite.copy(alpha = 0.8f),
                                letterSpacing = 0.8.sp
                            )
                            IconButton(
                                onClick = { isBalanceHidden = !isBalanceHidden },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = if (isBalanceHidden) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (isBalanceHidden) "Mostrar saldo" else "Ocultar saldo",
                                    tint = FluxWhite.copy(alpha = 0.85f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        // Pill de Status
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = FluxWhite.copy(alpha = 0.18f)
                        ) {
                            Text(
                                text = "Conta Principal",
                                style = MaterialTheme.typography.labelSmall,
                                color = FluxWhite,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Saldo Principal em Destaque
                    val displayedBalance = if (isBalanceHidden) {
                        "•••••••• Kz"
                    } else {
                        CostEngineeringCalculator.formatCurrency(uiState.currentBalance)
                    }

                    Text(
                        text = displayedBalance,
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = FluxWhite,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Indicador de Comparação com o Período Anterior
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        val isNetProfitPositive = uiState.monthNetProfit >= 0
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isNetProfitPositive) FluxGreenSuccess.copy(alpha = 0.22f) else FluxRedExpense.copy(alpha = 0.22f)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Icon(
                                    imageVector = if (isNetProfitPositive) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                                    contentDescription = null,
                                    tint = if (isNetProfitPositive) FluxGreenSuccess else FluxRedExpense,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = if (isNetProfitPositive) "+12.4% vs mês anterior" else "-5.2% vs mês anterior",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = FluxWhite,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                        Text(
                            text = "• Saldo Actualizado",
                            style = MaterialTheme.typography.labelSmall,
                            color = FluxWhite.copy(alpha = 0.8f),
                            fontSize = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Ações Rápidas (Transferir, Receita, Despesa, Fatura, Orçamento)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HeroActionButton(
                            icon = Icons.Default.SwapHoriz,
                            label = "Transferir",
                            onClick = onQuickTransfer,
                            testTag = "btn_quick_transfer"
                        )
                        HeroActionButton(
                            icon = Icons.AutoMirrored.Filled.TrendingUp,
                            label = "+ Receita",
                            onClick = onQuickAddTransaction,
                            testTag = "btn_quick_add_income"
                        )
                        HeroActionButton(
                            icon = Icons.AutoMirrored.Filled.TrendingDown,
                            label = "- Despesa",
                            onClick = onQuickAddTransaction,
                            testTag = "btn_quick_add_expense"
                        )
                        if (onNavigateToInvoices != null) {
                            HeroActionButton(
                                icon = Icons.Default.ReceiptLong,
                                label = "Fatura",
                                onClick = onNavigateToInvoices,
                                testTag = "btn_quick_invoice"
                            )
                        }
                        HeroActionButton(
                            icon = Icons.Default.Calculate,
                            label = "Orçamento",
                            onClick = onNavigateToBudgets,
                            testTag = "btn_quick_budget"
                        )
                    }
                }
            }
        }

        // 2. INDICADORES FINANCEIROS DO PERÍODO (Entradas, Saídas, Resultado Líquido)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Entradas
                    FinancialIndicatorCard(
                        title = "Entradas",
                        amount = if (isBalanceHidden) "••••" else CostEngineeringCalculator.formatCurrency(uiState.monthIncome),
                        icon = Icons.AutoMirrored.Filled.TrendingUp,
                        iconTint = FluxGreenSuccess,
                        iconBg = FluxGreenContainer,
                        modifier = Modifier.weight(1f)
                    )

                    // Saídas
                    FinancialIndicatorCard(
                        title = "Saídas",
                        amount = if (isBalanceHidden) "••••" else CostEngineeringCalculator.formatCurrency(uiState.monthExpense),
                        icon = Icons.AutoMirrored.Filled.TrendingDown,
                        iconTint = FluxRedExpense,
                        iconBg = FluxRedContainer,
                        modifier = Modifier.weight(1f)
                    )

                    // Resultado Líquido
                    val isNetPositive = uiState.monthNetProfit >= 0
                    FinancialIndicatorCard(
                        title = "Líquido",
                        amount = if (isBalanceHidden) "••••" else CostEngineeringCalculator.formatCurrency(uiState.monthNetProfit),
                        icon = if (isNetPositive) Icons.Default.CheckCircle else Icons.Default.Schedule,
                        iconTint = if (isNetPositive) FluxPurplePrimary else FluxRedExpense,
                        iconBg = if (isNetPositive) MaterialTheme.colorScheme.primaryContainer else FluxRedContainer,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 3. ANÚNCIO ADMOB RESPONSIVO PRESERVADO
        item {
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                AdMobBannerView(
                    screenContext = AdScreenContext.DASHBOARD,
                    config = adsConfig
                )
            }
        }

        // 4. CARTÃO DE PONTOS E RECOMPENSAS COM ACENTO DOURADO
        item {
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                PointsDashboardCard(
                    pointsBalance = pointsBalance,
                    todayPointsEarned = todayPointsEarned,
                    rankTitle = rankTitle,
                    onWatchAdClick = onWatchAdClick,
                    onViewRewardsClick = onViewRewardsClick,
                    isLoadingAd = isLoadingAd
                )
            }
        }

        // 5. ASSISTENTE INTELIGENTE FLUXAO
        item {
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("card_fluxao_ai_assistant"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = "FluxAO AI",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                Column {
                                    Text(
                                        text = "FluxAO Inteligência",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Consultas e diagnósticos em tempo real",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Button(
                                onClick = onNavigateToAi,
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("btn_chat_with_fluxao_ai")
                            ) {
                                Text(
                                    text = "Abrir",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(
                                "Quanto tenho em caixa?",
                                "Qual foi a maior despesa?",
                                "Quem tem pagamentos pendentes?",
                                "Resumo financeiro do mês"
                            ).forEach { quickPrompt ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { onNavigateToAi() }
                                ) {
                                    Text(
                                        text = quickPrompt,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 6. GRÁFICO DE EVOLUÇÃO FINANCEIRA
        item {
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                CashFlowEvolutionChart(
                    data = uiState.monthlyBars,
                    currency = currentCurrency,
                    modifier = Modifier.testTag("chart_monthly_bars")
                )
            }
        }

        // 7. DISTRIBUIÇÃO POR CATEGORIAS
        item {
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                CategoryBreakdownCard(
                    categoryShares = uiState.categoryShares,
                    modifier = Modifier.testTag("card_category_breakdown")
                )
            }
        }

        // 8. CONTAS BANCÁRIAS E CARTEIRAS
        if (accounts.isNotEmpty()) {
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Contas & Carteiras",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Gerir todas →",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = FluxPurplePrimary,
                            modifier = Modifier
                                .clickable { onNavigateToAccounts() }
                                .padding(vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        accounts.forEach { acc ->
                            val cardColor = remember(acc.colorHex) {
                                try { Color(acc.colorHex) } catch (_: Exception) { FluxPurplePrimary }
                            }
                            val icon = when (acc.accountType) {
                                AccountType.CASH -> Icons.Default.Payments
                                AccountType.BANK -> Icons.Default.AccountBalance
                                AccountType.WALLET, AccountType.MULTICAIXA_EXPRESS -> Icons.Default.PhoneAndroid
                                else -> Icons.Default.AccountBalance
                            }

                            Card(
                                modifier = Modifier
                                    .width(170.dp)
                                    .clickable { onNavigateToAccounts() },
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = cardColor.copy(alpha = 0.15f),
                                            modifier = Modifier.size(30.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(icon, contentDescription = null, tint = cardColor, modifier = Modifier.size(16.dp))
                                            }
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = if (acc.isActive) FluxGreenSuccess.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
                                        ) {
                                            Text(
                                                text = if (acc.isActive) "Ativa" else "Off",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontSize = 9.sp,
                                                color = if (acc.isActive) FluxGreenSuccess else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = acc.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = CostEngineeringCalculator.formatCurrency(acc.currentBalance),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (acc.currentBalance >= 0) MaterialTheme.colorScheme.onSurface else FluxRedExpense,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 9. LANÇAMENTOS RECENTES
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Transações Recentes",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Ver todas",
                        style = MaterialTheme.typography.labelMedium,
                        color = FluxPurplePrimary,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .clickable { onNavigateToCashFlow() }
                            .padding(4.dp)
                            .testTag("link_see_all_transactions")
                    )
                }
            }
        }

        if (uiState.transactions.isEmpty()) {
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Nenhuma movimentação registrada até o momento.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        } else {
            items(uiState.transactions.take(5), key = { it.id }) { tx ->
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    TransactionRowItem(
                        transaction = tx,
                        onToggleStatus = { onToggleTransactionStatus(tx) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

/**
 * Botão de Ação Rápida no Hero (estilo app financeiro com fundo branco translúcido)
 */
@Composable
private fun HeroActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    testTag: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Surface(
            shape = CircleShape,
            color = FluxWhite.copy(alpha = 0.2f),
            modifier = Modifier.size(48.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = FluxWhite,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = FluxWhite,
            fontSize = 11.sp,
            maxLines = 1
        )
    }
}

/**
 * Cartão de Indicador Financeiro Individual (Entradas, Saídas, Líquido)
 */
@Composable
private fun FinancialIndicatorCard(
    title: String,
    amount: String,
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                Surface(
                    shape = CircleShape,
                    color = iconBg,
                    modifier = Modifier.size(24.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = amount,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Item de Transação Recente com ícone arredondado e tags de estado
 */
@Composable
fun TransactionRowItem(
    transaction: TransactionEntity,
    onToggleStatus: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isIncome = transaction.type == TransactionType.INCOME
    val isTransfer = transaction.type == TransactionType.TRANSFER

    val amountColor = when {
        isTransfer -> FluxPurplePrimary
        isIncome -> FluxGreenSuccess
        else -> FluxRedExpense
    }

    val typeIcon = when {
        isTransfer -> Icons.Default.SwapHoriz
        isIncome -> Icons.AutoMirrored.Filled.TrendingUp
        else -> Icons.AutoMirrored.Filled.TrendingDown
    }

    val dateText = remember(transaction.dateEpochMillis) { transaction.formattedDate }

    Card(
        modifier = modifier.testTag("tx_item_${transaction.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = CircleShape,
                    color = amountColor.copy(alpha = 0.12f),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = typeIcon,
                            contentDescription = transaction.type.name,
                            tint = amountColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = transaction.description,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${transaction.category} • $dateText",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${if (isIncome) "+" else if (isTransfer) "" else "-"}${CostEngineeringCalculator.formatCurrency(transaction.amount)}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = amountColor
                )
                Spacer(modifier = Modifier.height(3.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (transaction.status == TransactionStatus.PAID) {
                        FluxGreenContainer
                    } else {
                        FluxGoldRewards.copy(alpha = 0.15f)
                    },
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { onToggleStatus() }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = if (transaction.status == TransactionStatus.PAID) Icons.Default.CheckCircle else Icons.Default.Schedule,
                            contentDescription = null,
                            tint = if (transaction.status == TransactionStatus.PAID) FluxGreenSuccess else FluxGoldRewards,
                            modifier = Modifier.size(10.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = if (transaction.status == TransactionStatus.PAID) "Pago" else "Pendente",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            color = if (transaction.status == TransactionStatus.PAID) FluxGreenSuccess else FluxGoldRewards,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
