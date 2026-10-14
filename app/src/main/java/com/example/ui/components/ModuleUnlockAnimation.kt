package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.FluxGoldPrimary
import com.example.ui.theme.FluxGoldVibrant
import kotlinx.coroutines.delay
import kotlin.random.Random

private data class ConfettiParticle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var color: Color,
    var size: Float,
    var rotation: Float,
    var vRot: Float,
    var alpha: Float,
    val shapeType: Int // 0: circle, 1: rect
)

@Composable
fun ModuleUnlockAnimationOverlay(
    featureTitle: String,
    pointsConsumed: Int,
    durationDays: Int,
    iconVector: ImageVector,
    onContinue: () -> Unit
) {
    val scaleAnim = remember { Animatable(0.2f) }
    val glowPulseTransition = rememberInfiniteTransition(label = "glow_pulse")
    
    val pulseScale by glowPulseTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val pulseAlpha by glowPulseTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    var autoContinueProgress by remember { mutableFloatStateOf(1f) }
    var isTimerActive by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        // Spring bounce entry animation
        scaleAnim.animateTo(
            targetValue = 1.0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )

        // 3.5 seconds auto-continue progress countdown
        val totalMs = 3500L
        val stepMs = 50L
        var elapsedMs = 0L

        while (elapsedMs < totalMs && isTimerActive) {
            delay(stepMs)
            elapsedMs += stepMs
            autoContinueProgress = 1f - (elapsedMs.toFloat() / totalMs.toFloat())
        }

        if (isTimerActive) {
            onContinue()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.75f))
            .testTag("overlay_module_unlocked_animation"),
        contentAlignment = Alignment.Center
    ) {
        // Particle Confetti Effect
        ConfettiParticleCanvas()

        // Card Container
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .scale(scaleAnim.value)
                .testTag("card_unlocked_celebration"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
            border = androidx.compose.foundation.BorderStroke(
                2.dp,
                Brush.horizontalGradient(
                    colors = listOf(
                        FluxGoldVibrant,
                        Color(0xFF10B981),
                        FluxGoldPrimary
                    )
                )
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Animated Icon with Glowing Rings
                Box(
                    modifier = Modifier.size(110.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Pulsing Outer Aura
                    Box(
                        modifier = Modifier
                            .size(90.dp * pulseScale)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        FluxGoldVibrant.copy(alpha = pulseAlpha),
                                        Color(0xFF10B981).copy(alpha = pulseAlpha * 0.5f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )

                    // Secondary Emerald Ring
                    Box(
                        modifier = Modifier
                            .size(82.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981).copy(alpha = 0.15f))
                            .border(2.dp, Color(0xFF10B981).copy(alpha = 0.8f), CircleShape)
                    )

                    // Main Golden Icon Box
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        FluxGoldVibrant,
                                        FluxGoldPrimary
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = iconVector,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    // Checkmark Badge at bottom-right
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .align(Alignment.BottomEnd)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981))
                            .border(2.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Sucesso",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Deducted Points Floating Tag
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = FluxGoldVibrant.copy(alpha = 0.12f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, FluxGoldPrimary.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stars,
                            contentDescription = null,
                            tint = FluxGoldPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (pointsConsumed > 0) "-$pointsConsumed PTS Consumidos" else "Acesso Ativo",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = FluxGoldPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "MÓDULO LIBERADO! ✨",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Você desbloqueou \"$featureTitle\" com sucesso por $durationDays dias.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Progress indicator for countdown
                LinearProgressIndicator(
                    progress = { autoContinueProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = Color(0xFF10B981),
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        isTimerActive = false
                        onContinue()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF10B981)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("btn_enter_unlocked_module")
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ENTRAR NO MÓDULO AGORA",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun ConfettiParticleCanvas() {
    val particles = remember {
        val colors = listOf(
            Color(0xFFFFD700), // Gold
            Color(0xFF10B981), // Emerald
            Color(0xFF8B5CF6), // Purple
            Color(0xFF06B6D4), // Cyan
            Color(0xFFF59E0B), // Amber
            Color(0xFFEC4899)  // Pink
        )
        val list = mutableStateListOf<ConfettiParticle>()
        for (i in 0 until 45) {
            val angle = Random.nextFloat() * 2f * Math.PI.toFloat()
            val speed = Random.nextFloat() * 12f + 4f
            list.add(
                ConfettiParticle(
                    x = 0f, // Centered initially
                    y = 0f,
                    vx = kotlin.math.cos(angle) * speed,
                    vy = kotlin.math.sin(angle) * speed - 6f, // initial upward burst
                    color = colors.random(),
                    size = Random.nextFloat() * 14f + 8f,
                    rotation = Random.nextFloat() * 360f,
                    vRot = (Random.nextFloat() - 0.5f) * 15f,
                    alpha = 1f,
                    shapeType = Random.nextInt(2)
                )
            )
        }
        list
    }

    LaunchedEffect(Unit) {
        while (true) {
            withFrameNanos {
                for (p in particles) {
                    p.x += p.vx
                    p.y += p.vy
                    p.vy += 0.35f // Gravity effect
                    p.rotation += p.vRot
                    p.alpha = (p.alpha - 0.008f).coerceAtLeast(0f)
                }
            }
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val centerX = size.width / 2f
        val centerY = size.height / 2.2f

        for (p in particles) {
            if (p.alpha <= 0f) continue
            val px = centerX + p.x
            val py = centerY + p.y

            rotate(degrees = p.rotation, pivot = Offset(px, py)) {
                if (p.shapeType == 0) {
                    drawCircle(
                        color = p.color.copy(alpha = p.alpha),
                        radius = p.size / 2f,
                        center = Offset(px, py)
                    )
                } else {
                    drawRect(
                        color = p.color.copy(alpha = p.alpha),
                        topLeft = Offset(px - p.size / 2f, py - p.size / 2f),
                        size = Size(p.size, p.size * 0.6f)
                    )
                }
            }
        }
    }
}
