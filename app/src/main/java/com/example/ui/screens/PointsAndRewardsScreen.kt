package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.LockClock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.i18n.I18nService.t
import com.example.data.local.entity.PointsTransactionEntity
import com.example.data.service.AdsRewardService
import com.example.data.service.AntiFraudService
import com.example.data.service.AwardPointsResult
import com.example.data.service.PointsService
import com.example.data.service.PointsTransactionService
import com.example.data.service.RewardItem
import com.example.data.service.RewardService
import com.example.ui.theme.FluxGoldPrimary
import com.example.ui.theme.FluxGoldVibrant
import com.example.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PointsAndRewardsScreen(
    viewModel: MainViewModel,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val userPointsState by viewModel.userPointsFlow.collectAsStateWithLifecycle(initialValue = null)
    val pointsSummaryState by viewModel.pointsSummaryFlow.collectAsStateWithLifecycle(
        initialValue = com.example.data.service.PointsSummary()
    )
    val transactionsState by viewModel.pointsTransactionsFlow.collectAsStateWithLifecycle(
        initialValue = emptyList()
    )

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var isWatchingAd by remember { mutableStateOf(false) }
    var rewardEarnedFeedback by remember { mutableStateOf(false) }
    var selectedRewardToRedeem by remember { mutableStateOf<RewardItem?>(null) }

    val balance = pointsSummaryState.currentBalance
    val rankTitle = pointsSummaryState.rankTitle

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Stars,
                            contentDescription = null,
                            tint = FluxGoldPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = t("points.title", "Pontos & Recompensas"),
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("points_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = t("common.back", "Voltar")
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Hero Balance Header Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .testTag("points_hero_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1E293B)
                ),
                elevation = CardDefaults.cardElevation(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF0F172A),
                                    Color(0xFF1E293B),
                                    Color(0xFF334155)
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = FluxGoldPrimary.copy(alpha = 0.2f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, FluxGoldPrimary)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.WorkspacePremium,
                                        contentDescription = null,
                                        tint = FluxGoldVibrant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = rankTitle,
                                        color = FluxGoldVibrant,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF10B981).copy(alpha = 0.2f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Shield,
                                        contentDescription = null,
                                        tint = Color(0xFF34D399),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = t("points.antifraud_active", "Antifraude Ativo"),
                                        color = Color(0xFF34D399),
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = t("points.your_balance", "Seu Saldo Atual"),
                            color = Color.LightGray,
                            fontSize = 13.sp
                        )

                        Row(
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Text(
                                text = "$balance",
                                fontSize = 42.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = FluxGoldVibrant
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = t("points.pts", "PTS"),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = FluxGoldPrimary,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Divider(color = Color.White.copy(alpha = 0.1f))
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = t("points.total_earned", "Acumulado Total"),
                                    color = Color.Gray,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = "+${pointsSummaryState.lifetimeEarned} pts",
                                    color = Color(0xFF34D399),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }

                            Column {
                                Text(
                                    text = t("points.total_spent", "Resgatado Total"),
                                    color = Color.Gray,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = "-${pointsSummaryState.lifetimeSpent} pts",
                                    color = Color(0xFFF87171),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }

            // Tabs Navigation
            val tabTitles = listOf(
                t("points.tab_earn", "Ganhar Pontos"),
                t("points.tab_rewards", "Recompensas"),
                t("points.tab_entitlements", "Meus Benefícios"),
                t("points.tab_history", "Histórico"),
                t("points.tab_rules", "Regras & Antifraude")
            )

            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                edgePadding = 16.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            when (selectedTabIndex) {
                0 -> EarnPointsTab(
                    isWatchingAd = isWatchingAd,
                    rewardEarnedFeedback = rewardEarnedFeedback,
                    onWatchRewardedAd = {
                        isWatchingAd = true
                        scope.launch {
                            viewModel.watchRewardedAd(
                                context = context,
                                onRewardEarned = { pts ->
                                    isWatchingAd = false
                                    rewardEarnedFeedback = true
                                    scope.launch {
                                        snackbarHostState.showSnackbar("+$pts pontos recebidos!")
                                        kotlinx.coroutines.delay(3000)
                                        rewardEarnedFeedback = false
                                    }
                                },
                                onError = { msg ->
                                    isWatchingAd = false
                                    scope.launch {
                                        snackbarHostState.showSnackbar(msg)
                                    }
                                }
                            )
                        }
                    }
                )
                1 -> RewardsCatalogTab(
                    rewardCatalog = viewModel.rewardService.catalog,
                    currentBalance = balance,
                    onSelectReward = { reward -> selectedRewardToRedeem = reward }
                )
                2 -> MyEntitlementsTab(
                    viewModel = viewModel,
                    currentBalance = balance,
                    onSelectReward = { reward -> selectedRewardToRedeem = reward }
                )
                3 -> TransactionsHistoryTab(transactions = transactionsState)
                4 -> RulesAndAntiFraudTab(viewModel = viewModel)
            }
        }
    }

    // Modal Confirmation for Redemption
    selectedRewardToRedeem?.let { reward ->
        AlertDialog(
            onDismissRequest = { selectedRewardToRedeem = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.CardGiftcard,
                    contentDescription = null,
                    tint = FluxGoldPrimary,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = t("points.confirm_redemption", "Confirmar Resgate"),
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = reward.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = reward.description,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = FluxGoldPrimary.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = "Custo: ${reward.pointsRequired} PTS (Seu Saldo: $balance PTS)",
                            modifier = Modifier.padding(12.dp),
                            fontWeight = FontWeight.Bold,
                            color = FluxGoldVibrant
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val rewardToProcess = reward
                        selectedRewardToRedeem = null
                        scope.launch {
                            viewModel.redeemReward(
                                reward = rewardToProcess,
                                onSuccess = {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("🎁 Recompensa '${rewardToProcess.title}' resgatada com sucesso!")
                                    }
                                },
                                onError = { err ->
                                    scope.launch {
                                        snackbarHostState.showSnackbar("⚠️ $err")
                                    }
                                }
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FluxGoldPrimary),
                    enabled = balance >= reward.pointsRequired
                ) {
                    Text(t("points.redeem_now", "Resgatar Agora"), color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedRewardToRedeem = null }) {
                    Text(t("common.cancel", "Cancelar"))
                }
            }
        )
    }
}

