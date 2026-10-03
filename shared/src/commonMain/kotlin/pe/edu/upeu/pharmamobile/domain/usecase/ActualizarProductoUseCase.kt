package pe.edu.upeu.pharmamobile.domain.usecase

import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.domain.repository.ProductoRepository
import pe.edu.upeu.pharmamobile.data.remote.ejecutarLlamada

class ActualizarProductoUseCase(
    private val repository: ProductoRepository
) {
    suspend operator fun invoke(producto: Producto): Result<Producto> = ejecutarLlamada {
        repository.actualizar(producto)
    }
}
