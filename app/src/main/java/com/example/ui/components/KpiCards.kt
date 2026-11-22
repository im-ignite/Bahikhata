package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LedgerFigure
import com.example.ui.theme.bahi
import java.text.NumberFormat
import java.util.Locale

/** 1,23,456.00 style grouping, the way shopkeepers actually write amounts. */
fun bahiRupees(amount: Double, decimals: Int = 2): String {
    val nf = NumberFormat.getNumberInstance(Locale("en", "IN"))
    nf.minimumFractionDigits = decimals
    nf.maximumFractionDigits = decimals
    return "₹" + nf.format(amount)
}

@Composable
fun MetricStatCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    shape: Shape = HandCutShape,
) {
    TactileCard(modifier = modifier, shape = shape, onClick = onClick, contentPadding = androidx.compose.foundation.layout.PaddingValues(14.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            EmbossedIcon(icon, iconTint, iconBg)
        }
        Spacer(Modifier.height(10.dp))
        Text(
            text = value,
            style = LedgerFigure,
            // long rupee amounts step down instead of wrapping on narrow phones
            fontSize = if (value.length > 11) 18.sp else 22.sp,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** A small pressed-in tile for the icon: gradient fill plus a lit top edge. */
@Composable
private fun EmbossedIcon(icon: ImageVector, tint: Color, bg: Color) {
    val shape = RoundedCornerShape(12.dp)
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(shape)
            .background(Brush.verticalGradient(listOf(bg.copy(alpha = (bg.alpha + 0.10f).coerceAtMost(1f)), bg)), shape)
            .border(1.dp, Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.5f), tint.copy(alpha = 0.18f))), shape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
    }
}

private data class Kpi(
    val title: String,
    val value: String,
    val subtitle: String,
    val icon: ImageVector,
    val tint: Color,
    val onClick: () -> Unit,
)

/**
 * Same signature as before. 2×2 on phones, one row of four from 560dp up
 * (tablets, foldables, landscape).
 */
@Composable
fun SummaryMetricsGrid(
    totalWeightKg: Double,
    totalPieces: Int,
    totalSalesAmount: Double,
    transactionCount: Int,
    onWeightClick: () -> Unit = {},
    onPiecesClick: () -> Unit = {},
    onSalesClick: () -> Unit = {},
    onOrdersClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val cs = MaterialTheme.colorScheme
    val x = MaterialTheme.bahi
    val items = listOf(
        Kpi("Total Fish Weight", "${String.format("%.1f", totalWeightKg)} kg", "Sold to clients", Icons.Default.FitnessCenter, cs.primary, onWeightClick),
        Kpi("Fish Pieces", "$totalPieces pcs", "Harvested & sold", Icons.Default.Inventory2, cs.secondary, onPiecesClick),
        Kpi("Total Sales (₹)", bahiRupees(totalSalesAmount), "Total revenue", Icons.Default.AttachMoney, cs.tertiary, onSalesClick),
        Kpi("Client Sales", "$transactionCount orders", "Client sales logs", Icons.Default.Receipt, x.success, onOrdersClick),
    )
    val shapes = listOf(HandCutShape, HandCutShapeAlt, HandCutShapeAlt, HandCutShape)

    BoxWithConstraints(modifier = modifier) {
        val wide = maxWidth >= 560.dp
        if (wide) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items.forEachIndexed { i, k -> KpiCell(k, shapes[i], Modifier.weight(1f)) }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items.chunked(2).forEachIndexed { row, pair ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        pair.forEachIndexed { col, k -> KpiCell(k, shapes[row * 2 + col], Modifier.weight(1f)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun KpiCell(k: Kpi, shape: Shape, modifier: Modifier) {
    MetricStatCard(
        title = k.title,
        value = k.value,
        subtitle = k.subtitle,
        icon = k.icon,
        iconTint = k.tint,
        iconBg = k.tint.copy(alpha = 0.12f),
        modifier = modifier,
        onClick = k.onClick,
        shape = shape,
    )
}
