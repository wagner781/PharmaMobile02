package pe.edu.upeu.pharmamobile.data.repository

import pe.edu.upeu.pharmamobile.data.mapper.toDomain
import pe.edu.upeu.pharmamobile.data.mapper.toRequest
import pe.edu.upeu.pharmamobile.data.remote.ProductoApi
import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.domain.repository.ProductoRepository

class ProductoRepositorioRest(
    private val api: ProductoApi,
    private val categoriaPorDefecto: Long
) : ProductoRepository {

    override suspend fun listar(): List<Producto> =
        api.listar().contenido.map { it.toDomain() }

    override suspend fun obtener(id: Long): Producto =
        api.obtener(id).toDomain()

    override suspend fun registrar(producto: Producto): Producto =
        api.crear(producto.toRequest(categoriaPorDefecto)).toDomain()

    override suspend fun actualizar(producto: Producto): Producto =
        api.actualizar(producto.id, producto.toRequest(categoriaPorDefecto)).toDomain()

    override suspend fun eliminar(id: Long) = api.eliminar(id)
}
