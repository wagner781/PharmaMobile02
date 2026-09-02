package pe.edu.upeu.pharmamobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import pe.edu.upeu.pharmamobile.presentation.MainScreen
import pe.edu.upeu.pharmamobile.presentation.theme.PharmaMobilTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // 1. Estado que guarda si el tema oscuro está activado o no
            var darkTheme by remember { mutableStateOf(false) }

            // 2. Aplicamos el tema dinámico
            PharmaMobilTheme(darkTheme = darkTheme) {
                // 3. Pasamos el estado y la función para cambiarlo a la pantalla principal
                MainScreen(
                    darkTheme = darkTheme,
                    onToggleTheme = { darkTheme = !darkTheme }
                )
            }
        }
    }
}