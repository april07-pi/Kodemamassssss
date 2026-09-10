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

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class KodeMamasColors(
  val isDark: Boolean,
  val background: Color,
  val surface: Color,
  val surfaceVariant: Color,
  val cardBorder: Color,
  val textPrimary: Color,
  val textSecondary: Color,
  val brandPurple: Color = Color(0xFF6F1FA8),
  val brandGold: Color = Color(0xFFFFB300),
  val bottomNavBackground: Color
)

val LocalKodeMamasColors = staticCompositionLocalOf {
  KodeMamasColors(
    isDark = true,
    background = Color(0xFF150624),
    surface = Color(0xFF220C38),
    surfaceVariant = Color(0xFF321250),
    cardBorder = Color(0xFF4B1E78),
    textPrimary = Color.White,
    textSecondary = Color(0xFFD4C7E6),
    bottomNavBackground = Color(0xFF1B072F)
  )
}

private val DarkColorScheme =
  darkColorScheme(
    primary = BrandPurplePrimary,
    secondary = BrandPurpleCard,
    tertiary = BrandGoldAccent,
    background = BrandPurpleDarkBg,
    surface = BrandPurpleCard,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White
  )

private val LightColorScheme =
  lightColorScheme(
    primary = BrandPurplePrimary,
    secondary = BrandPurpleCard,
    tertiary = BrandGoldAccent,
    background = Color(0xFFFAF6FF), // Soft lavender white
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = Color(0xFF0C0714),
    onSurface = Color(0xFF0C0714)
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is available on Android 12+
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

  val customColors = if (darkTheme) {
    KodeMamasColors(
      isDark = true,
      background = Color(0xFF150624),
      surface = Color(0xFF220C38),
      surfaceVariant = Color(0xFF321250),
      cardBorder = Color(0xFF4B1E78),
      textPrimary = Color.White,
      textSecondary = Color(0xFFD4C7E6),
      bottomNavBackground = Color(0xFF1B072F)
    )
  } else {
    KodeMamasColors(
      isDark = false,
      background = Color(0xFFF7F4FD),
      surface = Color.White,
      surfaceVariant = Color(0xFFEDE7F6),
      cardBorder = Color(0xFFE5DEFA),
      textPrimary = Color(0xFF1F122E),
      textSecondary = Color(0xFF6B5C80),
      bottomNavBackground = Color.White
    )
  }

  CompositionLocalProvider(LocalKodeMamasColors provides customColors) {
    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
  }
}
