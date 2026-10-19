package com.bticket.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** Identité bTicket : noir, anthracite, or et ivoire (mêmes couleurs que le site). */
private val DarkColors = darkColorScheme(
    primary = Color(0xFFD4AF37),
    onPrimary = Color(0xFF111318),
    primaryContainer = Color(0xFFE3C257),
    onPrimaryContainer = Color(0xFF111318),
    secondary = Color(0xFF2F3540),
    onSecondary = Color(0xFFF4F0E6),
    secondaryContainer = Color(0xFF2F3540),
    onSecondaryContainer = Color(0xFFF4F0E6),
    tertiary = Color(0xFFE3C257),
    onTertiary = Color(0xFF111318),
    background = Color(0xFF111318),
    onBackground = Color(0xFFF4F0E6),
    surface = Color(0xFF111318),
    onSurface = Color(0xFFF4F0E6),
    surfaceVariant = Color(0xFF363D4A),
    onSurfaceVariant = Color(0xFFA9A69E),
    surfaceContainerLowest = Color(0xFF1A1E26),
    surfaceContainerLow = Color(0xFF1F242D),
    surfaceContainer = Color(0xFF252A34),
    surfaceContainerHigh = Color(0xFF2C323D),
    surfaceContainerHighest = Color(0xFF363D4A),
    outline = Color(0xFF8C8878),
    outlineVariant = Color(0xFF4A4430),
    error = Color(0xFFF0726A),
    onError = Color(0xFF111318),
    errorContainer = Color(0xFF3B1F1F),
    onErrorContainer = Color(0xFFF0726A),
)

private val LightColors = lightColorScheme(
    primary = Color(0xFF8A6A0C),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF96750F),
    onPrimaryContainer = Color(0xFFFFFFFF),
    secondary = Color(0xFF252A34),
    onSecondary = Color(0xFFF4F0E6),
    secondaryContainer = Color(0xFFE8E1CE),
    onSecondaryContainer = Color(0xFF252A34),
    tertiary = Color(0xFF7A5F0B),
    onTertiary = Color(0xFFFFFFFF),
    background = Color(0xFFF4F0E6),
    onBackground = Color(0xFF111318),
    surface = Color(0xFFF4F0E6),
    onSurface = Color(0xFF111318),
    surfaceVariant = Color(0xFFDDD4BC),
    onSurfaceVariant = Color(0xFF5B5D66),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFFAF7EE),
    surfaceContainer = Color(0xFFEFE9DA),
    surfaceContainerHigh = Color(0xFFE8E1CE),
    surfaceContainerHighest = Color(0xFFDDD4BC),
    outline = Color(0xFF8A8574),
    outlineVariant = Color(0xFFD3C9AE),
    error = Color(0xFFC0392B),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFBE1DE),
    onErrorContainer = Color(0xFFC0392B),
)

@Composable
fun BTicketTheme(dark: Boolean, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (dark) DarkColors else LightColors,
        content = content,
    )
}
