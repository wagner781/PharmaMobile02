package pe.edu.upeu.pharmamobile.presentation.producto

// Imports necesarios para Compose Multiplatform
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
import pe.edu.upeu.pharmamobile.domain.model.Producto // Importamos tu entidad Producto

@Composable
fun ProductoScreen() {
    // PASOS 7 y 8: Estado del formulario (variables observables)
    var nombre by remember { mutableStateOf("") }
    var precioStr by remember { mutableStateOf("") }
    var stockStr by remember { mutableStateOf("") }

    // Estado para el mensaje de resultado (éxito o error)
    var mensaje by remember { mutableStateOf("") }
    var esExito by remember { mutableStateOf(false) }

    // PASO 4 y 5: Estructura Column con padding y fillMaxWidth
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Título de la pantalla
        Text(
            text = "Registro de Producto",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // PASO 6: Campo Nombre
        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre del producto") },
            modifier = Modifier.fillMaxWidth()
        )

        // PASO 6: Campo Precio (usamos String para manejar la entrada libre)
        OutlinedTextField(
            value = precioStr,
            onValueChange = { precioStr = it },
            label = { Text("Precio (ej: 8.50)") },
            modifier = Modifier.fillMaxWidth()
        )

        // PASO 6: Campo Stock (usamos String para manejar la entrada libre)
        OutlinedTextField(
            value = stockStr,
            onValueChange = { stockStr = it },
            label = { Text("Stock (ej: 100)") },
            modifier = Modifier.fillMaxWidth()
        )

        // PASO 9: Botón Registrar
        Button(
            onClick = {
                // PASO 10: VALIDACIONES (según los casos de prueba)
                // Caso 02: Nombre vacío
                if (nombre.isBlank()) {
                    mensaje = "Ingrese nombre del producto"
                    esExito = false
                    return@Button
                }

                // Caso 03: Precio inválido (no es número o es <= 0)
                val precio = precioStr.toDoubleOrNull()
                if (precio == null || precio <= 0.0) {
                    mensaje = "Ingrese precio válido"
                    esExito = false
                    return@Button
                }

                // Caso 04: Stock negativo o no es número entero
                val stock = stockStr.toIntOrNull()
                if (stock == null || stock < 0) {
                    mensaje = "El stock no puede ser negativo"
                    esExito = false
                    return@Button
                }

                // PASO 11: SI todo es correcto, CREAMOS el objeto Producto
                val nuevoProducto = Producto(
                    id = 0L, // En la BD real se genera automáticamente, por ahora 0
                    nombre = nombre,
                    precio = precio,
                    stock = stock
                )

                // PASO 12: Mostramos mensaje de éxito (Caso 01)
                mensaje = "Producto registrado correctamente: ${nuevoProducto.nombre}"
                esExito = true
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Text("REGISTRAR")
        }

        // PASO 12: Componente Text reactivo para mostrar mensajes
        if (mensaje.isNotEmpty()) {
            Text(
                text = mensaje,
                color = if (esExito) Color.Green else Color.Red,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}