package pe.edu.upeu.pharmamobile.data.repository

import kotlinx.coroutines.delay
import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.domain.repository.ProductoRepository

class ProductRepositoryEnMemoria : ProductoRepository {
    private val productos = mutableListOf<Producto>()
    private var contadorId = 1L

    override suspend fun registrar(producto: Producto): Producto {
        delay(500) // Simula latencia de red
        val nuevoProducto = producto.copy(id = contadorId++)
        productos.add(nuevoProducto)
        return nuevoProducto
    }

    override suspend fun listar(): List<Producto> {
        delay(500)
        return productos.toList()
    }
}