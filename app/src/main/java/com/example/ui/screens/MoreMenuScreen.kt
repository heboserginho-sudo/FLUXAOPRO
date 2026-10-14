package com.example.ui.screens

import com.example.data.model.FeatureKeyHelper
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.FluxGoldRewards
import com.example.ui.theme.FluxPurpleContainer
import com.example.ui.theme.FluxPurplePrimary
import com.example.ui.theme.FluxWhite
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreMenuScreen(
    viewModel: MainViewModel,
    onNavigateToProjects: () -> Unit,
    onNavigateToCategories: () -> Unit,
    onNavigateToGoals: () -> Unit,
    onNavigateToAgenda: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToAiAssistant: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToAccounts: () -> Unit,
    onNavigateToClients: () -> Unit,
    onNavigateToInvoices: () -> Unit,
    onNavigateToSuperAdmin: () -> Unit = {},
    onNavigateToCustomerPortal: () -> Unit = {},
    onNavigateToPoints: () -> Unit = {},
    onExitDashboard: () -> Unit = {}
) {
    val unreadNotificationsCount by viewModel.unreadNotificationCount.collectAsStateWithLifecycle()
    val subscription by viewModel.subscription.collectAsStateWithLifecycle()
    val unlockedFeatures by viewModel.unlockedFeaturesState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Menu & Ferramentas", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Profile teaser card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onNavigateToProfile() }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(FluxPurplePrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = FluxWhite, modifier = Modifier.size(26.dp))
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = subscription.companyName.ifBlank { "Minha Empresa" },
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "NIF: ${subscription.nif.ifBlank { "Consumidor Final" }} • Modo Local",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            item {
                Text(
                    text = "Gestão Operacional & Financeira",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                )
            }

            item {
                MoreMenuItem(
                    title = "Projetos & Obras",
                    subtitle = "Custos, receitas, margens de lucro e status",
                    icon = Icons.Default.Assignment,
                    tag = "menu_projects",
                    onClick = onNavigateToProjects
                )
            }

            item {
                MoreMenuItem(
                    title = "Categorias Financeiras",
                    subtitle = "Gerir receitas e despesas com cores e ícones",
                    icon = Icons.Default.Category,
                    tag = "menu_categories",
                    onClick = onNavigateToCategories
                )
            }

            item {
                MoreMenuItem(
                    title = "Metas Financeiras",
                    subtitle = "Metas de lucro, vendas, despesas e reserva",
                    icon = Icons.Default.Flag,
                    tag = "menu_goals",
                    onClick = onNavigateToGoals
                )
            }

            item {
                val isAgendaUnlocked = FeatureKeyHelper.isUnlocked(unlockedFeatures, "appointments") || viewModel.isFeatureUnlocked("appointments")
                MoreMenuItem(
                    title = "Agenda & Lembretes",
                    subtitle = "Pagamentos, recebimentos, reuniões e tarefas",
                    icon = Icons.Default.Event,
                    tag = "menu_agenda",
                    badge = if (!isAgendaUnlocked) "🔒 30 PTS" else null,
                    badgeColor = if (!isAgendaUnlocked) MaterialTheme.colorScheme.error else FluxPurplePrimary,
                    onClick = onNavigateToAgenda
                )
            }

            item {
                MoreMenuItem(
                    title = "Central de Notificações",
                    subtitle = "Alertas de vencimento, limites e novidades",
                    icon = Icons.Default.Notifications,
                    tag = "menu_notifications",
                    badge = if (unreadNotificationsCount > 0) "$unreadNotificationsCount" else null,
                    onClick = onNavigateToNotifications
                )
            }

            item {
                val isAiUnlocked = FeatureKeyHelper.isUnlocked(unlockedFeatures, "fluxoai") || viewModel.isFeatureUnlocked("fluxoai")
                MoreMenuItem(
                    title = "Assistente Inteligente (FluxAO IA)",
                    subtitle = "Análise de fluxo de caixa, dicas e insights",
                    icon = Icons.Default.AutoAwesome,
                    tag = "menu_ai",
                    badge = if (!isAiUnlocked) "🔒 80 PTS" else "IA",
                    badgeColor = if (!isAiUnlocked) MaterialTheme.colorScheme.error else FluxPurplePrimary,
                    onClick = onNavigateToAiAssistant
                )
            }

            item {
                Text(
                    text = "Faturamento & Entidades",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                )
            }

            item {
                MoreMenuItem(
                    title = "Contas Bancárias & Caixas",
                    subtitle = "BAI, BFA, BIC, Caixa Físico e Saldos",
                    icon = Icons.Default.AccountBalance,
                    tag = "menu_accounts",
                    onClick = onNavigateToAccounts
                )
            }

            item {
                val isClientsUnlocked = FeatureKeyHelper.isUnlocked(unlockedFeatures, "customers") || viewModel.isFeatureUnlocked("customers")
                MoreMenuItem(
                    title = "Clientes & Parceiros",
                    subtitle = "Cadastro comercial com NIF e contactos",
                    icon = Icons.Default.People,
                    tag = "menu_clients",
                    badge = if (!isClientsUnlocked) "🔒 30 PTS" else null,
                    badgeColor = if (!isClientsUnlocked) MaterialTheme.colorScheme.error else FluxPurplePrimary,
                    onClick = onNavigateToClients
                )
            }

            item {
                val isInvoicesUnlocked = FeatureKeyHelper.isUnlocked(unlockedFeatures, "invoices") || viewModel.isFeatureUnlocked("invoices")
                MoreMenuItem(
                    title = "Faturas Comerciais Pro",
                    subtitle = "Emissão de Faturas, Pró-Formas e Recibos em PDF",
                    icon = Icons.Default.ReceiptLong,
                    tag = "menu_invoices",
                    badge = if (!isInvoicesUnlocked) "🔒 50 PTS" else null,
                    badgeColor = if (!isInvoicesUnlocked) MaterialTheme.colorScheme.error else FluxPurplePrimary,
                    onClick = onNavigateToInvoices
                )
            }

            item {
                MoreMenuItem(
                    title = "Pontos & Recompensas FluxAO",
                    subtitle = "Ganhe pontos em ações reais ou anúncios e resgate recompensas",
                    icon = Icons.Default.Stars,
                    tag = "menu_points",
                    badge = "NOVO",
                    badgeColor = FluxGoldRewards,
                    onClick = onNavigateToPoints
                )
            }

            item {
                Text(
                    text = "Plataforma SaaS Multiempresa",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                )
            }

            item {
                MoreMenuItem(
                    title = "Painel Super Admin",
                    subtitle = "Gestão global de empresas, utilizadores e planos SaaS",
                    icon = Icons.Default.AdminPanelSettings,
                    tag = "menu_super_admin",
                    badge = "SAAS",
                    badgeColor = FluxPurplePrimary,
                    onClick = onNavigateToSuperAdmin
                )
            }

            item {
                MoreMenuItem(
                    title = "Área do Cliente (Portal)",
                    subtitle = "Visão do cliente para orçamentos, agendamentos e faturas",
                    icon = Icons.Default.Storefront,
                    tag = "menu_customer_portal",
                    onClick = onNavigateToCustomerPortal
                )
            }

            item {
                Text(
                    text = "Sistema",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                )
            }

            item {
                MoreMenuItem(
                    title = "Configurações",
                    subtitle = "Tema, moeda, backups e notificações",
                    icon = Icons.Default.Settings,
                    tag = "menu_settings",
                    onClick = onNavigateToSettings
                )
            }

            item {
                MoreMenuItem(
                    title = "Sair da Dashboard (Menu Inicial)",
                    subtitle = "Voltar para onde insere o Nome da Empresa, NIF e Contacto",
                    icon = Icons.AutoMirrored.Filled.ExitToApp,
                    tag = "menu_exit_to_initial",
                    badge = "Sair",
                    badgeColor = MaterialTheme.colorScheme.error,
                    onClick = onExitDashboard
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun MoreMenuItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    tag: String,
    badge: String? = null,
    badgeColor: Color = MaterialTheme.colorScheme.error,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .testTag(tag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(FluxPurplePrimary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = title, tint = FluxPurplePrimary, modifier = Modifier.size(22.dp))
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(text = subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            if (badge != null) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(badgeColor)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = badge,
                        color = if (badgeColor == FluxPurplePrimary) FluxWhite else Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            Icon(
                Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}
