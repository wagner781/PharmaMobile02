package pe.edu.upeu.pharmamobile

import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.domain.repository.ProductoRepository

// Repositorio falso: lista vacía
class RepositorioVacio : ProductoRepository {
    override suspend fun registrar(p: Producto): Producto = p
    override suspend fun listar(): List<Producto> = emptyList()
}

// Repositorio falso: 3 productos. Además "espía" si registrar fue llamado.
class RepositorioConProductos : ProductoRepository {
    var registrarLlamado = false

    override suspend fun registrar(p: Producto): Producto {
        registrarLlamado = true
        return p
    }

    override suspend fun listar(): List<Producto> = listOf(
        Producto(1L, "Paracetamol", 15.50, 100),
        Producto(2L, "Ibuprofeno", 18.90, 50),
        Producto(3L, "Amoxicilina", 25.00, 5)
    )
}

// Repositorio falso: lanza excepción
class RepositorioFalla : ProductoRepository {
    override suspend fun registrar(p: Producto): Producto =
        throw IllegalStateException("Sin conexión con el servidor de la botica")

    override suspend fun listar(): List<Producto> =
        throw IllegalStateException("Sin conexión con el servidor de la botica")
}