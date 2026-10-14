package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Save
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.i18n.CountryDirectory
import com.example.data.i18n.I18nService
import com.example.data.i18n.InternationalPhoneHelper
import com.example.ui.components.CountrySelectionDialog
import com.example.data.model.AppThemeMode
import com.example.ui.components.InvoiceCustomizationDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.CompanySubscription
import com.example.ui.theme.FluxGoldPrimary
import com.example.ui.theme.FluxGoldVibrant
import com.example.ui.viewmodel.MainViewModel

@Composable
fun CompanySetupAndPaymentScreen(
    subscription: CompanySubscription,
    viewModel: MainViewModel,
    canDismiss: Boolean = true,
    onContinueToApp: () -> Unit,
    onExitApp: () -> Unit = {}
) {
    val context = LocalContext.current

    var companyName by remember(subscription.companyName) { mutableStateOf(subscription.companyName) }
    var nif by remember(subscription.nif) { mutableStateOf(subscription.nif) }
    var contactPhone by remember(subscription.contactPhone) { mutableStateOf(subscription.contactPhone) }

    var nameError by remember { mutableStateOf<String?>(null) }
    var nifError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }

    val isFormComplete = companyName.trim().isNotBlank() && nif.trim().isNotBlank() && contactPhone.trim().isNotBlank()

    var showCustomizationDialog by remember { mutableStateOf(false) }
    val customization by viewModel.invoiceCustomization.collectAsStateWithLifecycle()
    val themeMode by viewModel.currentThemeMode.collectAsStateWithLifecycle()

    val currentCountryCode by viewModel.currentCountry.collectAsStateWithLifecycle()
    val countryConfig = remember(currentCountryCode) { CountryDirectory.getCountryConfiguration(currentCountryCode) }
    var showCountryDialog by remember { mutableStateOf(false) }

    fun validate(): Boolean {
        var isValid = true
        if (companyName.trim().isBlank()) {
            nameError = "O Nome da Empresa é obrigatório"
            isValid = false
        } else {
            nameError = null
        }

        if (nif.trim().isBlank()) {
            nifError = "O ${countryConfig.taxIdLabel} é obrigatório"
            isValid = false
        } else {
            nifError = null
        }

        if (contactPhone.trim().isBlank()) {
            phoneError = "O Contacto é obrigatório"
            isValid = false
        } else {
            phoneError = null
        }

        return isValid
    }

    fun saveCompanyProfile() {
        if (!validate()) {
            Toast.makeText(
                context,
                "Preencha os dados obrigatórios da empresa.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val finalName = companyName.trim()
        val finalNif = nif.trim()
        val finalPhone = contactPhone.trim()

        val e164 = InternationalPhoneHelper.formatToE164(countryConfig.phoneCode, finalPhone)

        viewModel.setCountry(countryConfig.countryCode)
        viewModel.updateCompanyInfo(
            companyName = finalName,
            nif = finalNif,
            contactPhone = e164
        )
        Toast.makeText(context, "Dados da empresa gravados com sucesso para ${countryConfig.countryName}!", Toast.LENGTH_SHORT).show()
        onContinueToApp()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(24.dp))
            // Header Hero
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(1.dp, FluxGoldPrimary.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, FluxGoldPrimary.copy(alpha = 0.3f)),
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .clickable { showCountryDialog = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(countryConfig.flag, fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "🌍 ${countryConfig.countryName} (${countryConfig.countryCode}) • Autoridade: ${countryConfig.taxAuthority}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = FluxGoldVibrant
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = Color(0xFF10B981).copy(alpha = 0.15f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${countryConfig.currency} (${countryConfig.currencySymbol})",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF10B981)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, FluxGoldPrimary.copy(alpha = 0.3f)),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .clickable {
                                        if (canDismiss) {
                                            onContinueToApp()
                                        } else {
                                            onExitApp()
                                        }
                                    }
                                    .testTag("btn_close_company_screen")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (canDismiss) Icons.AutoMirrored.Filled.ArrowBack else Icons.Default.Close,
                                        contentDescription = if (canDismiss) "Voltar" else "Sair do App",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (canDismiss) "Voltar" else "Sair do App",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.flux_ao_icon),
                            contentDescription = "FluxAO",
                            modifier = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .border(1.dp, FluxGoldPrimary, RoundedCornerShape(14.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Flux",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "AO Global",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Black,
                                    color = FluxGoldVibrant
                                )
                            }
                            Text(
                                text = "Identificação da Empresa • Sistema Multipaís",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Insira os dados da sua empresa para acessar o Dashboard de gestão financeira e faturação.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Section: Company Profile Form
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Apartment,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Identificação da Empresa",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Jurisdição Fiscal: ${countryConfig.countryName} • Autoridade: ${countryConfig.taxAuthority}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isFormComplete) {
                            Color(0xFF10B981).copy(alpha = 0.12f)
                        } else {
                            MaterialTheme.colorScheme.error.copy(alpha = 0.12f)
                        },
                        border = BorderStroke(
                            1.dp,
                            if (isFormComplete) Color(0xFF10B981).copy(alpha = 0.35f) else MaterialTheme.colorScheme.error.copy(alpha = 0.35f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isFormComplete) Icons.Default.CheckCircle else Icons.Default.Lock,
                                contentDescription = null,
                                tint = if (isFormComplete) Color(0xFF10B981) else MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isFormComplete) {
                                    "Todos os dados preenchidos! Acesso liberado para o Painel."
                                } else {
                                    "Acesso restrito: Preencha Nome, ${countryConfig.taxIdLabel} e Contacto (${countryConfig.phoneCode})."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isFormComplete) Color(0xFF10B981) else MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = companyName,
                        onValueChange = {
                            companyName = it
                            if (it.trim().isNotBlank()) nameError = null
                        },
                        isError = nameError != null,
                        supportingText = {
                            if (nameError != null) {
                                Text(nameError!!, color = MaterialTheme.colorScheme.error)
                            }
                        },
                        label = { Text("Nome da Empresa / Razão Social *") },
                        placeholder = { Text("Ex: Sociedade Comercial Lda / LLC") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Badge, contentDescription = null)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_company_name"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = nif,
                            onValueChange = {
                                nif = it
                                if (it.trim().isNotBlank()) nifError = null
                            },
                            isError = nifError != null,
                            supportingText = {
                                if (nifError != null) {
                                    Text(nifError!!, color = MaterialTheme.colorScheme.error)
                                }
                            },
                            label = { Text("${countryConfig.taxIdLabel} *") },
                            placeholder = { Text(countryConfig.taxIdType) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        Row(
                            modifier = Modifier.weight(1.2f),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .height(56.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { showCountryDialog = true }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(countryConfig.flag, fontSize = 16.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(countryConfig.phoneCode, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            OutlinedTextField(
                                value = contactPhone,
                                onValueChange = {
                                    contactPhone = it
                                    if (it.trim().isNotBlank()) phoneError = null
                                },
                                isError = phoneError != null,
                                supportingText = {
                                    if (phoneError != null) {
                                        Text(phoneError!!, color = MaterialTheme.colorScheme.error)
                                    }
                                },
                                label = { Text("Contacto *") },
                                placeholder = { Text(countryConfig.phoneSample.ifBlank { "000 000 000" }) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedButton(
                        onClick = { showCustomizationDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, FluxGoldPrimary.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_open_customization_from_company_screen")
                    ) {
                        Icon(imageVector = Icons.Default.Palette, contentDescription = null, tint = FluxGoldVibrant, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Personalizar Logótipo & Layout (${countryConfig.countryName})", fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { saveCompanyProfile() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isFormComplete) FluxGoldVibrant else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (isFormComplete) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("btn_save_company_profile")
                    ) {
                        Icon(
                            imageVector = if (isFormComplete) Icons.AutoMirrored.Filled.ArrowForward else Icons.Default.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Entrar no Painel (Dashboard)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    if (companyName.isNotBlank() || nif.isNotBlank() || contactPhone.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = {
                                companyName = ""
                                nif = ""
                                contactPhone = ""
                                nameError = null
                                nifError = null
                                phoneError = null
                                viewModel.updateCompanyInfo("", "", "")
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_clear_company_fields")
                        ) {
                            Text("Limpar Campos / Nova Empresa", fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // Section: Aparência e Tema Visual (Claro / Escuro / Sistema)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_theme_selector")
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = when (themeMode) {
                                            AppThemeMode.DARK -> Icons.Default.DarkMode
                                            AppThemeMode.LIGHT -> Icons.Default.LightMode
                                            AppThemeMode.SYSTEM -> Icons.Default.BrightnessAuto
                                        },
                                        contentDescription = null,
                                        tint = FluxGoldVibrant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Aparência & Tema",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Escolha o modo visual da interface",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ) {
                            Text(
                                text = themeMode.label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AppThemeMode.values().forEach { mode ->
                            val isSelected = themeMode == mode
                            val icon = when (mode) {
                                AppThemeMode.DARK -> Icons.Default.DarkMode
                                AppThemeMode.LIGHT -> Icons.Default.LightMode
                                AppThemeMode.SYSTEM -> Icons.Default.BrightnessAuto
                            }
                            OutlinedButton(
                                onClick = { viewModel.setThemeMode(mode) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_theme_mode_${mode.name.lowercase()}"),
                                shape = RoundedCornerShape(12.dp),
                                colors = if (isSelected) {
                                    ButtonDefaults.outlinedButtonColors(
                                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                        contentColor = FluxGoldVibrant
                                    )
                                } else {
                                    ButtonDefaults.outlinedButtonColors(
                                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                border = BorderStroke(
                                    if (isSelected) 1.5.dp else 1.dp,
                                    if (isSelected) FluxGoldVibrant else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                ),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 10.dp)
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = when (mode) {
                                            AppThemeMode.DARK -> "Escuro"
                                            AppThemeMode.LIGHT -> "Claro"
                                            AppThemeMode.SYSTEM -> "Sistema"
                                        },
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section: Activated Modules Overview
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Módulos Disponíveis",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Todos os recursos do sistema estão 100% liberados",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    val features = listOf(
                        "Fluxo de Caixa em tempo real (Receitas, Despesas e Saldos)",
                        "Faturas Certificadas AGT (FT, FR, FP, NC) com Hash e IVA",
                        "Gestão de Clientes (CRM com ligação direta e WhatsApp)",
                        "Personalização de Faturas com Logótipo da Empresa e Cores",
                        "Engenharia de Custos e Formação de Preço por Markup",
                        "Elaboração de Orçamentos e Propostas em PDF / WhatsApp",
                        "Demonstrativo de Resultado do Exercício (DRE) e Relatórios"
                    )

                    features.forEach { feature ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier
                                    .size(18.dp)
                                    .padding(top = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = feature,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedButton(
                        onClick = { saveCompanyProfile() },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_continue_to_dashboard")
                    ) {
                        Text("Ir para o Painel Principal", fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
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

    if (showCustomizationDialog) {
        InvoiceCustomizationDialog(
            customization = customization,
            onDismiss = { showCustomizationDialog = false },
            onPickLogo = { uri ->
                viewModel.saveInvoiceLogo(uri) { /* handled */ }
            },
            onRemoveLogo = {
                viewModel.removeInvoiceLogo()
            },
            onSaveCustomization = { accentColor, style, footer, showLogo, showWatermark, showQr, bankInfo ->
                viewModel.updateInvoiceCustomization(
                    accentColorHex = accentColor,
                    templateStyle = style,
                    customFooterNotes = footer,
                    showLogoOnInvoice = showLogo,
                    showWatermark = showWatermark,
                    showQrCode = showQr,
                    bankAccountInfo = bankInfo
                )
            }
        )
    }
}
