package pe.edu.upeu.pharmamobile.presentation.producto

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import pe.edu.upeu.pharmamobile.domain.model.EnvioProductoFallido
import pe.edu.upeu.pharmamobile.platform.formatearAntiguedad
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ContentCopy

import androidx.compose.material.icons.filled.Refresh

@Composable
fun ProductoScreen(
    viewModel: ProductoViewModel,
    puedeRegistrar: Boolean = true
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val formulario = uiState.formulario
    val sincronizacion = uiState.sincronizacion
    val fallidas = sincronizacion.envios.fallidas
    var mostrarFallos by remember { mutableStateOf(false) }

    if (mostrarFallos) {
        DetalleEnviosFallidos(fallidas, onCerrar = { mostrarFallos = false })
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Catálogo de Productos", style = MaterialTheme.typography.headlineSmall)
            IconButton(
                onClick = { viewModel.sincronizar() },
                enabled = !sincronizacion.enCurso
            ) {
                if (sincronizacion.enCurso) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.Refresh, contentDescription = "Sincronizar")
                }
            }
        }

        ResumenSincronizacion(sincronizacion, onRevisar = { mostrarFallos = true })

        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
                TextButton(
                    onClick = { viewModel.simularSincronizacionAntigua(2) },
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                ) {
                    Text("Simular >1 día", style = MaterialTheme.typography.labelSmall)
                }
                TextButton(
                    onClick = { viewModel.limpiarBaseLocalParaPrueba() },
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                ) {
                    Text("Vaciar local", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                }
        }

        if (puedeRegistrar) {

        // Formulario
        OutlinedTextField(
            value = formulario.nombre,
            onValueChange = { viewModel.actualizarFormulario(nombre = it) },
            label = { Text("Nombre") },
            modifier = Modifier.fillMaxWidth(),
            isError = formulario.nombreError != null
        )
        if (formulario.nombreError != null) {
            Text(formulario.nombreError!!, color = Color.Red, style = MaterialTheme.typography.bodySmall)
        }

        OutlinedTextField(
            value = formulario.precio,
            onValueChange = { viewModel.actualizarFormulario(precio = it) },
            label = { Text("Precio") },
            modifier = Modifier.fillMaxWidth(),
            isError = formulario.precioError != null
        )
        if (formulario.precioError != null) {
            Text(formulario.precioError!!, color = Color.Red, style = MaterialTheme.typography.bodySmall)
        }

        OutlinedTextField(
            value = formulario.stock,
            onValueChange = { viewModel.actualizarFormulario(stock = it) },
            label = { Text("Stock") },
            modifier = Modifier.fillMaxWidth(),
            isError = formulario.stockError != null
        )
        if (formulario.stockError != null) {
            Text(formulario.stockError!!, color = Color.Red, style = MaterialTheme.typography.bodySmall)
        }

        Button(
            onClick = { viewModel.guardarProducto() },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            enabled = uiState.operacion !is ProductoUiState.Operacion.EnCurso
        ) {
            if (uiState.operacion is ProductoUiState.Operacion.EnCurso) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp))
            } else {
                Text(if (formulario.id == null) "Registrar" else "Actualizar")
            }
        }

        if (formulario.id != null) {
            TextButton(
                onClick = { viewModel.actualizarFormulario(id = null, nombre = "", precio = "", stock = "") },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.operacion !is ProductoUiState.Operacion.EnCurso
            ) {
                Text("Cancelar edición")
            }
        }

        if (uiState.mensajeExito != null) {
            Text(
                text = uiState.mensajeExito!!,
                color = Color.Green,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
        if (uiState.operacion is ProductoUiState.Operacion.Fallida) {
            val fallo = uiState.operacion as ProductoUiState.Operacion.Fallida
            Text(
                text = fallo.mensaje,
                color = Color.Red,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
        } else {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    text = "Modo solo lectura: No cuenta con permisos para registrar o modificar productos.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Lista de productos según fase
        when (val fase = uiState.fase) {
            ProductoUiState.Fase.Cargando -> IndicadorCarga()
            ProductoUiState.Fase.SinProductos -> EstadoVacio("No hay productos registrados")
            is ProductoUiState.Fase.ConProductos -> ListaProductos(
                productos = fase.productos,
                idsFallidos = fallidas.map { it.id }.toSet(),
                puedeRegistrar = puedeRegistrar,
                onEdit = { viewModel.editarProducto(it) },
                onDelete = { viewModel.eliminar(it.id) },
                onShare = { viewModel.compartir(it) },
                onCopy = { viewModel.copiar(it) },
                operacionEnCurso = uiState.operacion
            )
            is ProductoUiState.Fase.Error -> EstadoError(fase.mensaje)
        }
    }
}

@Composable
private fun ResumenSincronizacion(estado: SincronizacionUiState, onRevisar: () -> Unit) {
    val ultima = estado.envios.ultimaSincronizacion
    var antiguedad by remember(ultima) { mutableStateOf(formatearAntiguedad(ultima)) }
    LaunchedEffect(ultima) {
        if (ultima != null) {
            while (true) {
                delay(30_000)
                antiguedad = formatearAntiguedad(ultima)
            }
        }
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            Text(
                text = estado.estadoConexion,
                style = MaterialTheme.typography.titleSmall,
                color = if (estado.hayError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
            )
            Text(antiguedad, style = MaterialTheme.typography.bodySmall)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Pendientes: ${estado.envios.pendientes} · Fallidas: ${estado.envios.fallidas.size}",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium
                )
                if (estado.envios.fallidas.isNotEmpty()) {
                    TextButton(onClick = onRevisar) { Text("Revisar fallos") }
                }
            }
        }
    }
}

