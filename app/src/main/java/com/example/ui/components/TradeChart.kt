package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.CyanSecondary
import com.example.ui.theme.TealPrimary
import com.example.ui.viewmodel.DailyChartPoint

@Composable
fun WeightAndSalesTrendChart(
    points: List<DailyChartPoint>,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 800),
        label = "chartAnim"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Weight & Activity Trends",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Aggregated metric distribution over selected dates",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(TealPrimary, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Weight (kg)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(AmberAccent, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Sales (₹)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (points.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No batch or sales records in this date range",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                val maxWeight = points.maxOfOrNull { it.weightKg }?.coerceAtLeast(10.0) ?: 10.0
                val maxSales = points.maxOfOrNull { it.salesAmount }?.coerceAtLeast(100.0) ?: 100.0

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                ) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height
                    val paddingBottom = 40f
                    val chartHeight = canvasHeight - paddingBottom

                    // Draw subtle horizontal grid lines
                    val gridLines = 4
                    for (i in 0..gridLines) {
                        val y = chartHeight * (i.toFloat() / gridLines)
                        drawLine(
                            color = Color.LightGray.copy(alpha = 0.3f),
                            start = Offset(0f, y),
                            end = Offset(canvasWidth, y),
                            strokeWidth = 1.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                        )
                    }

                    val stepX = canvasWidth / (points.size.coerceAtLeast(1) + 1)

                    // Draw Bars for Weight
                    val barWidth = 24.dp.toPx()
                    points.forEachIndexed { index, point ->
                        val centerX = stepX * (index + 1)
                        val barHeight = ((point.weightKg / maxWeight) * chartHeight * 0.85f * animatedProgress).toFloat()
                        val topY = chartHeight - barHeight

                        // Teal gradient bar
                        drawRoundRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(TealPrimary, CyanSecondary.copy(alpha = 0.8f))
                            ),
                            topLeft = Offset(centerX - barWidth / 2, topY),
                            size = Size(barWidth, barHeight),
                            cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                        )
                    }

                    // Draw Line for Sales
                    if (points.size > 1) {
                        val linePath = Path()
                        points.forEachIndexed { index, point ->
                            val x = stepX * (index + 1)
                            val y = chartHeight - ((point.salesAmount / maxSales) * chartHeight * 0.75f * animatedProgress).toFloat()
                            if (index == 0) {
                                linePath.moveTo(x, y)
                            } else {
                                linePath.lineTo(x, y)
                            }
                        }

                        drawPath(
                            path = linePath,
                            color = AmberAccent,
                            style = Stroke(width = 3.dp.toPx())
                        )

                        // Draw points on sales line
                        points.forEachIndexed { index, point ->
                            val x = stepX * (index + 1)
                            val y = chartHeight - ((point.salesAmount / maxSales) * chartHeight * 0.75f * animatedProgress).toFloat()
                            drawCircle(
                                color = Color.White,
                                radius = 5.dp.toPx(),
                                center = Offset(x, y)
                            )
                            drawCircle(
                                color = AmberAccent,
                                radius = 3.5.dp.toPx(),
                                center = Offset(x, y)
                            )
                        }
                    }
                }

                // Date X-axis labels
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    val displayCount = points.size.coerceAtMost(6)
                    val step = (points.size / displayCount).coerceAtLeast(1)
                    points.filterIndexed { index, _ -> index % step == 0 }.take(6).forEach { point ->
                        val shortLabel = if (point.dateLabel.length >= 10) point.dateLabel.substring(5) else point.dateLabel
                        Text(
                            text = shortLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
