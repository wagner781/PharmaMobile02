package pe.edu.upeu.pharmamobile

import androidx.compose.runtime.*
import pe.edu.upeu.pharmamobile.presentation.MainScreen
import pe.edu.upeu.pharmamobile.presentation.theme.PharmaMobilTheme

@Composable
fun App() {
    var darkTheme by remember { mutableStateOf(false) }

    PharmaMobilTheme(darkTheme = darkTheme) {
        MainScreen(
            darkTheme = darkTheme,
            onToggleTheme = { darkTheme = !darkTheme }
        )
    }
}