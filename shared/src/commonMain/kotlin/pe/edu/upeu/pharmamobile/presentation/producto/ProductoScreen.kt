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
            onClick = { viewModel.registrarProducto() },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            enabled = !uiState.guardando
        ) {
            if (uiState.guardando) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp))
            } else {
                Text("Registrar")
            }
        }

        if (uiState.mensaje != null) {
            Text(
                text = uiState.mensaje!!,
                color = if (uiState.guardando) Color.Gray else Color.Green,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Lista de productos según fase
        when (val fase = uiState.fase) {
            ProductoUiState.Fase.Cargando -> {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            ProductoUiState.Fase.SinProductos -> {
                Text("No hay productos registrados")
            }
            ProductoUiState.Fase.ConProductos -> {
                LazyColumn {
                    items(uiState.productos) { producto ->
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
                                Column {
                                    Text(producto.nombre, style = MaterialTheme.typography.bodyLarge)
                                    Text("Precio: S/. ${producto.precio}", style = MaterialTheme.typography.bodyMedium)
                                    if (producto.requiereReposicion) {
                                        Text("⚠️ Requiere reposición", color = Color(0xFFFFA500))
                                    }
                                }
                                Text(
                                    text = "Stock: ${producto.stock}",
                                    color = when {
                                        producto.stock == 0 -> Color.Red
                                        producto.stock <= 5 -> Color(0xFFFFA500)
                                        else -> Color.Green
                                    }
                                )
                            }
                        }
                    }
                }
            }
            is ProductoUiState.Fase.Error -> {
                Text("Error: ${fase.detalle}", color = Color.Red)
            }
        }
    }
}