package pe.edu.upeu.pharmamobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import org.koin.android.ext.android.get
import org.koin.core.parameter.parametersOf
import pe.edu.upeu.pharmamobile.domain.permiso.GestorPermisos
import pe.edu.upeu.pharmamobile.permiso.enviarNotificacionDePrueba
import pe.edu.upeu.pharmamobile.presentation.MainScreen
import pe.edu.upeu.pharmamobile.presentation.inicio.PermisoNotificaciones
import pe.edu.upeu.pharmamobile.presentation.theme.PharmaMobilTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val gestorPermisos = get<GestorPermisos> { parametersOf(this) }
        setContent {
            // 1. Estado que guarda si el tema oscuro está activado o no
            var darkTheme by remember { mutableStateOf(false) }

            // 2. Aplicamos el tema dinámico
            PharmaMobilTheme(darkTheme = darkTheme) {
                // 3. Pasamos el estado y la función para cambiarlo a la pantalla principal
                MainScreen(
                    darkTheme = darkTheme,
                    onToggleTheme = { darkTheme = !darkTheme },
                    contenidoInicio = {
                        PermisoNotificaciones(gestorPermisos, enviarPrueba = { enviarNotificacionDePrueba() })
                    }
                )
            }
        }
    }
}
