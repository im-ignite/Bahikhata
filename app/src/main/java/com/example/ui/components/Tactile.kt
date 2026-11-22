package com.example.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.matchParentSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LedgerFigure
import com.example.ui.theme.bahi

// ─────────────────────────────────────────────────────────────
//  Lightweight "3D" without a 3D engine.
//  Every surface is two plain layers: a coloured EDGE (its
//  thickness, like the side of a ledger book) and a FACE that
//  sits on top. Pressing sinks the face onto the edge. No
//  bitmaps, no OpenGL, no extra dependencies: it costs two
//  rectangles and one soft shadow.
// ─────────────────────────────────────────────────────────────

/** Slightly uneven corners, like cards cut by hand instead of by a template. */
val HandCutShape = RoundedCornerShape(topStart = 22.dp, topEnd = 14.dp, bottomEnd = 24.dp, bottomStart = 16.dp)
val HandCutShapeAlt = RoundedCornerShape(topStart = 14.dp, topEnd = 22.dp, bottomEnd = 16.dp, bottomStart = 24.dp)

@Composable
fun TactileSurface(
    face: Brush,
    edge: Color,
    modifier: Modifier = Modifier,
    shape: Shape = HandCutShape,
    depth: Dp = 4.dp,
    onClick: (() -> Unit)? = null,
    role: Role? = if (onClick != null) Role.Button else null,
    content: @Composable BoxScope.() -> Unit,
) {
    val extras = MaterialTheme.bahi
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val sink by animateDpAsState(
        targetValue = if (pressed && onClick != null) depth * 0.75f else 0.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "tactileSink",
    )

    // The bottom padding reserves room for the edge so layouts never jump when pressed.
    Box(modifier = modifier.padding(bottom = depth)) {
        Box(
            Modifier
                .matchParentSize()
                .offset(y = depth)
                .background(edge, shape),
        )
        Box(
            modifier = Modifier
                .offset(y = sink)
                .shadow(
                    elevation = 6.dp,
                    shape = shape,
                    ambientColor = extras.shadowTint.copy(alpha = 0.16f),
                    spotColor = extras.shadowTint.copy(alpha = 0.26f),
                )
                .background(face, shape)
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        listOf(extras.paperHighlight, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
                    ),
                    shape = shape,
                )
                .then(
                    if (onClick != null) {
                        Modifier.clickable(
                            interactionSource = interaction,
                            indication = null,
                            role = role,
                            onClick = onClick,
                        )
                    } else {
                        Modifier
                    },
                ),
        ) { content() }
    }
}

/** Drop-in replacement for Card: raised, slightly lit from above, presses down when tappable. */
@Composable
fun TactileCard(
    modifier: Modifier = Modifier,
    shape: Shape = HandCutShape,
    depth: Dp = 4.dp,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    TactileSurface(
        modifier = modifier,
        shape = shape,
        depth = depth,
        onClick = onClick,
        face = Brush.verticalGradient(listOf(cs.surface, lerp(cs.surface, cs.surfaceVariant, 0.55f))),
        edge = MaterialTheme.bahi.edge,
    ) {
        Column(modifier = Modifier.padding(contentPadding), content = content)
    }
}

/** Primary action. Minimum 52dp tall so it is easy to hit with wet or fishy hands. */
@Composable
fun TactileButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    tone: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
) {
    TactileSurface(
        modifier = modifier,
        shape = RoundedCornerShape(topStart = 18.dp, topEnd = 14.dp, bottomEnd = 18.dp, bottomStart = 14.dp),
        depth = 4.dp,
        onClick = onClick,
        face = Brush.verticalGradient(listOf(lerp(tone, Color.White, 0.14f), tone)),
        edge = lerp(tone, Color.Black, 0.38f),
    ) {
        Row(
            modifier = Modifier
                .heightIn(min = 52.dp)
                .padding(horizontal = 22.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) {
                Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
            }
            Text(text, color = contentColor, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, maxLines = 1)
        }
    }
}

/**
 * The one memorable object on the dashboard: today's ledger total on a
 * fish-scale pattern. Everything else stays quiet.
 */
@Composable
fun LedgerHeroCard(
    label: String,
    amount: String,
    caption: String,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
) {
    val x = MaterialTheme.bahi
    TactileSurface(
        modifier = modifier.fillMaxWidth(),
        shape = HandCutShapeAlt,
        depth = 5.dp,
        onClick = onClick,
        face = Brush.linearGradient(listOf(x.heroTop, x.heroBottom)),
        edge = lerp(x.heroBottom, Color.Black, 0.4f),
    ) {
        Column(
            modifier = Modifier
                .scalePattern()
                .padding(horizontal = 20.dp, vertical = 18.dp),
        ) {
            Text(label, style = MaterialTheme.typography.labelLarge, color = Color.White.copy(alpha = 0.82f), maxLines = 1)
            Spacer(Modifier.size(4.dp))
            Text(
                amount,
                style = LedgerFigure,
                fontSize = 34.sp,
                lineHeight = 42.sp,
                color = Color.White,
                maxLines = 1,
            )
            Spacer(Modifier.size(2.dp))
            Text(caption, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.82f))
            if (trailing != null) {
                Spacer(Modifier.size(12.dp))
                trailing()
            }
        }
    }
}

/** Overlapping fish scales, drawn once per size from a single Path. */
fun Modifier.scalePattern(
    color: Color = Color.White.copy(alpha = 0.09f),
    scale: Dp = 22.dp,
): Modifier = drawWithCache {
    val s = scale.toPx()
    val path = Path()
    val rows = (size.height / (s * 0.5f)).toInt() + 2
    val cols = (size.width / s).toInt() + 2
    for (r in 0 until rows) {
        val y = r * s * 0.5f
        val shift = if (r % 2 == 0) 0f else s / 2f
        for (c in -1 until cols) {
            val left = c * s + shift
            path.arcTo(Rect(left, y - s / 2f, left + s, y + s / 2f), 0f, 180f, true)
        }
    }
    val stroke = Stroke(width = 1.2.dp.toPx())
    onDrawBehind { drawPath(path, color, style = stroke) }
}

/** A slightly wobbly underline, as if drawn with a pen. Use once per screen title, not everywhere. */
fun Modifier.handUnderline(
    color: Color,
    thickness: Dp = 2.5.dp,
    fraction: Float = 0.62f,
): Modifier = this
    .drawBehind {
        val t = thickness.toPx()
        val y = size.height - t
        val w = size.width * fraction
        val p = Path().apply {
            moveTo(0f, y)
            cubicTo(w * 0.25f, y - t * 1.6f, w * 0.58f, y + t * 1.4f, w, y - t * 0.5f)
        }
        drawPath(p, color, style = Stroke(width = t, cap = StrokeCap.Round))
    }
    .padding(bottom = 7.dp)

/** Faint ruled lines and a red margin: the page behind everything is a ledger leaf. */
@Composable
fun Modifier.ledgerPaper(): Modifier {
    val x = MaterialTheme.bahi
    return drawBehind {
        val gap = 32.dp.toPx()
        var y = gap
        while (y < size.height) {
            drawLine(x.ruleLine, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
            y += gap
        }
        val mx = 10.dp.toPx()
        drawLine(x.marginLine, Offset(mx, 0f), Offset(mx, size.height), strokeWidth = 1.5.dp.toPx())
    }
}
