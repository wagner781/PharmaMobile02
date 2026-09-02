package pe.edu.upeu.pharmamobile.presentation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.*
import androidx.compose.material3.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pe.edu.upeu.pharmamobile.navegation.Screen
import pe.edu.upeu.pharmamobile.presentation.producto.ProductoScreen
import pe.edu.upeu.pharmamobile.presentation.cliente.ClientesScreen
import pe.edu.upeu.pharmamobile.presentation.Pedidos.PedidosScreen
import kotlinx.coroutines.launch
import pe.edu.upeu.pharmamobile.presentation.inicio.InicioScreen

@Composable
fun MainScreen(
    darkTheme: Boolean,
    onToggleTheme: () -> Unit
) {

    // Estado que guarda la pantalla actual
    var pantallaActual by remember { mutableStateOf<Screen>(Screen.Inicio) }

    // Estado y corrutina para el Drawer
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Título dinámico según la pantalla
    fun getTitle(screen: Screen): String {
        return when (screen) {
            Screen.Inicio -> "Inicio"
            Screen.Productos -> "Registro de Productos"
            Screen.Clientes -> "Clientes"
            Screen.Pedidos -> "Pedidos"
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                // Encabezado del Drawer
                Text(
                    text = "PharmaMobil",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(16.dp)
                )
                HorizontalDivider()

                // Opciones de navegación
                NavigationDrawerItem(
                    label = { Text("Inicio") },
                    selected = pantallaActual == Screen.Inicio,
                    onClick = {
                        pantallaActual = Screen.Inicio
                        scope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                NavigationDrawerItem(
                    label = { Text("Productos") },
                    selected = pantallaActual == Screen.Productos,
                    onClick = {
                        pantallaActual = Screen.Productos
                        scope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                NavigationDrawerItem(
                    label = { Text("Clientes") },
                    selected = pantallaActual == Screen.Clientes,
                    onClick = {
                        pantallaActual = Screen.Clientes
                        scope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                NavigationDrawerItem(
                    label = { Text("Pedidos") },
                    selected = pantallaActual == Screen.Pedidos,
                    onClick = {
                        pantallaActual = Screen.Pedidos
                        scope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }
        }
    ) {
        // Scaffold con TopAppBar
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(getTitle(pantallaActual)) },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch { drawerState.open() }
                        }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Abrir menú"
                            )
                        }
                    }
                )
            }
        ) { paddingValues ->
            // Contenido principal con padding de la barra
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (pantallaActual) {
                    Screen.Inicio -> InicioScreen()
                    Screen.Productos -> ProductoScreen()
                    Screen.Clientes -> ClientesScreen()
                    Screen.Pedidos -> PedidosScreen()
                }
            }
        }
    }
}