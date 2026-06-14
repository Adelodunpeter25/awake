package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AwakeSettings
import com.example.ui.theme.DarkCharcoal
import com.example.ui.theme.MediumGray
import com.example.ui.theme.PureWhite
import com.example.ui.theme.StarkBlack

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigurationScreen(
    settings: AwakeSettings,
    onBack: () -> Unit,
    onSaveSettings: (duration: Int, batteryEnabled: Boolean, batteryPct: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    // Intercept back button triggers in Android 12 to safely return to the home dashboard
    BackHandler {
        onBack()
    }

    // Map database durationMinutes (which uses -1 for infinity) to a slider value in 1..301 range (301 means Infinity)
    var sliderValue by remember(settings.durationMinutes) {
        mutableStateOf(
            if (settings.durationMinutes == -1) 301f else settings.durationMinutes.toFloat().coerceIn(1f, 300f)
        )
    }

    var batteryGuardEnabled by remember(settings.batteryThresholdEnabled) {
        mutableStateOf(settings.batteryThresholdEnabled)
    }
    var batteryThresholdPct by remember(settings.batteryThreshold) {
        mutableStateOf(settings.batteryThreshold)
    }

    val scrollState = rememberScrollState()

    // Shortcut Presets
    val presets = listOf(
        1 to "1m",
        5 to "5m",
        15 to "15m",
        30 to "30m",
        60 to "1h",
        120 to "2h",
        300 to "5h",
        -1 to "∞"
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "CONFIGURATION",
                        letterSpacing = 2.sp,
                        color = PureWhite,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Return to Dashboard",
                            tint = PureWhite
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = StarkBlack
                )
            )
        },
        containerColor = StarkBlack
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            
            // Header Info text
            Text(
                text = "Customize the duration to keep your screen on. The system active locks will update instantly upon saving.",
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                lineHeight = 16.sp,
                color = MediumGray
            )

            // Section 1: Duration Slider
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "AWAKE DURATION",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = PureWhite,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = if (sliderValue.toInt() == 301) "Infinite (∞)" else formatLabelMinutes(sliderValue.toInt()),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp,
                        color = PureWhite,
                        fontWeight = FontWeight.Black
                    )
                }

                // Continuous custom slider including 301 as infinity threshold
                Slider(
                    value = sliderValue,
                    onValueChange = { sliderValue = it },
                    valueRange = 1f..301f,
                    colors = SliderDefaults.colors(
                        thumbColor = PureWhite,
                        activeTrackColor = PureWhite,
                        inactiveTrackColor = DarkCharcoal,
                        activeTickColor = PureWhite,
                        inactiveTickColor = MediumGray
                    )
                )

                // Quick Presets Layout
                Text(
                    text = "Quick Presets",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = MediumGray
                )

                CompactFlowLayout {
                    presets.forEach { (mins, label) ->
                        val currentSliderEquivalent = if (mins == -1) 301f else mins.toFloat()
                        val isSelected = sliderValue.toInt() == currentSliderEquivalent.toInt()
                        Button(
                            onClick = { sliderValue = currentSliderEquivalent },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) PureWhite else StarkBlack,
                                contentColor = if (isSelected) StarkBlack else PureWhite
                            ),
                            border = BorderStroke(1.dp, if (isSelected) PureWhite else DarkCharcoal),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                            modifier = Modifier.padding(end = 6.dp, bottom = 6.dp)
                        ) {
                            Text(
                                text = label,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Divider(color = DarkCharcoal, thickness = 1.dp)

            // Section 2: Battery Guard Protection
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "BATTERY PROTECTION",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = PureWhite,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Shut down keeping awake service automatically when battery level is critical.",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = MediumGray
                        )
                    }
                    Switch(
                        checked = batteryGuardEnabled,
                        onCheckedChange = { batteryGuardEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = StarkBlack,
                            checkedTrackColor = PureWhite,
                            uncheckedThumbColor = MediumGray,
                            uncheckedTrackColor = StarkBlack,
                            uncheckedBorderColor = MediumGray
                        )
                    )
                }

                if (batteryGuardEnabled) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Battery Threshold",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = MediumGray
                            )
                            Text(
                                text = "$batteryThresholdPct%",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                color = PureWhite,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Slider(
                            value = batteryThresholdPct.toFloat(),
                            onValueChange = { batteryThresholdPct = it.toInt() },
                            valueRange = 5f..50f,
                            colors = SliderDefaults.colors(
                                thumbColor = PureWhite,
                                activeTrackColor = PureWhite,
                                inactiveTrackColor = DarkCharcoal
                            )
                        )
                    }
                }
            }

            // Margin space before primary triggers
            Spacer(modifier = Modifier.weight(1f))

            // Section 3: Save button
            Button(
                onClick = {
                    val actualMins = if (sliderValue.toInt() == 301) -1 else sliderValue.toInt()
                    onSaveSettings(actualMins, batteryGuardEnabled, batteryThresholdPct)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PureWhite,
                    contentColor = StarkBlack
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "SAVE & APPLY CONFIGURATION",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

private fun formatLabelMinutes(minutes: Int): String {
    if (minutes < 60) {
        return "$minutes min"
    }
    val h = minutes / 60
    val m = minutes % 60
    return if (m > 0) "${h}h ${m}m" else "${h}h"
}

@Composable
fun CompactFlowLayout(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    androidx.compose.ui.layout.Layout(
        content = content,
        modifier = modifier
    ) { measurables, constraints ->
        val placeables = measurables.map { measurable ->
            measurable.measure(constraints.copy(minWidth = 0, minHeight = 0))
        }

        val rows = mutableListOf<MutableList<androidx.compose.ui.layout.Placeable>>()
        var currentRow = mutableListOf<androidx.compose.ui.layout.Placeable>()
        var rowWidth = 0
        val maxRowWidth = constraints.maxWidth

        placeables.forEach { placeable ->
            val spacing = if (currentRow.isEmpty()) 0 else 8.dp.roundToPx()
            if (rowWidth + placeable.width + spacing > maxRowWidth && currentRow.isNotEmpty()) {
                rows.add(currentRow)
                currentRow = mutableListOf()
                rowWidth = 0
            }
            if (currentRow.isNotEmpty()) {
                rowWidth += spacing
            }
            currentRow.add(placeable)
            rowWidth += placeable.width
        }
        if (currentRow.isNotEmpty()) {
            rows.add(currentRow)
        }

        var totalHeight = 0
        rows.forEachIndexed { index, row ->
            val rowHeight = row.maxOf { it.height }
            totalHeight += rowHeight
            if (index < rows.size - 1) {
                totalHeight += 8.dp.roundToPx()
            }
        }

        layout(constraints.maxWidth, totalHeight.coerceIn(constraints.minHeight, constraints.maxHeight)) {
            var currentY = 0
            rows.forEach { row ->
                val rowHeight = row.maxOf { it.height }
                var currentX = 0
                row.forEach { placeable ->
                    placeable.placeRelative(currentX, currentY + (rowHeight - placeable.height) / 2)
                    currentX += placeable.width + 8.dp.roundToPx()
                }
                currentY += rowHeight + 8.dp.roundToPx()
            }
        }
    }
}
