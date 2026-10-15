package ir.ariakish.app.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Pink = Color(0xFFFF4F83)
val PinkSoft = Color(0xFFFFE6EE)
val Lavender = Color(0xFF6C72E8)
val Sky = Color(0xFFDAEDFF)
val Cream = Color(0xFFFFF1D7)
val Ink = Color(0xFF262126)
val AppBackground = Color(0xFFFFF9FB)

private val Colors = lightColorScheme(
    primary = Pink,
    onPrimary = Color.White,
    primaryContainer = PinkSoft,
    onPrimaryContainer = Color(0xFF631C38),
    secondary = Lavender,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE7E9FF),
    tertiary = Color(0xFFF5B946),
    background = AppBackground,
    surface = Color.White,
    surfaceVariant = Color(0xFFF7F2F5),
    onSurface = Ink,
    outline = Color(0xFFE6DDE1)
)

@Composable
fun AriakishTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Colors, content = content)
}
