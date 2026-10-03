package pe.edu.upeu.pharmamobile.domain.usecase

import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.domain.repository.ProductoRepository
import pe.edu.upeu.pharmamobile.data.remote.ejecutarLlamada

class EliminarProductoUseCase(
    private val repository: ProductoRepository
) {
    suspend operator fun invoke(id: Long): Result<Unit> = ejecutarLlamada {
        repository.eliminar(id)
    }
}
