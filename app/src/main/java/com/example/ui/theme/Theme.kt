package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val NamazDarkScheme = darkColorScheme(
    primary = EmeraldPrimary,
    onPrimary = Color(0xFF032110),
    secondary = GoldAccent,
    onSecondary = Color(0xFF2B2000),
    background = BgDeepForest,
    onBackground = TextPrimary,
    surface = CardDarkEmerald,
    onSurface = TextPrimary,
    surfaceVariant = CardElevated,
    onSurfaceVariant = TextSecondary,
    outline = BorderEmeraldGlow,
    error = DangerRed
)

@Composable
fun NamazTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = NamazDarkScheme,
        typography = Typography,
        content = content
    )
}
