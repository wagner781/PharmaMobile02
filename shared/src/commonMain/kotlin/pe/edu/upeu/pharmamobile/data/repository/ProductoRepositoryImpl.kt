package pe.edu.upeu.pharmamobile.data.repository

import pe.edu.upeu.pharmamobile.data.mapper.toDomain
import pe.edu.upeu.pharmamobile.data.remote.ProductoApi
import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.domain.repository.ProductoRepository

class ProductoRepositoryImpl(
    private val api: ProductoApi
) : ProductoRepository {
    override suspend fun registrar(producto: Producto): Producto {
        TODO("Not yet implemented")
    }

    override suspend fun listar(): Result<List<Producto>> = runCatching {
        api.obtenerProductos().map { it.toDomain() }
    }
}
