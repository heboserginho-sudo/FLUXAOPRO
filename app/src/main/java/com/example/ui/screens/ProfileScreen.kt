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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.i18n.CountryDirectory
import com.example.data.i18n.I18nService
import com.example.data.i18n.InternationalPhoneHelper
import com.example.ui.components.CountrySelectionDialog
import com.example.ui.components.LanguageSelectionDialog
import com.example.ui.theme.Emerald500
import com.example.ui.theme.FluxGoldPrimary
import com.example.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("user_profile_prefs", Context.MODE_PRIVATE) }
    val subscription by viewModel.subscription.collectAsStateWithLifecycle()
    val currency by viewModel.currentCurrency.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val projects by viewModel.allProjects.collectAsStateWithLifecycle()
    val clients by viewModel.allClients.collectAsStateWithLifecycle()
    val currentLanguage by viewModel.currentLanguage.collectAsStateWithLifecycle()
    val currentCountryCode by viewModel.currentCountry.collectAsStateWithLifecycle()
    val currentTimezoneId by viewModel.currentTimezone.collectAsStateWithLifecycle()

    val t = { key: String -> I18nService.t(key, lang = currentLanguage) }
    val countryInfo = remember(currentCountryCode) { CountryDirectory.getByCode(currentCountryCode) }

    var companyName by remember { mutableStateOf(subscription.companyName.ifBlank { prefs.getString("profile_name", "Minha Empresa") ?: "Minha Empresa" }) }
    var nif by remember { mutableStateOf(subscription.nif.ifBlank { prefs.getString("profile_nif", "5412893012") ?: "5412893012" }) }
    var phone by remember { mutableStateOf(subscription.contactPhone.ifBlank { prefs.getString("profile_phone", countryInfo.phoneSample.ifBlank { "923 000 000" }) ?: "923 000 000" }) }
    var email by remember { mutableStateOf(prefs.getString("profile_email", "contato@empresa.com") ?: "contato@empresa.com") }
    var iban by remember { mutableStateOf(subscription.destinationIban.ifBlank { prefs.getString("profile_iban", "0006 0000 43818047301 44") ?: "0006 0000 43818047301 44" }) }

    // International Address fields
    var addressStreet by remember { mutableStateOf(prefs.getString("profile_street", "") ?: "") }
    var addressCity by remember { mutableStateOf(prefs.getString("profile_city", "") ?: "") }
    var addressState by remember { mutableStateOf(prefs.getString("profile_state", "") ?: "") }
    var addressPostalCode by remember { mutableStateOf(prefs.getString("profile_postal_code", "") ?: "") }

    var showCountryDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(t("profile.title"), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("profile_back_btn")) {
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
            // Header Card
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(FluxGoldPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = companyName.take(2).uppercase().ifBlank { "FX" },
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = companyName.ifBlank { "FluxAO Gestão" },
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(countryInfo.flag, fontSize = 18.sp)
                        }
                        Text(
                            text = "${countryInfo.taxIdLabel}: ${nif.ifBlank { "Consumidor Final" }}",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = Emerald500, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("100% Local • Supabase Multi-Country Ready", fontSize = 11.sp, color = Emerald500, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // Quick Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Lançamentos", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${uiState.transactions.size}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Projetos", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${projects.size}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Clientes", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${clients.size}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }

            // Form Inputs
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(t("profile.organization_data"), fontWeight = FontWeight.Bold, fontSize = 15.sp)

                    // Country Selector
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showCountryDialog = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(countryInfo.flag, fontSize = 24.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(t("profile.country"), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("${countryInfo.name} (${countryInfo.isoCode})", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                            }
                            Text(t("common.edit"), color = FluxGoldPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    OutlinedTextField(
                        value = companyName,
                        onValueChange = { companyName = it },
                        label = { Text(t("profile.company_name")) },
                        leadingIcon = { Icon(Icons.Default.Business, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = nif,
                        onValueChange = { nif = it },
                        label = { Text("${countryInfo.taxIdLabel} (${t("profile.tax_id")})") },
                        leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Phone with country dial code
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .height(56.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { showCountryDialog = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(countryInfo.flag, fontSize = 18.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(countryInfo.callingCode, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }

                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text(t("profile.phone")) },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text(t("profile.email")) },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = iban,
                        onValueChange = { iban = it },
                        label = { Text(t("profile.iban")) },
                        leadingIcon = { Icon(Icons.Default.AccountBalance, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(t("profile.address"), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    OutlinedTextField(
                        value = addressStreet,
                        onValueChange = { addressStreet = it },
                        label = { Text("Rua, Número e Complemento") },
                        leadingIcon = { Icon(Icons.Default.Home, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = addressCity,
                            onValueChange = { addressCity = it },
                            label = { Text("Cidade / Distrito") },
                            leadingIcon = { Icon(Icons.Default.LocationCity, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = addressPostalCode,
                            onValueChange = { addressPostalCode = it },
                            label = { Text(countryInfo.postalCodeLabel) },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Moeda Principal Ativa:", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${currency.displayName} (${currency.symbol})", fontWeight = FontWeight.Bold, color = FluxGoldPrimary)
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = {
                            prefs.edit()
                                .putString("profile_name", companyName.trim())
                                .putString("profile_nif", nif.trim())
                                .putString("profile_phone", phone.trim())
                                .putString("profile_email", email.trim())
                                .putString("profile_iban", iban.trim())
                                .putString("profile_street", addressStreet.trim())
                                .putString("profile_city", addressCity.trim())
                                .putString("profile_postal_code", addressPostalCode.trim())
                                .apply()

                            val e164 = InternationalPhoneHelper.formatToE164(countryInfo.callingCode, phone.trim())

                            viewModel.updateCompanySubscription(
                                companyName = companyName.trim(),
                                nif = nif.trim(),
                                contactPhone = e164,
                                destinationIban = iban.trim()
                            )

                            scope.launch {
                                snackbarHostState.showSnackbar(t("profile.saved_success"))
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = FluxGoldPrimary, contentColor = Color.Black),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(t("profile.save_changes"), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }
        }
    }

    if (showCountryDialog) {
        CountrySelectionDialog(
            selectedCountryCode = currentCountryCode,
            onCountrySelected = { selected ->
                viewModel.setCountry(selected.isoCode)
                showCountryDialog = false
            },
            onDismiss = { showCountryDialog = false }
        )
    }

    if (showLanguageDialog) {
        LanguageSelectionDialog(
            selectedLanguage = currentLanguage,
            onLanguageSelected = { selected ->
                viewModel.setLanguage(selected)
                showLanguageDialog = false
            },
            onDismiss = { showLanguageDialog = false }
        )
    }
}
