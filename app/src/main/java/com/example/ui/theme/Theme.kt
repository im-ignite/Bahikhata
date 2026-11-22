package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

/** Tokens Material 3 has no slot for: the "thickness" of tactile surfaces, ledger paper lines, hero gradient. */
@Immutable
data class BahiExtras(
    val isDark: Boolean,
    val paperHighlight: Color,
    val edge: Color,
    val shadowTint: Color,
    val ruleLine: Color,
    val marginLine: Color,
    val heroTop: Color,
    val heroBottom: Color,
    val success: Color,
    val danger: Color,
)

private val LightExtras = BahiExtras(
    isDark = false,
    paperHighlight = Color.White,
    edge = Color(0xFFB9CDCA),
    shadowTint = InkDark,
    ruleLine = InkDark.copy(alpha = 0.045f),
    marginLine = DangerRed.copy(alpha = 0.22f),
    heroTop = Color(0xFF0E7C7B),
    heroBottom = Color(0xFF0A4F55),
    success = SuccessGreen,
    danger = DangerRed,
)

private val DarkExtras = BahiExtras(
    isDark = true,
    paperHighlight = Color.White.copy(alpha = 0.10f),
    edge = Color(0xFF050E11),
    shadowTint = Color.Black,
    ruleLine = Color.White.copy(alpha = 0.035f),
    marginLine = Color(0xFFFF8B80).copy(alpha = 0.28f),
    heroTop = Color(0xFF0F6F6E),
    heroBottom = Color(0xFF08373D),
    success = Color(0xFF52D394),
    danger = Color(0xFFFF8B80),
)

val LocalBahiExtras = staticCompositionLocalOf { LightExtras }

val MaterialTheme.bahi: BahiExtras
    @Composable @ReadOnlyComposable get() = LocalBahiExtras.current

private val BahiShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF4CC3BF),
    onPrimary = Color(0xFF00302F),
    primaryContainer = TealContainerDark,
    onPrimaryContainer = TealContainerLight,
    secondary = CyanSecondaryDark,
    onSecondary = InkDark,
    secondaryContainer = CyanContainerDark,
    onSecondaryContainer = CyanContainerLight,
    tertiary = Color(0xFFE2B455),
    onTertiary = Color(0xFF3A2A00),
    tertiaryContainer = AmberAccentDark,
    onTertiaryContainer = AmberAccentLight,
    background = DarkBg,
    onBackground = Color(0xFFE6EFEE),
    surface = DarkSurface,
    onSurface = Color(0xFFE6EFEE),
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFB4C9CC),
    outline = DarkCardBorder,
    error = Color(0xFFFF8B80),
    onError = Color(0xFF4A0A06),
)

private val LightColorScheme = lightColorScheme(
    primary = TealPrimary,
    onPrimary = Color.White,
    primaryContainer = TealContainerLight,
    onPrimaryContainer = Color(0xFF05302F),
    secondary = CyanSecondary,
    onSecondary = Color.White,
    secondaryContainer = CyanContainerLight,
    onSecondaryContainer = InkDark,
    tertiary = AmberAccentDark,
    onTertiary = Color.White,
    tertiaryContainer = AmberAccentLight,
    onTertiaryContainer = Color(0xFF3A2A00),
    background = LightBg,
    onBackground = InkDark,
    surface = LightSurface,
    onSurface = InkDark,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = Color(0xFF45616A),
    outline = LightCardBorder,
    error = DangerRed,
    onError = Color.White,
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // keep the branded ledger look; flip to true to follow wallpaper colours
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    CompositionLocalProvider(LocalBahiExtras provides if (darkTheme) DarkExtras else LightExtras) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = BahiShapes,
            content = content,
        )
    }
}
