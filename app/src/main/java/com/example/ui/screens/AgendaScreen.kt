package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.AppointmentEntity
import com.example.data.local.entity.BlockedSlotEntity
import com.example.data.local.entity.ClientEntity
import com.example.data.local.entity.NotificationEntity
import com.example.ui.theme.Emerald500
import com.example.ui.theme.FluxGoldPrimary
import com.example.ui.theme.FluxGoldVibrant
import com.example.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

// Utility for Africa/Luanda Timezone date formatting
fun getLuandaCalendar(timeMillis: Long = System.currentTimeMillis()): Calendar {
    return Calendar.getInstance(TimeZone.getTimeZone("Africa/Luanda"), Locale.getDefault()).apply {
        timeInMillis = timeMillis
    }
}

fun formatLuandaDate(timeMillis: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "AO")).apply {
        timeZone = TimeZone.getTimeZone("Africa/Luanda")
    }
    return sdf.format(Date(timeMillis))
}

val STANDARD_TIME_SLOTS = listOf(
    "08:00", "09:00", "10:00", "11:00", "12:00",
    "13:00", "14:00", "15:00", "16:00", "17:00", "18:00"
)

val SERVICE_TYPES = listOf(
    "Levantamento & Medição Técnica",
    "Fabricação & Corte de Peças",
    "Instalação & Montagem em Obra",
    "Manutenção & Assistência Técnica",
    "Consultoria & Reunião Comercial",
    "Entrega de Material",
    "Outro Serviço"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgendaScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(context, "Notificações nativas do smartphone ativadas!", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    val appointments by viewModel.allAppointments.collectAsStateWithLifecycle()
    val clients by viewModel.allClients.collectAsStateWithLifecycle()

    var viewModeTab by remember { mutableIntStateOf(0) } // 0 = Lista / Painel, 1 = Calendário Visual, 2 = Horários Bloqueados
    var searchQuery by remember { mutableStateOf("") }
    var selectedStatusFilter by remember { mutableStateOf<String?>(null) } // null = Todos
    var selectedDateFilter by remember { mutableStateOf<String?>(null) } // "TODAY", "TOMORROW", "THIS_WEEK", "THIS_MONTH", null

    var showCreateDialog by remember { mutableStateOf(false) }
    var selectedAppointmentForDetails by remember { mutableStateOf<AppointmentEntity?>(null) }
    var appointmentToReschedule by remember { mutableStateOf<AppointmentEntity?>(null) }
    var appointmentToCancel by remember { mutableStateOf<AppointmentEntity?>(null) }

    // Business Hours config state
    var openingHour by remember { mutableStateOf("08:00") }
    var closingHour by remember { mutableStateOf("18:00") }
    var defaultDurationMinutes by remember { mutableIntStateOf(60) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    // Filter Logic
    val filteredAppointments = remember(appointments, searchQuery, selectedStatusFilter, selectedDateFilter) {
        val nowCal = getLuandaCalendar()
        val todayStr = formatLuandaDate(nowCal.timeInMillis)

        val tomorrowCal = getLuandaCalendar().apply { add(Calendar.DAY_OF_YEAR, 1) }
        val tomorrowStr = formatLuandaDate(tomorrowCal.timeInMillis)

        val weekEndCal = getLuandaCalendar().apply { add(Calendar.DAY_OF_YEAR, 7) }

        appointments.filter { appt ->
            val matchesSearch = searchQuery.isBlank() ||
                    appt.clientName.contains(searchQuery, ignoreCase = true) ||
                    appt.serviceType.contains(searchQuery, ignoreCase = true) ||
                    appt.address.contains(searchQuery, ignoreCase = true) ||
                    appt.title.contains(searchQuery, ignoreCase = true)

            val matchesStatus = selectedStatusFilter == null || appt.status.equals(selectedStatusFilter, ignoreCase = true)

            val matchesDate = when (selectedDateFilter) {
                "TODAY" -> appt.formattedDate == todayStr
                "TOMORROW" -> appt.formattedDate == tomorrowStr
                "THIS_WEEK" -> appt.dateEpochMillis >= nowCal.timeInMillis - 86400000L && appt.dateEpochMillis <= weekEndCal.timeInMillis
                "THIS_MONTH" -> {
                    val apptCal = getLuandaCalendar(appt.dateEpochMillis)
                    apptCal.get(Calendar.MONTH) == nowCal.get(Calendar.MONTH) && apptCal.get(Calendar.YEAR) == nowCal.get(Calendar.YEAR)
                }
                else -> true
            }

            matchesSearch && matchesStatus && matchesDate
        }
    }

    val pendingCount = appointments.count { it.status == "SOLICITADO" || it.status == "REAGENDAMENTO_SOLICITADO" }
    val confirmedCount = appointments.count { it.status == "CONFIRMADO" }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Gestão de Agendamentos", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Fuso Horário: Africa/Luanda (GMT+1)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("agenda_back_btn")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    IconButton(onClick = { showSettingsDialog = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "Configurar Horários e Regras")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = FluxGoldVibrant,
                contentColor = Color.Black,
                modifier = Modifier.testTag("fab_create_appointment")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Novo Agendamento", modifier = Modifier.size(26.dp))
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Main Top Tabs: Lista & Dashboard, Calendário Visual
            TabRow(
                selectedTabIndex = viewModeTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = FluxGoldVibrant
            ) {
                Tab(
                    selected = viewModeTab == 0,
                    onClick = { viewModeTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Painel & Agendamentos", fontWeight = FontWeight.Bold)
                            if (pendingCount > 0) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFFF97316)
                                ) {
                                    Text(
                                        text = "$pendingCount",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                )

                Tab(
                    selected = viewModeTab == 1,
                    onClick = { viewModeTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Calendário Visual", fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }

            when (viewModeTab) {
                0 -> {
                    // List & Filters View
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Summary Cards
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    border = BorderStroke(1.dp, FluxGoldPrimary.copy(alpha = 0.2f)),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text("Total", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("${appointments.size}", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = Color(0xFFF97316).copy(alpha = 0.12f),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text("Pendentes", fontSize = 11.sp, color = Color(0xFFD97706))
                                        Text("$pendingCount", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color(0xFFD97706))
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = Emerald500.copy(alpha = 0.12f),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text("Confirmados", fontSize = 11.sp, color = Emerald500)
                                        Text("$confirmedCount", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Emerald500)
                                    }
                                }
                            }
                        }

                        // Search Bar
                        item {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text("Pesquisar por cliente, serviço, endereço...") },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                                trailingIcon = {
                                    if (searchQuery.isNotBlank()) {
                                        IconButton(onClick = { searchQuery = "" }) {
                                            Icon(Icons.Default.Clear, contentDescription = "Limpar")
                                        }
                                    }
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // Date Filters Row
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("Filtrar por Período:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    item {
                                        FilterChip(
                                            selected = selectedDateFilter == null,
                                            onClick = { selectedDateFilter = null },
                                            label = { Text("Todas Datas") }
                                        )
                                    }
                                    item {
                                        FilterChip(
                                            selected = selectedDateFilter == "TODAY",
                                            onClick = { selectedDateFilter = if (selectedDateFilter == "TODAY") null else "TODAY" },
                                            label = { Text("Hoje") }
                                        )
                                    }
                                    item {
                                        FilterChip(
                                            selected = selectedDateFilter == "TOMORROW",
                                            onClick = { selectedDateFilter = if (selectedDateFilter == "TOMORROW") null else "TOMORROW" },
                                            label = { Text("Amanhã") }
                                        )
                                    }
                                    item {
                                        FilterChip(
                                            selected = selectedDateFilter == "THIS_WEEK",
                                            onClick = { selectedDateFilter = if (selectedDateFilter == "THIS_WEEK") null else "THIS_WEEK" },
                                            label = { Text("Esta Semana") }
                                        )
                                    }
                                    item {
                                        FilterChip(
                                            selected = selectedDateFilter == "THIS_MONTH",
                                            onClick = { selectedDateFilter = if (selectedDateFilter == "THIS_MONTH") null else "THIS_MONTH" },
                                            label = { Text("Este Mês") }
                                        )
                                    }
                                }
                            }
                        }

                        // Status Filters Row
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("Filtrar por Status:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    item {
                                        FilterChip(
                                            selected = selectedStatusFilter == null,
                                            onClick = { selectedStatusFilter = null },
                                            label = { Text("Todos Status") }
                                        )
                                    }
                                    listOf(
                                        "SOLICITADO" to "Solicitados",
                                        "CONFIRMADO" to "Confirmados",
                                        "EM_ANDAMENTO" to "Em Andamento",
                                        "CONCLUIDO" to "Concluídos",
                                        "REAGENDAMENTO_SOLICITADO" to "Reagendamentos",
                                        "CANCELADO" to "Cancelados"
                                    ).forEach { (stKey, stLabel) ->
                                        item {
                                            FilterChip(
                                                selected = selectedStatusFilter == stKey,
                                                onClick = { selectedStatusFilter = if (selectedStatusFilter == stKey) null else stKey },
                                                label = { Text(stLabel) }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Appointments List
                        if (filteredAppointments.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = Icons.Default.Event,
                                            contentDescription = null,
                                            tint = FluxGoldPrimary,
                                            modifier = Modifier.size(52.dp)
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text("Nenhum agendamento encontrado", fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            "Clique no botão '+' para agendar uma nova medição, instalação ou serviço.",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                    }
                                }
                            }
                        } else {
                            items(filteredAppointments, key = { it.id }) { appt ->
                                AppointmentCardItem(
                                    appointment = appt,
                                    onViewDetails = { selectedAppointmentForDetails = appt },
                                    onConfirm = {
                                        val updated = appt.copy(status = "CONFIRMADO", historyLog = appt.historyLog + "\nConfirmado pelo Administrador em ${formatLuandaDate(System.currentTimeMillis())}")
                                        viewModel.saveAppointment(updated, context)
                                        Toast.makeText(context, "Agendamento confirmado!", Toast.LENGTH_SHORT).show()
                                    },
                                    onStart = {
                                        val updated = appt.copy(status = "EM_ANDAMENTO", historyLog = appt.historyLog + "\nIniciado pelo Administrador em ${formatLuandaDate(System.currentTimeMillis())}")
                                        viewModel.saveAppointment(updated, context)
                                        Toast.makeText(context, "Serviço em andamento!", Toast.LENGTH_SHORT).show()
                                    },
                                    onComplete = {
                                        val updated = appt.copy(status = "CONCLUIDO", historyLog = appt.historyLog + "\nConcluído em ${formatLuandaDate(System.currentTimeMillis())}")
                                        viewModel.saveAppointment(updated, context)
                                        Toast.makeText(context, "Agendamento concluído!", Toast.LENGTH_SHORT).show()
                                    },
                                    onRequestReschedule = { appointmentToReschedule = appt },
                                    onRequestCancellation = { appointmentToCancel = appt },
                                    onDelete = { viewModel.deleteAppointment(appt, context) }
                                )
                            }
                        }
                    }
                }

                1 -> {
                    // Modern Interactive Calendar View
                    ModernCalendarView(
                        appointments = appointments,
                        onSelectDateSlot = { dateMillis, timeSlot ->
                            showCreateDialog = true
                        },
                        onAppointmentClick = { appt ->
                            selectedAppointmentForDetails = appt
                        }
                    )
                }
            }
        }
    }

    // Create / New Appointment Dialog Form
    if (showCreateDialog) {
        CreateAppointmentDialog(
            clients = clients,
            openingHour = openingHour,
            closingHour = closingHour,
            onDismiss = { showCreateDialog = false },
            onConfirmAppointment = { clientName, clientPhone, clientId, serviceType, dateMillis, timeSlot, address, obs, uri, reminderEnabled, reminderMinutes ->
                scope.launch {
                    val conflict = viewModel.allAppointments.value.firstOrNull {
                        it.formattedDate == formatLuandaDate(dateMillis) &&
                                it.time == timeSlot &&
                                it.status != "CANCELADO"
                    }
                    if (conflict != null) {
                        Toast.makeText(context, "⚠️ Horário indisponível! Já existe agendamento de ${conflict.clientName} às $timeSlot.", Toast.LENGTH_LONG).show()
                        return@launch
                    }

                    val newAppt = AppointmentEntity(
                        clientId = clientId,
                        clientName = clientName,
                        clientPhone = clientPhone,
                        serviceType = serviceType,
                        dateEpochMillis = dateMillis,
                        time = timeSlot,
                        address = address,
                        observations = obs,
                        attachmentUri = uri,
                        status = "SOLICITADO",
                        title = "$serviceType - $clientName",
                        description = "Endereço: $address. Obs: $obs",
                        historyLog = "Criado e solicitado pelo cliente em ${formatLuandaDate(System.currentTimeMillis())}",
                        reminderEnabled = reminderEnabled,
                        reminderMinutes = reminderMinutes
                    )
                    viewModel.saveAppointment(newAppt, context)
                    showCreateDialog = false
                    Toast.makeText(context, "Agendamento solicitado com sucesso e alertas programados no telemóvel!", Toast.LENGTH_LONG).show()
                }
            }
        )
    }

    // Details & Timeline Dialog
    selectedAppointmentForDetails?.let { appt ->
        AppointmentDetailDialog(
            appointment = appt,
            onDismiss = { selectedAppointmentForDetails = null },
            onConfirm = {
                val updated = appt.copy(status = "CONFIRMADO", historyLog = appt.historyLog + "\nConfirmado em ${formatLuandaDate(System.currentTimeMillis())}")
                viewModel.saveAppointment(updated, context)
                selectedAppointmentForDetails = updated
                Toast.makeText(context, "Agendamento confirmado!", Toast.LENGTH_SHORT).show()
            },
            onStart = {
                val updated = appt.copy(status = "EM_ANDAMENTO", historyLog = appt.historyLog + "\nIniciado em ${formatLuandaDate(System.currentTimeMillis())}")
                viewModel.saveAppointment(updated, context)
                selectedAppointmentForDetails = updated
                Toast.makeText(context, "Status atualizado para Em Andamento!", Toast.LENGTH_SHORT).show()
            },
            onComplete = {
                val updated = appt.copy(status = "CONCLUIDO", historyLog = appt.historyLog + "\nConcluído em ${formatLuandaDate(System.currentTimeMillis())}")
                viewModel.saveAppointment(updated, context)
                selectedAppointmentForDetails = updated
                Toast.makeText(context, "Agendamento concluído com sucesso!", Toast.LENGTH_SHORT).show()
            },
            onRequestReschedule = {
                appointmentToReschedule = appt
            },
            onRequestCancel = {
                appointmentToCancel = appt
            }
        )
    }

    // Reschedule Request Dialog
    appointmentToReschedule?.let { appt ->
        RescheduleDialog(
            appointment = appt,
            onDismiss = { appointmentToReschedule = null },
            onRescheduleConfirm = { newDateMillis, newTimeSlot ->
                val updated = appt.copy(
                    status = "REAGENDAMENTO_SOLICITADO",
                    requestedRescheduleDateMillis = newDateMillis,
                    requestedRescheduleTime = newTimeSlot,
                    historyLog = appt.historyLog + "\nSolicitado Reagendamento para ${formatLuandaDate(newDateMillis)} às $newTimeSlot em ${formatLuandaDate(System.currentTimeMillis())}"
                )
                viewModel.saveAppointment(updated, context)
                appointmentToReschedule = null
                if (selectedAppointmentForDetails?.id == appt.id) {
                    selectedAppointmentForDetails = updated
                }
                Toast.makeText(context, "Solicitação de reagendamento enviada!", Toast.LENGTH_LONG).show()
            }
        )
    }

    // Cancel Request Dialog
    appointmentToCancel?.let { appt ->
        CancellationDialog(
            appointment = appt,
            onDismiss = { appointmentToCancel = null },
            onCancelConfirm = { reason ->
                val updated = appt.copy(
                    status = "CANCELADO",
                    cancellationReason = reason,
                    historyLog = appt.historyLog + "\nCancelado em ${formatLuandaDate(System.currentTimeMillis())}. Motivo: $reason"
                )
                viewModel.saveAppointment(updated, context)
                appointmentToCancel = null
                if (selectedAppointmentForDetails?.id == appt.id) {
                    selectedAppointmentForDetails = updated
                }
                Toast.makeText(context, "Agendamento cancelado.", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Admin Settings Dialog for Operating Hours & Rules
    if (showSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            title = { Text("Configurações do Agendamento") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Defina as regras de funcionamento e fuso horário para validação de disponibilidade.", style = MaterialTheme.typography.bodySmall)

                    OutlinedTextField(
                        value = openingHour,
                        onValueChange = { openingHour = it },
                        label = { Text("Horário de Abertura (Ex: 08:00)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = closingHour,
                        onValueChange = { closingHour = it },
                        label = { Text("Horário de Encerramento (Ex: 18:00)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = "$defaultDurationMinutes min",
                        onValueChange = {
                            val digits = it.filter { char -> char.isDigit() }
                            if (digits.isNotBlank()) defaultDurationMinutes = digits.toInt()
                        },
                        label = { Text("Duração Padrão por Serviço") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showSettingsDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = FluxGoldVibrant)) {
                    Text("Salvar Regras")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSettingsDialog = false }) {
                    Text("Fechar")
                }
            }
        )
    }
}

@Composable
fun AppointmentCardItem(
    appointment: AppointmentEntity,
    onViewDetails: () -> Unit,
    onConfirm: () -> Unit,
    onStart: () -> Unit,
    onComplete: () -> Unit,
    onRequestReschedule: () -> Unit,
    onRequestCancellation: () -> Unit,
    onDelete: () -> Unit
) {
    val (statusLabel, statusBg, statusFg) = when (appointment.status) {
        "SOLICITADO" -> Triple("SOLICITADO", Color(0xFFF97316).copy(alpha = 0.15f), Color(0xFFD97706))
        "CONFIRMADO" -> Triple("CONFIRMADO", Color(0xFF3B82F6).copy(alpha = 0.15f), Color(0xFF2563EB))
        "EM_ANDAMENTO" -> Triple("EM ANDAMENTO", Color(0xFF8B5CF6).copy(alpha = 0.15f), Color(0xFF7C3AED))
        "CONCLUIDO" -> Triple("CONCLUÍDO", Emerald500.copy(alpha = 0.15f), Emerald500)
        "REAGENDAMENTO_SOLICITADO" -> Triple("REAGENDAMENTO SOLICITADO", Color(0xFFF59E0B).copy(alpha = 0.15f), Color(0xFFB45309))
        "CANCELADO" -> Triple("CANCELADO", Color(0xFFEF4444).copy(alpha = 0.15f), Color(0xFFDC2626))
        else -> Triple(appointment.status, MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurface)
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onViewDetails() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Schedule, contentDescription = null, tint = FluxGoldVibrant, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${appointment.formattedDate} às ${appointment.time}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusBg
                ) {
                    Text(
                        text = statusLabel,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = statusFg,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = appointment.serviceType.ifBlank { appointment.title },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            if (appointment.clientName.isNotBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = appointment.clientName + if (appointment.clientPhone.isNotBlank()) " (${appointment.clientPhone})" else "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (appointment.address.isNotBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = appointment.address,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons Row according to status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                when (appointment.status) {
                    "SOLICITADO", "REAGENDAMENTO_SOLICITADO" -> {
                        Button(
                            onClick = onConfirm,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Emerald500),
                            modifier = Modifier.weight(1f).height(36.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Confirmar", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    "CONFIRMADO" -> {
                        Button(
                            onClick = onStart,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6)),
                            modifier = Modifier.weight(1f).height(36.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Iniciar", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    "EM_ANDAMENTO" -> {
                        Button(
                            onClick = onComplete,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Emerald500),
                            modifier = Modifier.weight(1f).height(36.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Concluir", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (appointment.status != "CANCELADO" && appointment.status != "CONCLUIDO") {
                    OutlinedButton(
                        onClick = onRequestReschedule,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).height(36.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        Icon(Icons.Default.Update, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reagendar", fontSize = 11.sp)
                    }

                    OutlinedButton(
                        onClick = onRequestCancellation,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                        modifier = Modifier.weight(1f).height(36.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Cancelar", fontSize = 11.sp)
                    }
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
fun ModernCalendarView(
    appointments: List<AppointmentEntity>,
    onSelectDateSlot: (Long, String) -> Unit,
    onAppointmentClick: (AppointmentEntity) -> Unit
) {
    var selectedMonthCal by remember { mutableStateOf(getLuandaCalendar()) }
    var selectedDateMillis by remember { mutableStateOf(System.currentTimeMillis()) }

    val daysInMonth = remember(selectedMonthCal) {
        val cal = selectedMonthCal.clone() as Calendar
        cal.set(Calendar.DAY_OF_MONTH, 1)
        val month = cal.get(Calendar.MONTH)
        val list = mutableListOf<Calendar?>()
        val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1
        for (i in 0 until firstDayOfWeek) {
            list.add(null)
        }
        while (cal.get(Calendar.MONTH) == month) {
            list.add(cal.clone() as Calendar)
            cal.add(Calendar.DAY_OF_MONTH, 1)
        }
        list
    }

    val monthYearLabel = remember(selectedMonthCal) {
        val sdf = SimpleDateFormat("MMMM yyyy", Locale("pt", "AO")).apply {
            timeZone = TimeZone.getTimeZone("Africa/Luanda")
        }
        sdf.format(selectedMonthCal.time)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Month Header Navigator
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    selectedMonthCal = (selectedMonthCal.clone() as Calendar).apply { add(Calendar.MONTH, -1) }
                }) {
                    Text("<", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }

                Text(
                    text = monthYearLabel.replaceFirstChar { it.uppercase() },
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                IconButton(onClick = {
                    selectedMonthCal = (selectedMonthCal.clone() as Calendar).apply { add(Calendar.MONTH, 1) }
                }) {
                    Text(">", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }
            }
        }

        // Days of week header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            listOf("Dom", "Seg", "Ter", "Qua", "Qui", "Sex", "Sáb").forEach { day ->
                Text(
                    text = day,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Days Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(daysInMonth) { dayCal ->
                if (dayCal == null) {
                    Box(modifier = Modifier.size(38.dp))
                } else {
                    val dateStr = formatLuandaDate(dayCal.timeInMillis)
                    val isSelected = formatLuandaDate(selectedDateMillis) == dateStr
                    val dayAppts = appointments.filter { it.formattedDate == dateStr && it.status != "CANCELADO" }

                    Surface(
                        shape = CircleShape,
                        color = when {
                            isSelected -> FluxGoldVibrant
                            dayAppts.isNotEmpty() -> FluxGoldPrimary.copy(alpha = 0.2f)
                            else -> Color.Transparent
                        },
                        modifier = Modifier
                            .size(38.dp)
                            .clickable { selectedDateMillis = dayCal.timeInMillis }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${dayCal.get(Calendar.DAY_OF_MONTH)}",
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected || dayAppts.isNotEmpty()) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurface
                                )
                                if (dayAppts.isNotEmpty() && !isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(4.dp)
                                            .clip(CircleShape)
                                            .background(FluxGoldVibrant)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Time slots for selected date
        Text(
            text = "Horários em ${formatLuandaDate(selectedDateMillis)}:",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )

        val selectedDayAppts = remember(appointments, selectedDateMillis) {
            val dateStr = formatLuandaDate(selectedDateMillis)
            appointments.filter { it.formattedDate == dateStr && it.status != "CANCELADO" }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(STANDARD_TIME_SLOTS) { slot ->
                val appt = selectedDayAppts.find { it.time == slot }
                val isOccupied = appt != null

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isOccupied) MaterialTheme.colorScheme.surfaceVariant else Emerald500.copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, if (isOccupied) FluxGoldPrimary.copy(alpha = 0.3f) else Emerald500.copy(alpha = 0.3f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (isOccupied && appt != null) {
                                onAppointmentClick(appt)
                            } else {
                                onSelectDateSlot(selectedDateMillis, slot)
                            }
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(16.dp), tint = if (isOccupied) FluxGoldVibrant else Emerald500)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(slot, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }

                        if (isOccupied && appt != null) {
                            Text(
                                text = "Ocupado: ${appt.clientName} (${appt.serviceType})",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        } else {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Emerald500.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "Disponível +",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Emerald500,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAppointmentDialog(
    clients: List<ClientEntity>,
    openingHour: String,
    closingHour: String,
    onDismiss: () -> Unit,
    onConfirmAppointment: (
        clientName: String,
        clientPhone: String,
        clientId: Long,
        serviceType: String,
        dateMillis: Long,
        timeSlot: String,
        address: String,
        obs: String,
        attachmentUri: String,
        reminderEnabled: Boolean,
        reminderMinutes: Int
    ) -> Unit
) {
    var selectedClient by remember { mutableStateOf<ClientEntity?>(null) }
    var customClientName by remember { mutableStateOf("") }
    var customClientPhone by remember { mutableStateOf("") }

    var serviceType by remember { mutableStateOf(SERVICE_TYPES.first()) }
    var selectedTimeSlot by remember { mutableStateOf(STANDARD_TIME_SLOTS.first()) }
    var address by remember { mutableStateOf("") }
    var obs by remember { mutableStateOf("") }
    var attachmentUri by remember { mutableStateOf("") }

    var reminderEnabled by remember { mutableStateOf(true) }
    var reminderMinutes by remember { mutableIntStateOf(60) } // Default 1 hour before

    var selectedDateMillis by remember { mutableStateOf(System.currentTimeMillis()) }

    var clientDropdownExpanded by remember { mutableStateOf(false) }
    var serviceDropdownExpanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Criar Agendamento", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Fechar")
                        }
                    }
                }

                // Client Selector
                item {
                    ExposedDropdownMenuBox(
                        expanded = clientDropdownExpanded,
                        onExpandedChange = { clientDropdownExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedClient?.name ?: customClientName.ifBlank { "Selecione o Cliente" },
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Cliente *") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = clientDropdownExpanded) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = clientDropdownExpanded,
                            onDismissRequest = { clientDropdownExpanded = false }
                        ) {
                            clients.forEach { c ->
                                DropdownMenuItem(
                                    text = { Text(c.name + " (${c.phone})") },
                                    onClick = {
                                        selectedClient = c
                                        address = c.address
                                        clientDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                if (selectedClient == null) {
                    item {
                        OutlinedTextField(
                            value = customClientName,
                            onValueChange = { customClientName = it },
                            label = { Text("Nome do Cliente") },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = customClientPhone,
                            onValueChange = { customClientPhone = it },
                            label = { Text("Telefone / WhatsApp") },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Service Type
                item {
                    ExposedDropdownMenuBox(
                        expanded = serviceDropdownExpanded,
                        onExpandedChange = { serviceDropdownExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = serviceType,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Tipo de Serviço *") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = serviceDropdownExpanded) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = serviceDropdownExpanded,
                            onDismissRequest = { serviceDropdownExpanded = false }
                        ) {
                            SERVICE_TYPES.forEach { srv ->
                                DropdownMenuItem(
                                    text = { Text(srv) },
                                    onClick = {
                                        serviceType = srv
                                        serviceDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Date & Time
                item {
                    Column {
                        Text("Data do Agendamento: ${formatLuandaDate(selectedDateMillis)}", style = MaterialTheme.typography.labelMedium)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Selecione o Horário Disponível:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(4.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(STANDARD_TIME_SLOTS) { slot ->
                                FilterChip(
                                    selected = selectedTimeSlot == slot,
                                    onClick = { selectedTimeSlot = slot },
                                    label = { Text(slot) }
                                )
                            }
                        }
                    }
                }

                // Address
                item {
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Endereço do Local de Atendimento *") },
                        placeholder = { Text("Rua, Bairro, Ponto de Referência em Luanda") },
                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Observations
                item {
                    OutlinedTextField(
                        value = obs,
                        onValueChange = { obs = it },
                        label = { Text("Observações / Detalhes Adicionais") },
                        minLines = 2,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Attachment Link
                item {
                    OutlinedTextField(
                        value = attachmentUri,
                        onValueChange = { attachmentUri = it },
                        label = { Text("Link / URI de Fotos ou Anexos") },
                        placeholder = { Text("Opcional: URL da foto da obra/planta") },
                        leadingIcon = { Icon(Icons.Default.AttachFile, contentDescription = null) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Native Smartphone Notifications Controls
                item {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(1.dp, FluxGoldPrimary.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Notifications, contentDescription = null, tint = FluxGoldVibrant, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Alertas Nativos no Telemóvel", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                                Switch(
                                    checked = reminderEnabled,
                                    onCheckedChange = { reminderEnabled = it },
                                    colors = SwitchDefaults.colors(checkedThumbColor = Color.Black, checkedTrackColor = FluxGoldVibrant)
                                )
                            }

                            if (reminderEnabled) {
                                Text(
                                    "O smartphone emitirá notificações automáticas 7 dias antes, 3 dias antes, 24h antes, 2h antes e no horário exato.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Text("Lembrete Antecipado Adicional:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    listOf(
                                        15 to "15 min antes",
                                        30 to "30 min antes",
                                        60 to "1 hora antes",
                                        120 to "2 horas antes",
                                        1440 to "24 horas antes"
                                    ).forEach { (mins, label) ->
                                        item {
                                            FilterChip(
                                                selected = reminderMinutes == mins,
                                                onClick = { reminderMinutes = mins },
                                                label = { Text(label, fontSize = 11.sp) }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            val finalName = selectedClient?.name ?: customClientName
                            val finalPhone = selectedClient?.phone ?: customClientPhone
                            val finalId = selectedClient?.id ?: 0L
                            if (finalName.isBlank()) {
                                return@Button
                            }
                            onConfirmAppointment(
                                finalName,
                                finalPhone,
                                finalId,
                                serviceType,
                                selectedDateMillis,
                                selectedTimeSlot,
                                address,
                                obs,
                                attachmentUri,
                                reminderEnabled,
                                reminderMinutes
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = FluxGoldVibrant),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text("Confirmar Agendamento", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun AppointmentDetailDialog(
    appointment: AppointmentEntity,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    onStart: () -> Unit,
    onComplete: () -> Unit,
    onRequestReschedule: () -> Unit,
    onRequestCancel: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Detalhes do Agendamento", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(appointment.serviceType, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Status Atual: ${appointment.displayStatusLabel}", fontWeight = FontWeight.SemiBold, color = FluxGoldVibrant)

                Spacer(modifier = Modifier.height(10.dp))

                Text("📅 Data & Hora: ${appointment.formattedDate} às ${appointment.time}")
                Text("👤 Cliente: ${appointment.clientName} (${appointment.clientPhone})")
                if (appointment.address.isNotBlank()) Text("📍 Endereço: ${appointment.address}")
                if (appointment.observations.isNotBlank()) Text("📝 Observações: ${appointment.observations}")
                if (appointment.attachmentUri.isNotBlank()) Text("📎 Anexo: ${appointment.attachmentUri}")

                Spacer(modifier = Modifier.height(12.dp))

                Text("Histórico & Timeline:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = appointment.historyLog.ifBlank { "Sem histórico de alterações." },
                        fontSize = 11.sp,
                        modifier = Modifier.padding(10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    when (appointment.status) {
                        "SOLICITADO", "REAGENDAMENTO_SOLICITADO" -> {
                            Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(containerColor = Emerald500), modifier = Modifier.weight(1f)) {
                                Text("Confirmar")
                            }
                        }
                        "CONFIRMADO" -> {
                            Button(onClick = onStart, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6)), modifier = Modifier.weight(1f)) {
                                Text("Em Andamento")
                            }
                        }
                        "EM_ANDAMENTO" -> {
                            Button(onClick = onComplete, colors = ButtonDefaults.buttonColors(containerColor = Emerald500), modifier = Modifier.weight(1f)) {
                                Text("Concluir")
                            }
                        }
                    }

                    if (appointment.status != "CANCELADO" && appointment.status != "CONCLUIDO") {
                        OutlinedButton(onClick = onRequestReschedule, modifier = Modifier.weight(1f)) {
                            Text("Reagendar")
                        }
                        OutlinedButton(onClick = onRequestCancel, colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error), modifier = Modifier.weight(1f)) {
                            Text("Cancelar")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RescheduleDialog(
    appointment: AppointmentEntity,
    onDismiss: () -> Unit,
    onRescheduleConfirm: (newDateMillis: Long, newTimeSlot: String) -> Unit
) {
    var newTimeSlot by remember { mutableStateOf(appointment.time) }
    var selectedDateMillis by remember { mutableStateOf(appointment.dateEpochMillis) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Solicitar Reagendamento") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Selecione a nova data e horário para o serviço do cliente ${appointment.clientName}:")

                Text("Nova Data: ${formatLuandaDate(selectedDateMillis)}", fontWeight = FontWeight.Bold)

                Text("Selecione o novo horário:")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(STANDARD_TIME_SLOTS) { slot ->
                        FilterChip(
                            selected = newTimeSlot == slot,
                            onClick = { newTimeSlot = slot },
                            label = { Text(slot) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onRescheduleConfirm(selectedDateMillis, newTimeSlot) }, colors = ButtonDefaults.buttonColors(containerColor = FluxGoldVibrant)) {
                Text("Confirmar Reagendamento")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun CancellationDialog(
    appointment: AppointmentEntity,
    onDismiss: () -> Unit,
    onCancelConfirm: (reason: String) -> Unit
) {
    var reason by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Solicitar Cancelamento") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Deseja cancelar o agendamento de ${appointment.clientName}?")
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Motivo do Cancelamento") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = { onCancelConfirm(reason.trim()) }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                Text("Confirmar Cancelamento")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Voltar")
            }
        }
    )
}
