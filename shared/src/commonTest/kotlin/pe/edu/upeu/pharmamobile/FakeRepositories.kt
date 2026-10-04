package pe.edu.upeu.pharmamobile

import pe.edu.upeu.pharmamobile.domain.error.ErrorApi
import pe.edu.upeu.pharmamobile.domain.error.ErrorApiException
import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.domain.repository.ProductoRepository

class FakeProductoRepository(
    initialProductos: List<Producto> = emptyList(),
    var fallarConError: ErrorApi? = null
) : ProductoRepository {

    private val db = initialProductos.toMutableList()
    private var nextId = (db.maxOfOrNull { it.id } ?: 0L) + 1L

    override suspend fun listar(): List<Producto> {
        kotlinx.coroutines.delay(10)
        checkError()
        return db.filter { it.estado }.toList()
    }

    override suspend fun obtener(id: Long): Producto {
        checkError()
        return db.find { it.id == id && it.estado } 
            ?: throw ErrorApiException(ErrorApi.NoEncontrado)
    }

    override suspend fun registrar(producto: Producto): Producto {
        checkError()
        val nuevo = producto.copy(id = nextId++)
        db.add(nuevo)
        return nuevo
    }

    override suspend fun actualizar(producto: Producto): Producto {
        checkError()
        val index = db.indexOfFirst { it.id == producto.id }
        if (index == -1) throw ErrorApiException(ErrorApi.NoEncontrado)
        db[index] = producto
        return producto
    }

    override suspend fun eliminar(id: Long) {
        kotlinx.coroutines.delay(10)
        checkError()
        val index = db.indexOfFirst { it.id == id && it.estado }
        if (index == -1) throw ErrorApiException(ErrorApi.NoEncontrado)
        val p = db[index]
        db[index] = p.copy(estado = false)
    }

    private fun checkError() {
        fallarConError?.let { throw ErrorApiException(it) }
    }
}