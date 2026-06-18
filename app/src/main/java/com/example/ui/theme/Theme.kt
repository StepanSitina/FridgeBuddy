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
import androidx.compose.ui.graphics.Color

private val DarkColorScheme =
  lightColorScheme(
    primary = FreshGreenPrimary,
    secondary = SaffronGoldSecondary,
    tertiary = TomatoRedTertiary,
    background = DarkBg,
    surface = DarkSurface,
    onBackground = CreamText,
    onSurface = CreamText,
    onPrimary = Color.White,
    onSecondary = Color.White
  )

private val LightColorScheme =
  lightColorScheme(
    primary = FreshGreenPrimary,
    secondary = SaffronGoldSecondary,
    tertiary = TomatoRedTertiary,
    background = LightBg,
    surface = LightSurface,
    onBackground = DarkText,
    onSurface = DarkText,
    onPrimary = Color.White,
    onSecondary = Color.White
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Disable dynamic color by default to preserve custom Natural Tones brand guidelines
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
