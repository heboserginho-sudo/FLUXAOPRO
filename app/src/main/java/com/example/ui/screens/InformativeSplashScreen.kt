package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.FeatureKeyHelper
import com.example.ui.theme.Emerald500
import com.example.ui.theme.FluxGoldLight
import com.example.ui.theme.FluxGoldPrimary
import com.example.ui.theme.FluxGoldRewards
import com.example.ui.theme.FluxGoldVibrant
import com.example.ui.theme.FluxPurplePrimary
import com.example.ui.theme.FluxPurpleContainer
import com.example.ui.theme.FluxWhite
import com.example.ui.theme.Obsidian800
import com.example.ui.theme.Obsidian850
import com.example.ui.theme.Obsidian900
import com.example.ui.viewmodel.MainViewModel

/**
 * Data representation of a feature displayed on the Informative Splash Screen.
 */
data class SplashFeatureInfo(
    val key: String,
    val title: String,
    val isUnlocked: Boolean,
    val pointsCost: Int,
    val icon: ImageVector
)

/**
 * Informative Splash Screen for FluxAO.
 * Explains available features, unlockable modules via points, and rewarded ad benefits
 * in an elegant, responsive, non-blocking interface.
 */
@Composable
fun InformativeSplashScreen(
    viewModel: MainViewModel,
    userBalance: Int,
    unlockedFeatures: Set<String>,
    onContinue: () -> Unit
) {
    // Dynamic cost lookup from configured reward catalog in ViewModel
    fun getDynamicCost(featureKey: String, fallbackCost: Int): Int {
        val canonical = FeatureKeyHelper.normalize(featureKey)
        return viewModel.rewardService.catalog.find {
            FeatureKeyHelper.normalize(it.code) == canonical
        }?.pointsRequired ?: fallbackCost
    }

    val agendaCost = remember { getDynamicCost("appointments", 30) }
    val invoicesCost = remember { getDynamicCost("invoices", 50) }
    val clientsCost = remember { getDynamicCost("customers", 30) }
    val reportsCost = remember { getDynamicCost("reports", 50) }
    val aiCost = remember { getDynamicCost("fluxoai", 80) }

    // Dynamic unlock status verification
    val isAgendaUnlocked = FeatureKeyHelper.isUnlocked(unlockedFeatures, "appointments") || viewModel.isFeatureUnlocked("appointments")
    val isInvoicesUnlocked = FeatureKeyHelper.isUnlocked(unlockedFeatures, "invoices") || viewModel.isFeatureUnlocked("invoices")
    val isClientsUnlocked = FeatureKeyHelper.isUnlocked(unlockedFeatures, "customers") || viewModel.isFeatureUnlocked("customers")
    val isReportsUnlocked = FeatureKeyHelper.isUnlocked(unlockedFeatures, "reports") || viewModel.isFeatureUnlocked("reports")
    val isAiUnlocked = FeatureKeyHelper.isUnlocked(unlockedFeatures, "fluxoai") || viewModel.isFeatureUnlocked("fluxoai")

    val unlockableFeatures = listOf(
        SplashFeatureInfo("appointments", "Agenda & Lembretes", isAgendaUnlocked, agendaCost, Icons.Default.Event),
        SplashFeatureInfo("invoices", "Faturas Comerciais Pro", isInvoicesUnlocked, invoicesCost, Icons.Default.ReceiptLong),
        SplashFeatureInfo("customers", "Clientes & Parceiros", isClientsUnlocked, clientsCost, Icons.Default.People),
        SplashFeatureInfo("reports", "Relatórios Avançados & DRE", isReportsUnlocked, reportsCost, Icons.Default.Assessment),
        SplashFeatureInfo("fluxoai", "Assistente IA (FluxAO IA)", isAiUnlocked, aiCost, Icons.Default.AutoAwesome)
    )

    val lockedList = unlockableFeatures.filter { !it.isUnlocked }
    val unlockedPremiumList = unlockableFeatures.filter { it.isUnlocked }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .testTag("splash_screen_informative"),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header: Logo + Welcome + Subtitle
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, bottom = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(76.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(FluxPurplePrimary.copy(alpha = 0.25f), Color.Transparent)
                                    )
                                )
                                .border(1.5.dp, FluxPurplePrimary.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.flux_ao_icon),
                                contentDescription = "FluxAO Logo",
                                modifier = Modifier
                                    .size(68.dp)
                                    .clip(RoundedCornerShape(18.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Flux",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "AO",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black,
                                color = FluxPurplePrimary
                            )
                        }

                        Text(
                            text = "Bem-vindo ao FluxAO",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(top = 2.dp)
                        )

                        Text(
                            text = "Organize, acompanhe e controle o seu negócio em um só lugar.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                                .fillMaxWidth()
                        )

                        // User Points Indicator
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = FluxGoldRewards.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, FluxGoldRewards.copy(alpha = 0.45f))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Stars,
                                    contentDescription = null,
                                    tint = FluxGoldRewards,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Você possui $userBalance pontos",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                // Section 1: Available Features
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Emerald500.copy(alpha = 0.35f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(Emerald500.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Emerald500,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Funções disponíveis",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Acesso gratuito liberado no seu sistema",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Default Free Features
                            FreeFeatureRow("Fluxo de Caixa", "Gestão de receitas e despesas")
                            FreeFeatureRow("Dashboard & Métricas", "Gráficos e visão global em tempo real")
                            FreeFeatureRow("Movimentos Financeiros", "Histórico e conferência de transações")
                            FreeFeatureRow("Contas Bancárias & Caixas", "Controle de saldo por entidade")

                            // Dynamic Already Unlocked Features
                            unlockedPremiumList.forEach { feat ->
                                FreeFeatureRow(
                                    title = feat.title,
                                    description = "Desbloqueado com sucesso na sua conta",
                                    isPremiumUnlocked = true
                                )
                            }
                        }
                    }
                }

                // Section 2: Unlockable Features (with points)
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, FluxGoldPrimary.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(FluxGoldPrimary.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = FluxGoldPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Desbloqueie mais funções",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Funcionalidades adicionais sem pagamento direto",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            if (lockedList.isEmpty()) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Emerald500,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Todas as funcionalidades principais estão desbloqueadas!",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Emerald500
                                    )
                                }
                            } else {
                                lockedList.forEach { feat ->
                                    UnlockableFeatureRow(
                                        title = feat.title,
                                        cost = feat.pointsCost,
                                        icon = feat.icon
                                    )
                                }
                            }
                        }
                    }
                }

                // Section 3: Earn Points with Rewarded Ads
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF8B5CF6).copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CardGiftcard,
                                        contentDescription = null,
                                        tint = Color(0xFF8B5CF6),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Ganhe pontos",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Acumule pontos através de anúncios premiados",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "Assista anúncios recompensados e ganhe pontos para desbloquear funcionalidades adicionais.",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 18.sp
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "Quanto mais pontos acumular, mais funcionalidades poderá desbloquear.",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = FluxGoldVibrant
                            )
                        }
                    }
                }

                // Footnote
                item {
                    Text(
                        text = "Algumas funcionalidades adicionais podem ser desbloqueadas usando pontos ganhos através de anúncios recompensados.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            // Bottom Continue Action Area
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Button(
                    onClick = onContinue,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("btn_splash_continue"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = FluxPurplePrimary,
                        contentColor = FluxWhite
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Continuar para o FluxAO",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FreeFeatureRow(
    title: String,
    description: String,
    isPremiumUnlocked: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = Emerald500,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = if (isPremiumUnlocked) Emerald500.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
        ) {
            Text(
                text = if (isPremiumUnlocked) "Desbloqueada" else "Gratuito",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (isPremiumUnlocked) Emerald500 else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
private fun UnlockableFeatureRow(
    title: String,
    cost: Int,
    icon: ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = FluxGoldPrimary,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = FluxGoldPrimary.copy(alpha = 0.15f),
            border = androidx.compose.foundation.BorderStroke(1.dp, FluxGoldPrimary.copy(alpha = 0.4f))
        ) {
            Text(
                text = "$cost pontos",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = FluxGoldVibrant,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }
    }
}
