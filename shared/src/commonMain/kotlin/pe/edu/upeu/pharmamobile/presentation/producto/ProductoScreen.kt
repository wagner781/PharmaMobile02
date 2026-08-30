package pe.edu.upeu.pharmamobile.presentation.producto

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
import pe.edu.upeu.pharmamobile.domain.model.Producto

@Composable
fun ProductoScreen() {
    // ============================================================
    // PASOS 1 al 4: DECLARACIÓN DE ESTADOS
    // ============================================================

    // Estados de los campos del formulario (como String para capturar entrada libre)
    var nombre by remember { mutableStateOf("") }
    var precioStr by remember { mutableStateOf("") }
    var stockStr by remember { mutableStateOf("") }

    // Estado para el mensaje de resultado (éxito o error)
    var mensaje by remember { mutableStateOf("") }
    var esExito by remember { mutableStateOf(false) }

    // PASOS 3 y 4: Control de envío (para mostrar errores SOLO después de intentar registrar)
    var intentoRegistrar by remember { mutableStateOf(false) }

    // ============================================================
    // ESTRUCTURA VISUAL (Column)
    // ============================================================
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Título
        Text(
            text = "Registro de Producto",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // ============================================================
        // CAMPO 1: NOMBRE (con validación visual)
        // ============================================================
        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre del producto") },
            modifier = Modifier.fillMaxWidth(),
            // PASO 9: Mostrar error visual solo después de intentar registrar
            isError = intentoRegistrar && nombre.isBlank()
        )

        // ============================================================
        // CAMPO 2: PRECIO (con validación visual)
        // ============================================================
        OutlinedTextField(
            value = precioStr,
            onValueChange = { precioStr = it },
            label = { Text("Precio (ej: 8.50)") },
            modifier = Modifier.fillMaxWidth(),
            // PASO 9: Mostrar error visual si el precio no es válido
            isError = intentoRegistrar && (
                    precioStr.toDoubleOrNull() == null ||
                            precioStr.toDoubleOrNull()!! <= 0.0
                    )
        )

        // ============================================================
        // CAMPO 3: STOCK (con validación visual)
        // ============================================================
        OutlinedTextField(
            value = stockStr,
            onValueChange = { stockStr = it },
            label = { Text("Stock (ej: 100)") },
            modifier = Modifier.fillMaxWidth(),
            // PASO 9: Mostrar error visual si el stock no es válido
            isError = intentoRegistrar && (
                    stockStr.toIntOrNull() == null ||
                            stockStr.toIntOrNull()!! < 0
                    )
        )

        // ============================================================
        // BOTÓN REGISTRAR
        // ============================================================
        Button(
            onClick = {
                // PASO 4: Marcamos que se intentó registrar (para activar errores visuales)
                intentoRegistrar = true

                // ============================================================
                // PASOS 5, 6 y 7: CONVERSIÓN SEGURA Y VALIDACIÓN SECUENCIAL
                // ============================================================

                // 1. Validar NOMBRE (no vacío ni solo espacios)
                if (nombre.isBlank()) {
                    mensaje = "El nombre es obligatorio."
                    esExito = false
                    return@Button
                }

                // 2. Validar PRECIO (conversión segura y rango)
                val precio = precioStr.toDoubleOrNull()
                if (precio == null) {
                    mensaje = "Ingrese un precio numérico."
                    esExito = false
                    return@Button
                }
                if (precio <= 0.0) {
                    mensaje = "El precio debe ser mayor que cero."
                    esExito = false
                    return@Button
                }

                // 3. Validar STOCK (conversión segura y rango)
                val stock = stockStr.toIntOrNull()
                if (stock == null) {
                    mensaje = "Ingrese un stock entero."
                    esExito = false
                    return@Button
                }
                if (stock < 0) {
                    mensaje = "El stock no puede ser negativo."
                    esExito = false
                    return@Button
                }

                // ============================================================
                // PASO 7: CREACIÓN DEL OBJETO PRODUCTO (solo si todo es válido)
                // ============================================================
                val nuevoProducto = Producto(
                    id = 0L, // En la BD real se genera automáticamente
                    nombre = nombre.trim(),
                    precio = precio,
                    stock = stock
                )

                // ============================================================
                // PASO 8: LIMPIEZA DEL FORMULARIO (después de registro exitoso)
                // ============================================================
                nombre = ""
                precioStr = ""
                stockStr = ""
                intentoRegistrar = false // Reseteamos para que los campos dejen de estar en rojo

                // Mostramos mensaje de éxito
                mensaje = "✅ Producto registrado correctamente: ${nuevoProducto.nombre}"
                esExito = true
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Text("REGISTRAR")
        }

        // ============================================================
        // MENSAJE DE RESULTADO (reactivo)
        // ============================================================
        if (mensaje.isNotEmpty()) {
            Text(
                text = mensaje,
                color = if (esExito) Color.Green else Color.Red,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}