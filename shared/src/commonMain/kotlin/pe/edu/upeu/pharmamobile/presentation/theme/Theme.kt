package pe.edu.upeu.pharmamobile.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Paleta de colores para el tema Oscuro
// En Theme.kt, modifica las paletas:
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF4CAF50),        // Verde corporativo
    secondary = Color(0xFF03DAC5),
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF2E7D32),        // Verde oscuro
    secondary = Color(0xFF03DAC5),
    background = Color(0xFFF5F5F5),
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black,
)

@Composable
fun PharmaMobilTheme(
    darkTheme: Boolean = isSystemInDarkTheme(), // Detecta el tema del sistema por defecto
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}