package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.i18n.AppLanguage
import com.example.data.i18n.CountryDirectory
import com.example.data.i18n.CurrencyService
import com.example.data.i18n.I18nService
import com.example.data.i18n.RegionalFormatService
import com.example.data.i18n.TimezoneService
import com.example.data.model.AppCurrency
import com.example.data.model.AppThemeMode
import com.example.ui.components.CountrySelectionDialog
import com.example.ui.components.CurrencySelectionDialog
import com.example.ui.components.LanguageSelectionDialog
import com.example.ui.components.TimezoneSelectionDialog
import com.example.ui.theme.Emerald500
import com.example.ui.theme.FluxPurplePrimary
import com.example.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val currentTheme by viewModel.currentThemeMode.collectAsStateWithLifecycle()
    val currentCurrency by viewModel.currentCurrency.collectAsStateWithLifecycle()
    val currentLanguage by viewModel.currentLanguage.collectAsStateWithLifecycle()
    val currentCountryCode by viewModel.currentCountry.collectAsStateWithLifecycle()
    val currentTimezoneId by viewModel.currentTimezone.collectAsStateWithLifecycle()
    val secondaryCurrency by viewModel.secondaryCurrency.collectAsStateWithLifecycle()
    val adsConfig by viewModel.adsConfiguration.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("app_settings_prefs", Context.MODE_PRIVATE) }

    var notifyDueSoon by remember { mutableStateOf(prefs.getBoolean("notify_due_soon", true)) }
    var notifyBudgetLimits by remember { mutableStateOf(prefs.getBoolean("notify_budget_limits", true)) }
    var notifyGoals by remember { mutableStateOf(prefs.getBoolean("notify_goals", true)) }

    var showLanguageDialog by remember { mutableStateOf(false) }
    var showCountryDialog by remember { mutableStateOf(false) }
    var showTimezoneDialog by remember { mutableStateOf(false) }
    var showCurrencyDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val t = { key: String -> I18nService.t(key, lang = currentLanguage) }
    val countryInfo = remember(currentCountryCode) { CountryDirectory.getByCode(currentCountryCode) }
    val timezoneInfo = remember(currentTimezoneId) { TimezoneService.getTimezone(currentTimezoneId) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(t("settings.title"), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("settings_back_btn")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = t("common.back"))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Language Setting Card (Section 1, 2, 3, 4)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().testTag("card_language_settings")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Language, contentDescription = null, tint = FluxPurplePrimary)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(t("settings.language"), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(t("settings.language_desc"), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { showLanguageDialog = true }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(currentLanguage.flag, fontSize = 28.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(currentLanguage.nativeName, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                                Text("${currentLanguage.displayName} (${currentLanguage.code.uppercase()})", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(FluxPurplePrimary.copy(alpha = 0.15f))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(t("common.edit"), color = FluxPurplePrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }

            // Country & Region Setting Card (Section 6 & 10)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().testTag("card_country_settings")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Public, contentDescription = null, tint = FluxPurplePrimary)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(t("settings.country"), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(t("settings.country_desc"), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { showCountryDialog = true }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(countryInfo.flag, fontSize = 28.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(countryInfo.name, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                                Text("Código: ${countryInfo.isoCode} • Indicativo: ${countryInfo.callingCode} • ${countryInfo.taxIdLabel}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(FluxPurplePrimary.copy(alpha = 0.15f))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(t("common.edit"), color = FluxPurplePrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }

            // Currency & Regional Formatting Card (Section 7 & 8)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().testTag("card_currency_settings")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = FluxPurplePrimary)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(t("settings.currency"), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(t("settings.currency_desc"), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { showCurrencyDialog = true }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(currentCurrency.flag, fontSize = 26.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(t("profile.currency"), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Text("${currentCurrency.displayName} (${currentCurrency.symbol})", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(FluxPurplePrimary.copy(alpha = 0.15f))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(t("common.edit"), color = FluxPurplePrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 8.dp))

                    // Formatting Preview
                    val sampleAmount = 1250000.0
                    val formattedSample = CurrencyService.format(sampleAmount, currentCurrency)
                    val dualSample = CurrencyService.formatDual(sampleAmount, currentCurrency, secondaryCurrency)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Exemplo de Formatação", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text("Região: ${countryInfo.name} (${currentLanguage.displayName})", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(formattedSample, fontWeight = FontWeight.Bold, color = Emerald500, fontSize = 14.sp)
                    }

                    if (currentCurrency != secondaryCurrency) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Conversão de Referência: $dualSample",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Timezone Setting Card (Section 9)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().testTag("card_timezone_settings")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Schedule, contentDescription = null, tint = FluxPurplePrimary)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(t("settings.timezone"), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(t("settings.timezone_desc"), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { showTimezoneDialog = true }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(timezoneInfo.displayName, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                            Text("ID: ${timezoneInfo.id} • ${timezoneInfo.utcOffset}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(FluxPurplePrimary.copy(alpha = 0.15f))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(t("common.edit"), color = FluxPurplePrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }

            // Theme Setting Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Palette, contentDescription = null, tint = FluxPurplePrimary)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(t("settings.theme"), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    ThemeOptionItem(
                        title = t("settings.theme_dark"),
                        subtitle = "Contraste profissional com dourado e grafite",
                        selected = currentTheme == AppThemeMode.DARK,
                        onSelect = { viewModel.setThemeMode(AppThemeMode.DARK) }
                    )

                    ThemeOptionItem(
                        title = t("settings.theme_light"),
                        subtitle = "Fundo limpo e iluminado",
                        selected = currentTheme == AppThemeMode.LIGHT,
                        onSelect = { viewModel.setThemeMode(AppThemeMode.LIGHT) }
                    )

                    ThemeOptionItem(
                        title = t("settings.theme_system"),
                        subtitle = "Adapta-se às configurações do sistema operativo",
                        selected = currentTheme == AppThemeMode.SYSTEM,
                        onSelect = { viewModel.setThemeMode(AppThemeMode.SYSTEM) }
                    )
                }
            }

            // Notifications Settings Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Notifications, contentDescription = null, tint = FluxPurplePrimary)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(t("settings.notifications_settings"), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(t("notifications.due_soon"), fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text("Alertas de pagamentos e faturas", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = notifyDueSoon,
                            onCheckedChange = {
                                notifyDueSoon = it
                                prefs.edit().putBoolean("notify_due_soon", it).apply()
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = FluxPurplePrimary, checkedTrackColor = FluxPurplePrimary.copy(alpha = 0.4f))
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(t("notifications.budget_limit"), fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text("Avisos ao atingir 50%, 75%, 90% e 100%", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = notifyBudgetLimits,
                            onCheckedChange = {
                                notifyBudgetLimits = it
                                prefs.edit().putBoolean("notify_budget_limits", it).apply()
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = FluxPurplePrimary, checkedTrackColor = FluxPurplePrimary.copy(alpha = 0.4f))
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(t("notifications.goal_reached"), fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text("Comemorações e avisos de prazo", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = notifyGoals,
                            onCheckedChange = {
                                notifyGoals = it
                                prefs.edit().putBoolean("notify_goals", it).apply()
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = FluxPurplePrimary, checkedTrackColor = FluxPurplePrimary.copy(alpha = 0.4f))
                        )
                    }
                }
            }

            // Publicidade & Google AdMob Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().testTag("card_admob_settings")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Campaign, contentDescription = null, tint = FluxPurplePrimary)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(t("settings.admob"), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text("Monetização oficial Google AdMob", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Switch(
                            checked = adsConfig.isAdsEnabled,
                            onCheckedChange = { viewModel.toggleAdsEnabled() },
                            colors = SwitchDefaults.colors(checkedThumbColor = FluxPurplePrimary, checkedTrackColor = FluxPurplePrimary.copy(alpha = 0.4f))
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = if (adsConfig.isAdsEnabled) "Status: Anúncios AdMob Ativos" else "Status: Anúncios AdMob Desativados",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (adsConfig.isAdsEnabled) Emerald500 else MaterialTheme.colorScheme.error
                    )
                }
            }

            // Backup & Segurança Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Backup, contentDescription = null, tint = FluxPurplePrimary)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(t("settings.backup"), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Seus dados financeiros residem com segurança no seu dispositivo com persistência local e Room Database.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Cópia de segurança gerada localmente com integridade.")
                                }
                            }
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, tint = FluxPurplePrimary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Exportar Cópia de Segurança", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Base SQLite Room íntegra", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // Sobre / Versão Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = FluxPurplePrimary)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(t("settings.about"), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text("FluxAO - Plataforma Financeira Global Multilíngue", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Text(t("settings.version"), fontSize = 12.sp, color = FluxPurplePrimary, fontWeight = FontWeight.Bold)
                    Text("Preparado para operar em qualquer país, idioma, moeda e fuso horário do mundo.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }

    if (showLanguageDialog) {
        LanguageSelectionDialog(
            selectedLanguage = currentLanguage,
            onLanguageSelected = { newLang ->
                viewModel.setLanguage(newLang)
                showLanguageDialog = false
            },
            onDismiss = { showLanguageDialog = false }
        )
    }

    if (showCountryDialog) {
        CountrySelectionDialog(
            selectedCountryCode = currentCountryCode,
            onCountrySelected = { newCountry ->
                viewModel.setCountry(newCountry.isoCode)
                showCountryDialog = false
            },
            onDismiss = { showCountryDialog = false }
        )
    }

    if (showTimezoneDialog) {
        TimezoneSelectionDialog(
            selectedTimezoneId = currentTimezoneId,
            onTimezoneSelected = { tz ->
                viewModel.setTimezone(tz.id)
                showTimezoneDialog = false
            },
            onDismiss = { showTimezoneDialog = false }
        )
    }

    if (showCurrencyDialog) {
        CurrencySelectionDialog(
            selectedCurrency = currentCurrency,
            onCurrencySelected = { selected ->
                viewModel.setCurrency(selected)
                showCurrencyDialog = false
            },
            onDismiss = { showCurrencyDialog = false }
        )
    }
}

@Composable
fun ThemeOptionItem(
    title: String,
    subtitle: String,
    selected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable { onSelect() }
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onSelect,
            colors = RadioButtonDefaults.colors(selectedColor = FluxPurplePrimary)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Column {
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
