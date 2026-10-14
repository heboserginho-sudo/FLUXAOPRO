package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.service.AwardPointsResult
import com.example.data.service.RewardItem
import com.example.ui.theme.FluxGoldPrimary
import com.example.ui.theme.FluxGoldVibrant
import com.example.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@Composable
fun FeatureLockDialog(
    featureCode: String,
    featureTitle: String,
    featureDescription: String,
    pointsRequired: Int,
    durationDays: Int = 30,
    currentBalance: Int,
    viewModel: MainViewModel,
    onDismiss: () -> Unit,
    onUnlocked: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isProcessing by remember { mutableStateOf(false) }
    var isWatchingAd by remember { mutableStateOf(false) }
    var showCelebrationAnimation by remember { mutableStateOf(false) }

    val rewardItem = remember(featureCode, featureTitle, pointsRequired) {
        RewardItem(
            id = "rew_$featureCode",
            code = featureCode,
            title = featureTitle,
            description = featureDescription,
            pointsRequired = pointsRequired,
            durationDays = durationDays,
            category = "feature_unlock",
            iconName = when (featureCode) {
                "invoices" -> "ReceiptLong"
                "customers" -> "People"
                "appointments" -> "CalendarMonth"
                "reports" -> "Assessment"
                "fluxoai" -> "AutoAwesome"
                "whatsapp_automation" -> "Chat"
                else -> "Lock"
            }
        )
    }

    val iconVector = remember(featureCode) {
        when (featureCode) {
            "invoices" -> Icons.Default.ReceiptLong
            "customers" -> Icons.Default.People
            "appointments" -> Icons.Default.CalendarMonth
            "reports" -> Icons.Default.Assessment
            "fluxoai" -> Icons.Default.AutoAwesome
            "whatsapp_automation" -> Icons.Default.Chat
            else -> Icons.Default.Lock
        }
    }

    val hasEnoughPoints = currentBalance >= pointsRequired
    val missingPoints = (pointsRequired - currentBalance).coerceAtLeast(0)

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("dialog_feature_lock_$featureCode"),
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    FluxGoldVibrant.copy(alpha = 0.2f),
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                )
                            )
                        )
                        .border(2.dp, FluxGoldPrimary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = iconVector,
                        contentDescription = null,
                        tint = FluxGoldPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "$featureTitle Bloqueado",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Desbloqueie esta funcionalidade usando seus pontos de recompensa.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Stats Box
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Custo de Desbloqueio:",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "$pointsRequired PTS",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = FluxGoldPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Duração da Acesso:",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "$durationDays Dias",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Seu Saldo Atual:",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "$currentBalance PTS",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (hasEnoughPoints) Color(0xFF059669) else MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (!hasEnoughPoints) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Stars,
                                contentDescription = null,
                                tint = FluxGoldPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Pontos Insuficientes! Faltam $missingPoints PTS para desbloquear.",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (hasEnoughPoints) {
                    Button(
                        onClick = {
                            if (!isProcessing) {
                                isProcessing = true
                                scope.launch {
                                    val result = viewModel.redeemFeatureReward(rewardItem)
                                    isProcessing = false
                                    when (result) {
                                        is AwardPointsResult.Success -> {
                                            showCelebrationAnimation = true
                                        }
                                        is AwardPointsResult.AlreadyAwarded -> {
                                            showCelebrationAnimation = true
                                        }
                                        is AwardPointsResult.Failed -> {
                                            Toast.makeText(context, "Erro: ${result.reason}", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = FluxGoldPrimary),
                        shape = RoundedCornerShape(10.dp),
                        enabled = !isProcessing,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_unlock_feature_$featureCode")
                    ) {
                        if (isProcessing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Desbloqueando...")
                        } else {
                            Text("DESBLOQUEAR POR $pointsRequired PTS", fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    Button(
                        onClick = {
                            if (!isWatchingAd) {
                                isWatchingAd = true
                                scope.launch {
                                    viewModel.watchRewardedAd(
                                        context = context,
                                        onRewardEarned = { pointsEarned ->
                                            isWatchingAd = false
                                            Toast.makeText(context, "+$pointsEarned pontos recebidos!", Toast.LENGTH_SHORT).show()
                                        },
                                        onError = { error ->
                                            isWatchingAd = false
                                            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        shape = RoundedCornerShape(10.dp),
                        enabled = !isWatchingAd,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_watch_ad_unlock_$featureCode")
                    ) {
                        if (isWatchingAd) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Carregando anúncio...")
                        } else {
                            Icon(Icons.Default.PlayCircle, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Assistir anúncio (+10 PTS)", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("AGORA NÃO", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
        dismissButton = null
    )

    if (showCelebrationAnimation) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = {
                showCelebrationAnimation = false
                onUnlocked()
            },
            properties = androidx.compose.ui.window.DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnBackPress = true,
                dismissOnClickOutside = false
            )
        ) {
            ModuleUnlockAnimationOverlay(
                featureTitle = featureTitle,
                pointsConsumed = pointsRequired,
                durationDays = durationDays,
                iconVector = iconVector,
                onContinue = {
                    showCelebrationAnimation = false
                    onUnlocked()
                }
            )
        }
    }
}
