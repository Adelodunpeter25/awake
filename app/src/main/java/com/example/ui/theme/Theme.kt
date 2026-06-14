package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = PureWhite,
    onPrimary = StarkBlack,
    secondary = LightGray,
    onSecondary = StarkBlack,
    tertiary = MediumGray,
    background = StarkBlack,
    onBackground = PureWhite,
    surface = DarkCharcoal,
    onSurface = PureWhite,
    surfaceVariant = DarkCharcoal,
    onSurfaceVariant = LightGray,
    outline = MediumGray
)

private val LightColorScheme = DarkColorScheme // Forced Dark theme

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Force dark mode as requested by user
  dynamicColor: Boolean = false, // Force custom black & white design brand
  content: @Composable () -> Unit,
) {
  val colorScheme = DarkColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
