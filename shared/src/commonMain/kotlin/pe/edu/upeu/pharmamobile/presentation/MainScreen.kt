package pe.edu.upeu.pharmamobile.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import pe.edu.upeu.pharmamobile.navegation.Screen
import pe.edu.upeu.pharmamobile.presentation.Pedidos.PedidosScreen
import pe.edu.upeu.pharmamobile.presentation.cliente.ClientesScreen
import pe.edu.upeu.pharmamobile.presentation.inicio.InicioScreen
import pe.edu.upeu.pharmamobile.presentation.producto.ProductoScreen
import pe.edu.upeu.pharmamobile.presentation.producto.ProductoViewModel
import pe.edu.upeu.pharmamobile.presentation.sesion.LoginScreen
import pe.edu.upeu.pharmamobile.presentation.sesion.SesionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    darkTheme: Boolean,
    onToggleTheme: () -> Unit,
    sesionViewModel: SesionViewModel = koinViewModel()
) {
    val sesionState by sesionViewModel.uiState.collectAsStateWithLifecycle()

    // 1. Si está verificando si hay token guardado al arrancar
    if (sesionState.cargando && sesionState.sesion == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    // 2. Si no hay sesión activa -> Mostrar LoginScreen
    if (sesionState.sesion == null) {
        LoginScreen(viewModel = sesionViewModel)
        return
    }

    val sesion = sesionState.sesion!!
    var pantallaActual by remember { mutableStateOf<Screen>(Screen.Inicio) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    fun getTitle(screen: Screen): String {
        return when (screen) {
            Screen.Inicio -> "Inicio"
            Screen.Productos -> "Productos"
            Screen.Clientes -> "Clientes"
            Screen.Pedidos -> "Pedidos"
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                // --- ENCABEZADO DEL DRAWER CON DATOS DEL USUARIO ---
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "PharmaMobil",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = sesion.nombreCompleto.ifBlank { sesion.usuario },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Usuario: ${sesion.usuario}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
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

                // Sección Clientes solo si tiene permiso CLIENTES_LEER
                if (sesion.puede("CLIENTES_LEER")) {
                    NavigationDrawerItem(
                        label = { Text("Clientes") },
                        selected = pantallaActual == Screen.Clientes,
                        onClick = {
                            pantallaActual = Screen.Clientes
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }

                // Sección Pedidos/Ventas solo si tiene permiso VENTAS_LEER
                if (sesion.puede("VENTAS_LEER")) {
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

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // Switch Tema Oscuro
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tema Oscuro",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Switch(
                        checked = darkTheme,
                        onCheckedChange = { onToggleTheme() }
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // Acción Cerrar Sesión
                NavigationDrawerItem(
                    label = { Text("Cerrar Sesión", color = MaterialTheme.colorScheme.error) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Cerrar sesión",
                            tint = MaterialTheme.colorScheme.error
                        )
                    },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        sesionViewModel.cerrarSesion()
                        pantallaActual = Screen.Inicio
                    },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }
        }
    ) {
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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (pantallaActual) {
                    Screen.Inicio -> InicioScreen()
                    Screen.Productos -> {
                        val viewModel: ProductoViewModel = koinViewModel()
                        ProductoScreen(
                            viewModel = viewModel,
                            puedeRegistrar = sesion.puede("CATALOGO_ESCRIBIR")
                        )
                    }
                    Screen.Clientes -> {
                        if (sesion.puede("CLIENTES_LEER")) {
                            ClientesScreen()
                        } else {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No tiene permisos para ver clientes.")
                            }
                        }
                    }
                    Screen.Pedidos -> {
                        if (sesion.puede("VENTAS_LEER")) {
                            PedidosScreen()
                        } else {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No tiene permisos para ver pedidos.")
                            }
                        }
                    }
                }
            }
        }
    }
}