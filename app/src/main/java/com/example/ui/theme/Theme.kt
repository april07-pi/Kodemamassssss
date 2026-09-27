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
  val brandPurple: Color = KodeMamasPurple,
  val brandGold: Color = KodeMamasGold,
  val brandMagenta: Color = KodeMamasMagenta,
  val brandPink: Color = KodeMamasPink,
  val brandCoral: Color = KodeMamasCoral,
  val bottomNavBackground: Color
)

val LocalKodeMamasColors = staticCompositionLocalOf {
  KodeMamasColors(
    isDark = true,
    background = KodeMamasDeepBg,
    surface = KodeMamasSurface,
    surfaceVariant = KodeMamasSurfaceCard,
    cardBorder = KodeMamasBorder,
    textPrimary = Color.White,
    textSecondary = KodeMamasLavender,
    bottomNavBackground = Color(0xFF100322)
  )
}

private val DarkColorScheme =
  darkColorScheme(
    primary = KodeMamasPurple,
    secondary = KodeMamasSurface,
    tertiary = KodeMamasGold,
    background = KodeMamasDeepBg,
    surface = KodeMamasSurface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White
  )

private val LightColorScheme =
  lightColorScheme(
    primary = KodeMamasPurple,
    secondary = Color(0xFFF3F4F6),
    tertiary = KodeMamasGold,
    background = Color(0xFFF9FAFB), // Clean modern white-gray
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color(0xFF111827),
    onTertiary = Color.Black,
    onBackground = Color(0xFF111827),
    onSurface = Color(0xFF111827)
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
      background = KodeMamasDeepBg,
      surface = KodeMamasSurface,
      surfaceVariant = KodeMamasSurfaceCard,
      cardBorder = KodeMamasBorder,
      textPrimary = Color.White,
      textSecondary = KodeMamasLavender,
      brandPurple = KodeMamasPurple,
      brandGold = KodeMamasGold,
      brandMagenta = KodeMamasMagenta,
      brandPink = KodeMamasPink,
      brandCoral = KodeMamasCoral,
      bottomNavBackground = Color(0xFF100322)
    )
  } else {
    KodeMamasColors(
      isDark = false,
      background = Color(0xFFF9FAFB),
      surface = Color.White,
      surfaceVariant = Color(0xFFF3F4F6),
      cardBorder = Color(0xFFE5E7EB),
      textPrimary = Color(0xFF111827),
      textSecondary = Color(0xFF4B5563),
      brandPurple = KodeMamasPurple,
      brandGold = KodeMamasGold,
      brandMagenta = KodeMamasMagenta,
      brandPink = KodeMamasPink,
      brandCoral = KodeMamasCoral,
      bottomNavBackground = Color.White
    )
  }

  CompositionLocalProvider(LocalKodeMamasColors provides customColors) {
    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
  }
}
