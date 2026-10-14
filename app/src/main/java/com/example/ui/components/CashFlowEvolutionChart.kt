package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.StackedBarChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppCurrency
import com.example.domain.calculator.CostEngineeringCalculator
import com.example.ui.theme.FluxCrimsonError
import com.example.ui.theme.FluxEmeraldSuccess
import com.example.ui.theme.FluxNavyBorder
import com.example.ui.theme.FluxTurquoise
import kotlin.math.abs
import kotlin.math.max

enum class ChartDisplayMode(val label: String, val icon: ImageVector) {
    BARS("Barras", Icons.Default.BarChart),
    AREA("Fluxo", Icons.AutoMirrored.Filled.ShowChart),
    NET("Líquido", Icons.Default.StackedBarChart)
}

enum class ChartTimeRange(val label: String, val monthsCount: Int) {
    LAST_3M("3M", 3),
    LAST_6M("6M", 6),
    LAST_12M("12M", 12)
}

/**
 * Gráfico financeiro limpo, preciso e interativo do FluxAO.
 *
 * Características:
 * - Seleção de período (3M, 6M, 12M).
 * - Modos de visualização analítica (Barras Receita/Despesa, Curva de Fluxo, Resultado Líquido).
 * - Eixos claros com linha base zero e linhas de grelha discretas.
 * - Tooltip interativo ao tocar ou arrastar.
 */
