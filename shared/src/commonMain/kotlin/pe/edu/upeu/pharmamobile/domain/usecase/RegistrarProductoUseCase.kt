package pe.edu.upeu.pharmamobile.domain.usecase

import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.domain.repository.ProductoRepository

class RegistrarProductoUseCase(
    private val repository: ProductoRepository
) {
    // Clase interna para errores de validación
    data class ErroresProducto(
        val nombre: String? = null,
        val precio: String? = null,
        val stock: String? = null
    ) {
        val hayAlguno: Boolean get() = nombre != null || precio != null || stock != null
    }

    suspend operator fun invoke(
        nombre: String,
        precioStr: String,
        stockStr: String
    ): Result<Producto> {
        // Validaciones
        val errores = ErroresProducto(
            nombre = if (nombre.isBlank()) "El nombre es obligatorio" else null,
            precio = when {
                precioStr.isBlank() -> "El precio es obligatorio"
                precioStr.toDoubleOrNull() == null -> "Ingrese un precio numérico"
                precioStr.toDoubleOrNull()!! <= 0.0 -> "El precio debe ser mayor que cero"
                else -> null
            },
            stock = when {
                stockStr.isBlank() -> "El stock es obligatorio"
                stockStr.toIntOrNull() == null -> "Ingrese un stock entero"
                stockStr.toIntOrNull()!! < 0 -> "El stock no puede ser negativo"
                else -> null
            }
        )

        if (errores.hayAlguno) {
            return Result.failure(IllegalStateException(errores.toString()))
        }

        // Crear producto y delegar en el repositorio
        val producto = Producto(
            id = 0L, // El repositorio asignará el ID
            nombre = nombre.trim(),
            precio = precioStr.toDouble(),
            stock = stockStr.toInt()
        )
        return runCatching { repository.registrar(producto) }
    }
}