@Composable
fun EarnPointsTab(
    isWatchingAd: Boolean,
    rewardEarnedFeedback: Boolean = false,
    onWatchRewardedAd: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // SECTION 1: GANHE PONTOS (REWARDED AD)
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "🎁",
                    fontSize = 20.sp
                )
                Text(
                    text = "GANHE PONTOS",
                    fontWeight = FontWeight.Black,
                    fontSize = 17.sp,
                    color = FluxGoldVibrant,
                    letterSpacing = 0.5.sp
                )
            }
            Spacer(modifier = Modifier.height(6.dp))

            // AdMob Rewarded Ad Featured Banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .testTag("rewarded_ad_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, FluxGoldVibrant.copy(alpha = 0.6f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PlayCircle,
                            contentDescription = null,
                            tint = FluxGoldVibrant,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "📺 Assistir anúncio recompensado",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Assista a um anúncio completo e ganhe 10 pontos.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = FluxGoldVibrant
                        ) {
                            Text(
                                text = "+10 PTS",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontWeight = FontWeight.Black,
                                color = Color.Black,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = onWatchRewardedAd,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("watch_ad_button"),
                        enabled = !isWatchingAd && !rewardEarnedFeedback,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (rewardEarnedFeedback) Color(0xFF10B981) else FluxGoldVibrant,
                            disabledContainerColor = if (rewardEarnedFeedback) Color(0xFF10B981) else FluxGoldVibrant.copy(alpha = 0.5f)
                        )
                    ) {
                        if (isWatchingAd) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.Black,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Carregando anúncio...", color = Color.Black, fontWeight = FontWeight.Bold)
                        } else if (rewardEarnedFeedback) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "✓ +10 pontos",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.PlayCircle,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Assistir anúncio",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // SECTION 2: CUSTO DE RECURSOS (USO DO FLUXAO)
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "💼",
                    fontSize = 20.sp
                )
                Text(
                    text = "CUSTO DE RECURSOS (USO DO FLUXAO)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "As ações abaixo consomem 2 pontos por utilização realizada com sucesso:",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        val resourceCostList = listOf(
            Triple("✓ Fatura Emitida", "Emita uma fatura fiscal ou comercial válida", "-2 PTS"),
            Triple("✓ Fatura enviada no WhatsApp", "Envie faturas diretamente pelo WhatsApp", "-2 PTS"),
            Triple("✓ Fatura baixada em PDF", "Faça o download de PDFs de faturas", "-2 PTS"),
            Triple("✓ Orçamento Criado", "Crie e envie orçamentos para seus clientes", "-2 PTS"),
            Triple("✓ Novo Cliente Cadastrado", "Registe novos clientes na sua plataforma", "-2 PTS"),
            Triple("✓ Agendamento Realizado", "Marque agendamentos na sua agenda comercial", "-2 PTS"),
            Triple("✓ Consulta no FluxoAI", "Análises financeiras com inteligência artificial", "-2 PTS"),
            Triple("✓ Movimento no Caixa", "Registe lançamentos e movimentações de caixa", "-2 PTS")
        )

        items(resourceCostList) { (title, desc, pointsCost) ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(text = desc, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFEF4444).copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFFEF4444).copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = pointsCost,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFEF4444),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun RewardsCatalogTab(
    rewardCatalog: List<RewardItem>,
    currentBalance: Int,
    onSelectReward: (RewardItem) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        items(rewardCatalog) { reward ->
            val canAfford = currentBalance >= reward.pointsRequired

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clickable { onSelectReward(reward) },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (canAfford) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ),
                elevation = CardDefaults.cardElevation(if (canAfford) 2.dp else 0.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(if (canAfford) FluxGoldPrimary.copy(alpha = 0.2f) else Color.Gray.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (reward.iconName) {
                                "Assessment" -> Icons.Default.Assessment
                                "Palette" -> Icons.Default.Palette
                                "LocalOffer" -> Icons.Default.LocalOffer
                                "Chat" -> Icons.Default.Chat
                                "SupportAgent" -> Icons.Default.SupportAgent
                                else -> Icons.Default.WorkspacePremium
                            },
                            contentDescription = null,
                            tint = if (canAfford) FluxGoldVibrant else Color.Gray
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = reward.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = reward.description,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(horizontalAlignment = Alignment.End) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (canAfford) FluxGoldPrimary else Color.Gray.copy(alpha = 0.3f)
                        ) {
                            Text(
                                text = "${reward.pointsRequired} PTS",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                color = if (canAfford) Color.Black else MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionsHistoryTab(
    transactions: List<PointsTransactionEntity>
) {
    if (transactions.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = t("points.no_transactions", "Nenhuma transação de pontos registada."),
                    color = Color.Gray
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            items(transactions) { tx ->
                val isEarn = tx.points > 0
                val formattedDate = java.text.SimpleDateFormat(
                    "dd/MM/yyyy HH:mm",
                    java.util.Locale.getDefault()
                ).format(java.util.Date(tx.createdAtEpoch))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isEarn) Color(0xFF10B981).copy(alpha = 0.15f)
                                    else Color(0xFFEF4444).copy(alpha = 0.15f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isEarn) Icons.Default.AutoAwesome else Icons.Default.CardGiftcard,
                                contentDescription = null,
                                tint = if (isEarn) Color(0xFF10B981) else Color(0xFFEF4444),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = tx.description,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "$formattedDate • Source: ${tx.source}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Text(
                            text = if (isEarn) "+${tx.points}" else "${tx.points}",
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isEarn) Color(0xFF10B981) else Color(0xFFEF4444),
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RulesAndAntiFraudTab(viewModel: MainViewModel) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = Color(0xFF10B981)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = t("points.antifraud_title", "Sistema Antifraude & Idempotência"),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "• Idempotência Ativa: Impede que a mesma fatura, orçamento ou cliente pontue mais de uma vez.\n" +
                                "• Servidor SSV (Server-Side Verification): Validação dos eventos do AdMob diretamente com token de integridade.\n" +
                                "• Anúncios Recompensados: Sem limite artificial diário — assista e ganhe pontos conforme disponibilidade do inventário AdMob.\n" +
                                "• Regras de Ações Reais: Pontos concedidos SOMENTE após conclusão efetiva da ação.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Disponibilidade de Anúncios",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Anúncios Recompensados:")
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF10B981).copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "Sem Limite Diário (Ilimitado)",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF047857)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MyEntitlementsTab(
    viewModel: MainViewModel,
    currentBalance: Int,
    onSelectReward: (RewardItem) -> Unit
) {
    val activeEntitlements by viewModel.activeEntitlementsList.collectAsStateWithLifecycle()

    val allFeatures = listOf(
        Triple("cashflow_basic", "Fluxo de Caixa", 0),
        Triple("invoices", "Faturas", 50),
        Triple("customers", "Clientes", 30),
        Triple("appointments", "Agendamentos", 30),
        Triple("reports", "Relatórios", 50),
        Triple("fluxoai", "FluxoAI", 80),
        Triple("whatsapp_automation", "Automação WhatsApp", 150)
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "🎁 MEUS BENEFÍCIOS & ACESSOS ATIVOS",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Acompanhe o estado de desbloqueio das funcionalidades do aplicativo:",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        items(allFeatures) { (code, name, cost) ->
            val isFree = code == "cashflow_basic"
            val activeEntitlement = activeEntitlements.firstOrNull { it.rewardCode == code && it.isCurrentlyActive }
            val isUnlocked = isFree || viewModel.isFeatureUnlocked(code) || activeEntitlement != null

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isUnlocked) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ),
                elevation = CardDefaults.cardElevation(if (isUnlocked) 2.dp else 0.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(
                                if (isUnlocked) Color(0xFF10B981).copy(alpha = 0.15f)
                                else MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isUnlocked) "✓" else "🔒",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isUnlocked) Color(0xFF10B981) else MaterialTheme.colorScheme.error
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        if (isFree) {
                            Text(
                                text = "Acesso Gratuito Padrão (Sem Custo)",
                                fontSize = 12.sp,
                                color = Color(0xFF10B981),
                                fontWeight = FontWeight.Medium
                            )
                        } else if (isUnlocked && activeEntitlement != null) {
                            Text(
                                text = "Ativo • Expira em: ${activeEntitlement.formattedExpirationDate}",
                                fontSize = 12.sp,
                                color = Color(0xFF10B981),
                                fontWeight = FontWeight.Medium
                            )
                        } else {
                            Text(
                                text = "Bloqueado • Custo: $cost PTS (30 Dias)",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    if (!isUnlocked && !isFree) {
                        val reward = viewModel.rewardService.catalog.firstOrNull { it.code == code }
                        if (reward != null) {
                            Button(
                                onClick = { onSelectReward(reward) },
                                colors = ButtonDefaults.buttonColors(containerColor = FluxGoldPrimary),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "$cost PTS",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
