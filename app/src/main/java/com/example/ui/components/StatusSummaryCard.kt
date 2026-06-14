package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AwakeSettings
import com.example.ui.theme.DarkCharcoal
import com.example.ui.theme.MediumGray
import com.example.ui.theme.PureWhite

@Composable
fun StatusSummaryCard(
    settings: AwakeSettings,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkCharcoal)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Target Duration",
                fontFamily = FontFamily.Monospace,
                color = MediumGray,
                fontSize = 12.sp
            )
            Text(
                text = if (settings.durationMinutes == -1) "Infinite (∞)" else formatMinutes(settings.durationMinutes),
                fontFamily = FontFamily.Monospace,
                color = PureWhite,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Battery Guard",
                fontFamily = FontFamily.Monospace,
                color = MediumGray,
                fontSize = 12.sp
            )
            Text(
                text = if (settings.batteryThresholdEnabled) {
                    "Shuts down below ${settings.batteryThreshold}%"
                } else {
                    "Disabled"
                },
                fontFamily = FontFamily.Monospace,
                color = if (settings.batteryThresholdEnabled) PureWhite else MediumGray,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Hint info",
                tint = MediumGray,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Adds Quick Settings tile to notification panel",
                fontFamily = FontFamily.Monospace,
                color = MediumGray,
                fontSize = 10.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun formatMinutes(minutes: Int): String {
    if (minutes < 60) {
        return "$minutes Minutes"
    }
    val hours = minutes / 60
    val remMins = minutes % 60
    return if (remMins > 0) {
        "${hours}h ${remMins}m"
    } else {
        "${hours}h"
    }
}
