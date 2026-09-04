package pe.edu.upeu.pharmamobile.presentation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.*
import androidx.compose.material3.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import pe.edu.upeu.pharmamobile.navegation.Screen
import pe.edu.upeu.pharmamobile.presentation.producto.ProductoScreen
import pe.edu.upeu.pharmamobile.presentation.cliente.ClientesScreen
import pe.edu.upeu.pharmamobile.presentation.Pedidos.PedidosScreen
import kotlinx.coroutines.launch
import pe.edu.upeu.pharmamobile.presentation.inicio.InicioScreen

@Composable
fun MainScreen(
    darkTheme: Boolean,         // Recibimos el estado del tema
    onToggleTheme: () -> Unit   // Recibimos la función para cambiarlo
) {
    // Estado de la pantalla actual (Inicio por defecto)
    var pantallaActual by remember { mutableStateOf<Screen>(Screen.Inicio) }

    // Estado del Drawer (cerrado por defecto)
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Función para obtener el título dinámico según la pantalla
    fun getTitle(screen: Screen): String {
        return when (screen) {
            Screen.Inicio -> "Inicio"
            Screen.Productos -> "Registro de Productos"
            Screen.Clientes -> "Clientes"
            Screen.Pedidos -> "Pedidos"
        }
    }

    // Estructura principal: Drawer envolviendo el Scaffold
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                // --- ENCABEZADO DEL DRAWER ---
                Text(
                    text = "PharmaMobil",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(16.dp)
                )
                HorizontalDivider()

                // --- OPCIONES DE NAVEGACIÓN ---
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
                // --- SEPARADOR ANTES DEL SWITCH ---
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // --- PASO 7: SWITCH PARA CAMBIAR EL TEMA ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tema Oscuro",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Switch(
                        checked = darkTheme,  // El estado actual
                        onCheckedChange = {
                            onToggleTheme()   // Llamamos a la función para invertir el estado
                        }
                    )
                }
            }
        }
    ) {
        // --- SCAFFOLD (Estructura de la pantalla) ---
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
            // Contenido dinámico según la pantalla seleccionada
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