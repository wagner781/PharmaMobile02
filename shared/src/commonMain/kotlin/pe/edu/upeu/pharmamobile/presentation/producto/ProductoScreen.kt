package pe.edu.upeu.pharmamobile.presentation.producto

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import pe.edu.upeu.pharmamobile.data.productosMock
import pe.edu.upeu.pharmamobile.domain.model.Producto

@Composable
fun ProductoScreen() {
    // Estados para el formulario
    var nombre by remember { mutableStateOf("") }
    var precioStr by remember { mutableStateOf("") }
    var stockStr by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf("") }
    var esExito by remember { mutableStateOf(false) }
    var intentoRegistrar by remember { mutableStateOf(false) }

    // Paso 3: Estado de la pestaña seleccionada (0: Activos, 1: Inactivos, 2: Bajo stock)
    var tabSeleccionada by remember { mutableStateOf(0) }
    val tabs = listOf("Activos", "Inactivos", "Bajo stock")

    // Función para filtrar productos según la pestaña
    fun productosFiltrados(): List<Producto> {
        return when (tabSeleccionada) {
            0 -> productosMock.filter { it.stock > 0 && it.stock > 5 } // Activos (stock > 5)
            1 -> productosMock.filter { it.stock == 0 }                // Inactivos (stock = 0)
            2 -> productosMock.filter { it.stock in 1..5 }             // Bajo stock (1 a 5)
            else -> emptyList()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Título
        Text(
            text = "Registro de Producto",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // --- FORMULARIO (igual que antes) ---
        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre del producto") },
            modifier = Modifier.fillMaxWidth(),
            isError = intentoRegistrar && nombre.isBlank()
        )
        OutlinedTextField(
            value = precioStr,
            onValueChange = { precioStr = it },
            label = { Text("Precio (ej: 8.50)") },
            modifier = Modifier.fillMaxWidth(),
            isError = intentoRegistrar && (precioStr.toDoubleOrNull() == null || precioStr.toDoubleOrNull()!! <= 0.0)
        )
        OutlinedTextField(
            value = stockStr,
            onValueChange = { stockStr = it },
            label = { Text("Stock (ej: 100)") },
            modifier = Modifier.fillMaxWidth(),
            isError = intentoRegistrar && (stockStr.toIntOrNull() == null || stockStr.toIntOrNull()!! < 0)
        )

        // ============================================================
        // BOTÓN REGISTRAR
        // ============================================================
        Button(
            onClick = {
                intentoRegistrar = true
                if (nombre.isBlank()) {
                    mensaje = "El nombre es obligatorio."
                    esExito = false
                    return@Button
                }
                val precio = precioStr.toDoubleOrNull()
                if (precio == null || precio <= 0.0) {
                    mensaje = "Ingrese un precio válido."
                    esExito = false
                    return@Button
                }
                val stock = stockStr.toIntOrNull()
                if (stock == null || stock < 0) {
                    mensaje = "El stock no puede ser negativo."
                    esExito = false
                    return@Button
                }
                // Crear producto (simulado, no se añade a la lista mock para no complicar)
                mensaje = "✅ Producto registrado: $nombre"
                esExito = true
                nombre = ""
                precioStr = ""
                stockStr = ""
                intentoRegistrar = false
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            Text("REGISTRAR")
        }

        if (mensaje.isNotEmpty()) {
            Text(
                text = mensaje,
                color = if (esExito) Color.Green else Color.Red,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- TABS Y LISTA DE PRODUCTOS ---
        Text(
            text = "Inventario",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Paso 3: Tabs
        ScrollableTabRow(
            selectedTabIndex = tabSeleccionada,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.fillMaxWidth()
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = tabSeleccionada == index,
                    onClick = { tabSeleccionada = index },
                    text = { Text(title) }
                )
            }
        }

        // Lista de productos filtrados
        val productos = productosFiltrados()
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            items(productos) { producto ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = producto.nombre, style = MaterialTheme.typography.bodyLarge)
                            Text(text = "Precio: S/. ${producto.precio}", style = MaterialTheme.typography.bodyMedium)
                        }
                        Text(
                            text = "Stock: ${producto.stock}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = when {
                                producto.stock == 0 -> Color.Red
                                producto.stock <= 5 -> Color(0xFFFFA500) // Naranja
                                else -> Color.Green
                            }
                        )
                    }
                }
            }
            if (productos.isEmpty()) {
                item {
                    Text(
                        text = "No hay productos en esta categoría",
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}