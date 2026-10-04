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
import pe.edu.upeu.pharmamobile.domain.model.Producto
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit

@Composable
fun ProductoScreen(viewModel: ProductoViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val formulario = uiState.formulario

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Registro de Producto", style = MaterialTheme.typography.headlineSmall)

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

        Spacer(modifier = Modifier.height(16.dp))

        // Lista de productos según fase
        when (val fase = uiState.fase) {
            ProductoUiState.Fase.Cargando -> IndicadorCarga()
            ProductoUiState.Fase.SinProductos -> EstadoVacio("No hay productos registrados")
            is ProductoUiState.Fase.ConProductos -> ListaProductos(
                productos = fase.productos,
                onEdit = { viewModel.editarProducto(it) },
                onDelete = { viewModel.eliminar(it.id) },
                operacionEnCurso = uiState.operacion
            )
            is ProductoUiState.Fase.Error -> EstadoError(fase.mensaje)
        }
    }
}

@Composable
private fun IndicadorCarga() {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun EstadoVacio(mensaje: String) {
    Text(mensaje)
}

@Composable
private fun ListaProductos(
    productos: List<Producto>,
    onEdit: (Producto) -> Unit,
    onDelete: (Producto) -> Unit,
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
                        Text("Precio: S/. ${producto.precio}", style = MaterialTheme.typography.bodyMedium)
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
                    }
                    Row {
                        IconButton(
                            onClick = { onEdit(producto) },
                            enabled = operacionEnCurso !is ProductoUiState.Operacion.EnCurso
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar")
                        }
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

@Composable
private fun EstadoError(mensaje: String) {
    Text("Error: $mensaje", color = Color.Red)
}