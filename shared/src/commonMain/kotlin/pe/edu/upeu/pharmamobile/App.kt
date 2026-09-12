package pe.edu.upeu.pharmamobile

import androidx.compose.runtime.*
import org.koin.compose.KoinContext
import org.koin.compose.viewmodel.koinViewModel
import pe.edu.upeu.pharmamobile.navegation.Screen
import pe.edu.upeu.pharmamobile.presentation.Pedidos.PedidosScreen
import pe.edu.upeu.pharmamobile.presentation.cliente.ClientesScreen
import pe.edu.upeu.pharmamobile.presentation.inicio.InicioScreen
import pe.edu.upeu.pharmamobile.presentation.producto.ProductoScreen
import pe.edu.upeu.pharmamobile.presentation.theme.PharmaMobilTheme

@Composable
fun App() {
    // Declaramos el estado del tema
    var darkTheme by remember { mutableStateOf(false) }

    // Declaramos el estado de la pantalla actual
    var pantallaActual by remember { mutableStateOf<Screen>(Screen.Inicio) }

    KoinContext {
        PharmaMobilTheme(darkTheme = darkTheme) {
            // ... resto de navegación (Drawer, Scaffold, etc.)
            when (pantallaActual) {
                Screen.Inicio -> InicioScreen()
                Screen.Productos -> ProductoScreen(viewModel = koinViewModel())
                Screen.Clientes -> ClientesScreen()
                Screen.Pedidos -> PedidosScreen()
            }
        }
    }
}