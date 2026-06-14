package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkCharcoal
import com.example.ui.theme.MediumGray
import com.example.ui.theme.PureWhite
import com.example.ui.theme.StarkBlack

@Composable
fun AwakeToggleButton(
    isRunning: Boolean,
    remainingSeconds: Int?,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Pulsing circle scale for active states
    val infiniteTransition = rememberInfiniteTransition(label = "pulsing")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(280.dp)
            .scale(if (isRunning) pulseScale else 1f)
    ) {
        // outer ring
        Box(
            modifier = Modifier
                .size(255.dp)
                .clip(CircleShape)
                .background(if (isRunning) DarkCharcoal else Color.Transparent)
                .clickable { onToggle() },
            contentAlignment = Alignment.Center
        ) {
            // inner pulsing circle/button
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .clip(CircleShape)
                    .background(if (isRunning) PureWhite else StarkBlack),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Display Timer or Infinite Symbol
                    Text(
                        text = if (isRunning) {
                            formatSeconds(remainingSeconds)
                        } else {
                            "OFF"
                        },
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isRunning) StarkBlack else PureWhite,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = (-1).sp
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = if (isRunning) "TAP TO STOP" else "TAP TO START",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp,
                        color = if (isRunning) StarkBlack.copy(alpha = 0.65f) else MediumGray,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

private fun formatSeconds(seconds: Int?): String {
    if (seconds == null) return "∞"
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return if (h > 0) {
        String.format("%02d:%02d:%02d", h, m, s)
    } else {
        String.format("%02d:%02d", m, s)
    }
}
