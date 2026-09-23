package com.aistudio.tradesync.wxqpzr.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AiVoiceTriggerButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val shadowOffset = 3.dp
    // On press: offset animates to 0.dp so it aligns with the shadow, giving a tactile push-down feel
    val translation by animateDpAsState(
        targetValue = if (isPressed) shadowOffset else 0.dp,
        label = "press_translation"
    )

    Row(
        modifier = modifier
            // We need padding to give room for the shadow/translation so it doesn't overlap sibling views
            .padding(end = shadowOffset, bottom = shadowOffset)
            .height(28.dp)
            .offset(x = translation, y = translation)
            .drawBehind {
                val cornerRadius = CornerRadius(size.height / 2, size.height / 2)
                drawRoundRect(
                    color = Color(0xFF292524),
                    topLeft = Offset(
                        (shadowOffset - translation).toPx(), 
                        (shadowOffset - translation).toPx()
                    ),
                    size = size,
                    cornerRadius = cornerRadius
                )
            }
            .background(Color(0xFFFACC15), CircleShape)
            .border(1.5.dp, Color(0xFF292524), CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = "AI Voice Assistant",
            modifier = Modifier.size(14.dp),
            tint = Color(0xFF292524)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "Bolein",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF292524)
        )
    }
}