@Composable
fun CashFlowEvolutionChart(
    data: List<MonthlyBarData>,
    currency: AppCurrency,
    modifier: Modifier = Modifier,
    title: String = "EVOLUÇÃO DO FLUXO",
    subtitle: String? = null
) {
    var displayMode by remember { mutableStateOf(ChartDisplayMode.BARS) }
    var selectedRange by remember { mutableStateOf(ChartTimeRange.LAST_6M) }
    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    val filteredData = remember(data, selectedRange) {
        if (data.isEmpty()) emptyList()
        else data.takeLast(selectedRange.monthsCount)
    }

    val maxVal = remember(filteredData) {
        val peak = filteredData.maxOfOrNull { max(it.income, it.expense) } ?: 0.0
        if (peak <= 0.0) 100_000.0 else peak * 1.15
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("chart_cash_flow_evolution"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header do Gráfico: Título e Controles
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = title.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 0.6.sp
                    )
                    Text(
                        text = subtitle ?: when (displayMode) {
                            ChartDisplayMode.BARS -> "Receitas vs Despesas"
                            ChartDisplayMode.AREA -> "Tendência de Caixa"
                            ChartDisplayMode.NET -> "Superávit / Déficit"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Seletor de Período 3M / 6M / 12M
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(modifier = Modifier.padding(2.dp)) {
                        ChartTimeRange.values().forEach { range ->
                            val isSelected = selectedRange == range
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { selectedRange = range }
                            ) {
                                Text(
                                    text = range.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Seletor de Tipo de Gráfico + Legendas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Modos: Barras, Fluxo, Líquido
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    ChartDisplayMode.values().forEach { mode ->
                        val isSelected = displayMode == mode
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent,
                            border = if (isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)) else null,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { displayMode = mode }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = mode.icon,
                                    contentDescription = mode.label,
                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = mode.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Legenda de Cores
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(FluxEmeraldSuccess))
                        Text("Receitas", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(FluxCrimsonError))
                        Text("Despesas", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Canvas Principal do Gráfico
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                if (filteredData.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Sem movimentações no período selecionado.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    val outlineVariantColor = MaterialTheme.colorScheme.outlineVariant
                    val incomeColor = FluxEmeraldSuccess
                    val expenseColor = FluxCrimsonError
                    val netColor = FluxTurquoise

                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(filteredData) {
                                detectTapGestures { offset ->
                                    val count = filteredData.size
                                    if (count > 0) {
                                        val slotWidth = size.width / count
                                        val idx = (offset.x / slotWidth).toInt().coerceIn(0, count - 1)
                                        selectedIndex = if (selectedIndex == idx) null else idx
                                    }
                                }
                            }
                            .pointerInput(filteredData) {
                                detectDragGestures { change, _ ->
                                    val count = filteredData.size
                                    if (count > 0) {
                                        val slotWidth = size.width / count
                                        val idx = (change.position.x / slotWidth).toInt().coerceIn(0, count - 1)
                                        selectedIndex = idx
                                    }
                                }
                            }
                    ) {
                        val canvasWidth = size.width
                        val canvasHeight = size.height - 24.dp.toPx()
                        val count = filteredData.size
                        val slotWidth = canvasWidth / count

                        // Linhas de grelha de precisão
                        val lines = 3
                        for (i in 0..lines) {
                            val y = canvasHeight * (i.toFloat() / lines)
                            drawLine(
                                color = gridColor,
                                start = Offset(0f, y),
                                end = Offset(canvasWidth, y),
                                strokeWidth = 1f,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                            )
                        }

                        when (displayMode) {
                            ChartDisplayMode.BARS -> {
                                val barWidth = (slotWidth * 0.32f).coerceAtMost(16.dp.toPx())
                                filteredData.forEachIndexed { index, item ->
                                    val centerX = slotWidth * index + slotWidth / 2f
                                    val incomeH = ((item.income / maxVal) * canvasHeight).toFloat().coerceAtLeast(2f)
                                    val expenseH = ((item.expense / maxVal) * canvasHeight).toFloat().coerceAtLeast(2f)

                                    // Barra Receita
                                    drawRoundRect(
                                        color = incomeColor,
                                        topLeft = Offset(centerX - barWidth - 2.dp.toPx(), canvasHeight - incomeH),
                                        size = Size(barWidth, incomeH),
                                        cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
                                    )

                                    // Barra Despesa
                                    drawRoundRect(
                                        color = expenseColor,
                                        topLeft = Offset(centerX + 2.dp.toPx(), canvasHeight - expenseH),
                                        size = Size(barWidth, expenseH),
                                        cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
                                    )
                                }
                            }
                            ChartDisplayMode.AREA -> {
                                val incomePath = Path()
                                val incomeArea = Path()
                                filteredData.forEachIndexed { index, item ->
                                    val x = slotWidth * index + slotWidth / 2f
                                    val y = canvasHeight - ((item.income / maxVal) * canvasHeight).toFloat()
                                    if (index == 0) {
                                        incomePath.moveTo(x, y)
                                        incomeArea.moveTo(x, canvasHeight)
                                        incomeArea.lineTo(x, y)
                                    } else {
                                        incomePath.lineTo(x, y)
                                        incomeArea.lineTo(x, y)
                                    }
                                }
                                val lastX = slotWidth * (count - 1) + slotWidth / 2f
                                incomeArea.lineTo(lastX, canvasHeight)
                                incomeArea.close()

                                drawPath(
                                    path = incomeArea,
                                    brush = Brush.verticalGradient(
                                        listOf(incomeColor.copy(alpha = 0.25f), Color.Transparent),
                                        startY = 0f,
                                        endY = canvasHeight
                                    )
                                )
                                drawPath(
                                    path = incomePath,
                                    color = incomeColor,
                                    style = Stroke(width = 2.5f, cap = StrokeCap.Round)
                                )
                            }
                            ChartDisplayMode.NET -> {
                                val halfHeight = canvasHeight / 2f
                                drawLine(
                                    color = outlineVariantColor,
                                    start = Offset(0f, halfHeight),
                                    end = Offset(canvasWidth, halfHeight),
                                    strokeWidth = 1.5f
                                )
                                val barWidth = (slotWidth * 0.45f).coerceAtMost(22.dp.toPx())
                                filteredData.forEachIndexed { index, item ->
                                    val centerX = slotWidth * index + slotWidth / 2f
                                    val net = item.net
                                    val isPositive = net >= 0
                                    val barH = ((abs(net) / maxVal) * halfHeight).toFloat().coerceAtLeast(3f)
                                    val topY = if (isPositive) halfHeight - barH else halfHeight
                                    drawRoundRect(
                                        color = if (isPositive) netColor else expenseColor,
                                        topLeft = Offset(centerX - barWidth / 2f, topY),
                                        size = Size(barWidth, barH),
                                        cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
                                    )
                                }
                            }
                        }

                        // Indicador de Seleção
                        selectedIndex?.let { idx ->
                            if (idx in 0 until count) {
                                val selX = slotWidth * idx + slotWidth / 2f
                                drawLine(
                                    color = netColor,
                                    start = Offset(selX, 0f),
                                    end = Offset(selX, canvasHeight),
                                    strokeWidth = 1.5f,
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
                                )
                                drawCircle(
                                    color = netColor,
                                    radius = 4.dp.toPx(),
                                    center = Offset(selX, canvasHeight - ((filteredData[idx].income / maxVal) * canvasHeight).toFloat().coerceIn(0f, canvasHeight))
                                )
                            }
                        }
                    }

                    // Rótulos do Eixo X (Meses)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .height(20.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        filteredData.forEachIndexed { index, item ->
                            val isSelected = selectedIndex == index
                            Text(
                                text = item.monthLabel,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Tooltip Detalhado de Seleção
            selectedIndex?.let { idx ->
                if (idx in 0 until filteredData.size) {
                    val selItem = filteredData[idx]
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${selItem.fullMonthName} ${selItem.year}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(
                                    text = "Ent: +${CostEngineeringCalculator.formatCurrency(selItem.income)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = FluxEmeraldSuccess,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Saí: -${CostEngineeringCalculator.formatCurrency(selItem.expense)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = FluxCrimsonError,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Líq: ${CostEngineeringCalculator.formatCurrency(selItem.net)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (selItem.net >= 0) FluxTurquoise else FluxCrimsonError,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