@Composable
private fun DetalleEnviosFallidos(fallidas: List<EnvioProductoFallido>, onCerrar: () -> Unit) {
    AlertDialog(
        onDismissRequest = onCerrar,
        title = { Text("Envíos fallidos") },
        text = {
            if (fallidas.isEmpty()) {
                Text("No hay envíos fallidos.")
            } else {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 320.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(fallidas, key = { it.id }) { fallo ->
                        Column {
                            Text(fallo.nombre, style = MaterialTheme.typography.titleSmall)
                            Text(fallo.motivo, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onCerrar) { Text("Cerrar") } }
    )
}

@Composable
private fun IndicadorCarga() {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun EstadoVacio(mensaje: String) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("📦", style = MaterialTheme.typography.headlineLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = mensaje,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ListaProductos(
    productos: List<ProductoUi>,
    idsFallidos: Set<Long>,
    puedeRegistrar: Boolean = true,
    onEdit: (ProductoUi) -> Unit,
    onDelete: (ProductoUi) -> Unit,
    onShare: (ProductoUi) -> Unit,
    onCopy: (ProductoUi) -> Unit,
    operacionEnCurso: ProductoUiState.Operacion
) {
    LazyColumn {
        items(productos) { producto ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(producto.nombre, style = MaterialTheme.typography.bodyLarge)
                        Text("Precio: ${producto.precio}", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = "Stock: ${producto.stock}",
                            color = when {
                                producto.stock == 0 -> Color.Red
                                producto.stock <= 5 -> Color(0xFFFFA500)
                                else -> Color.Green
                            }
                        )
                        if (producto.requiereReposicion) {
                            Text("⚠️ Requiere reposición", color = Color(0xFFFFA500))
                        }
                        if (producto.id < 0) {
                            val fallido = producto.id in idsFallidos
                            Surface(
                                shape = MaterialTheme.shapes.extraSmall,
                                color = if (fallido) MaterialTheme.colorScheme.errorContainer
                                    else MaterialTheme.colorScheme.tertiaryContainer,
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                Text(
                                    text = if (fallido) "Envío fallido" else "Pendiente de envío (ID: ${producto.id})",
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Row {
                        if (puedeRegistrar) {
                            IconButton(
                                onClick = { onEdit(producto) },
                                enabled = operacionEnCurso !is ProductoUiState.Operacion.EnCurso
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Editar")
                            }
                        }
                        IconButton(
                            onClick = { onCopy(producto) },
                            enabled = operacionEnCurso !is ProductoUiState.Operacion.EnCurso
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copiar")
                        }
                        IconButton(
                            onClick = { onShare(producto) },
                            enabled = operacionEnCurso !is ProductoUiState.Operacion.EnCurso
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Compartir", tint = MaterialTheme.colorScheme.primary)
                        }
                        if (puedeRegistrar) {
                            IconButton(
                                onClick = { onDelete(producto) },
                                enabled = operacionEnCurso !is ProductoUiState.Operacion.EnCurso
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.Red)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EstadoError(mensaje: String) {
    Text("Error: $mensaje", color = Color.Red)
}
