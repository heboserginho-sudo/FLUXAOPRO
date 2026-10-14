package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.Emerald500

@Composable
fun AuthScreen(
    onLoginSuccess: (email: String, role: String, companyId: String) -> Unit,
    onRegisterCompanySuccess: (companyName: String, adminName: String, email: String) -> Unit,
    onRegisterCustomerSuccess: (fullName: String, email: String) -> Unit
) {
    val context = LocalContext.current
    var authTab by remember { mutableStateOf(0) } // 0: Entrar, 1: Criar Empresa, 2: Registo Cliente

    // Login State
    var loginEmail by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    // Register Company State
    var companyName by remember { mutableStateOf("") }
    var adminName by remember { mutableStateOf("") }
    var companyEmail by remember { mutableStateOf("") }
    var companyPhone by remember { mutableStateOf("") }
    var regPassword by remember { mutableStateOf("") }

    // Register Customer State
    var customerName by remember { mutableStateOf("") }
    var customerEmail by remember { mutableStateOf("") }
    var customerPhone by remember { mutableStateOf("") }
    var customerCompanySlug by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp)
                .testTag("auth_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Logo / Branding
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "FluxAO Business",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "FLUXAO BUSINESS",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Autenticação Supabase & Plataforma Multiempresa",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Tabs
                TabRow(
                    selectedTabIndex = authTab,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.clip(RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = authTab == 0,
                        onClick = { authTab = 0 },
                        text = { Text("Entrar", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                    )
                    Tab(
                        selected = authTab == 1,
                        onClick = { authTab = 1 },
                        text = { Text("Nova Empresa", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                    )
                    Tab(
                        selected = authTab == 2,
                        onClick = { authTab = 2 },
                        text = { Text("Sou Cliente", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                when (authTab) {
                    0 -> {
                        // TAB ENTRAR (LOGIN)
                        OutlinedTextField(
                            value = loginEmail,
                            onValueChange = { loginEmail = it },
                            label = { Text("E-mail do Utilizador") },
                            placeholder = { Text("ex: admin@shmoveis.co.ao") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("input_login_email")
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = loginPassword,
                            onValueChange = { loginPassword = it },
                            label = { Text("Palavra-passe") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            trailingIcon = {
                                IconButton(onClick = { showPassword = !showPassword }) {
                                    Icon(
                                        imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Ver Palavra-passe"
                                    )
                                }
                            },
                            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("input_login_password")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Quick Role Test Helpers
                        Text(
                            text = "Acesso Rápido para Testes Multiempresa:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            TextButton(onClick = { loginEmail = "superadmin@fluxao.co.ao"; loginPassword = "admin" }) {
                                Text("Super Admin", fontSize = 10.sp)
                            }
                            TextButton(onClick = { loginEmail = "admin@shmoveis.co.ao"; loginPassword = "admin" }) {
                                Text("Admin Empresa A", fontSize = 10.sp)
                            }
                            TextButton(onClick = { loginEmail = "cliente@exemplo.co.ao"; loginPassword = "admin" }) {
                                Text("Cliente", fontSize = 10.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                if (loginEmail.isBlank()) {
                                    Toast.makeText(context, "Insira o seu e-mail", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                val role = when {
                                    loginEmail.contains("super", ignoreCase = true) -> "super_admin"
                                    loginEmail.contains("client", ignoreCase = true) || loginEmail.contains("customer", ignoreCase = true) -> "customer"
                                    loginEmail.contains("func", ignoreCase = true) -> "employee"
                                    else -> "company_admin"
                                }
                                val companyId = if (role == "super_admin") "" else "company_sh_moveis"
                                onLoginSuccess(loginEmail, role, companyId)
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("btn_login_submit"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Iniciar Sessão", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }

                    1 -> {
                        // TAB REGISTO DA EMPRESA (COMPANY ADMIN)
                        OutlinedTextField(
                            value = companyName,
                            onValueChange = { companyName = it },
                            label = { Text("Nome da Empresa") },
                            placeholder = { Text("Ex: SH Móveis, Barbearia Vip") },
                            leadingIcon = { Icon(Icons.Default.Apartment, contentDescription = null) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("input_company_name")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = adminName,
                            onValueChange = { adminName = it },
                            label = { Text("Nome do Administrador") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("input_admin_name")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = companyEmail,
                            onValueChange = { companyEmail = it },
                            label = { Text("E-mail Comercial") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("input_company_email")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = companyPhone,
                            onValueChange = { companyPhone = it },
                            label = { Text("Telefone / WhatsApp (+244)") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("input_company_phone")
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                if (companyName.isBlank() || adminName.isBlank() || companyEmail.isBlank()) {
                                    Toast.makeText(context, "Preencha os campos obrigatórios da empresa", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                onRegisterCompanySuccess(companyName, adminName, companyEmail)
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("btn_register_company"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Criar Empresa Multiempresa", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }

                    2 -> {
                        // TAB REGISTO CLIENTE (CUSTOMER PORTAL)
                        OutlinedTextField(
                            value = customerName,
                            onValueChange = { customerName = it },
                            label = { Text("Nome Completo") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("input_customer_name")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = customerEmail,
                            onValueChange = { customerEmail = it },
                            label = { Text("Seu E-mail") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("input_customer_email")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = customerPhone,
                            onValueChange = { customerPhone = it },
                            label = { Text("Contacto WhatsApp") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("input_customer_phone")
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                if (customerName.isBlank() || customerEmail.isBlank()) {
                                    Toast.makeText(context, "Preencha o seu nome e e-mail", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                onRegisterCustomerSuccess(customerName, customerEmail)
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("btn_register_customer"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Emerald500)
                        ) {
                            Text("Criar Conta de Cliente", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                }
            }
        }
    }
}
