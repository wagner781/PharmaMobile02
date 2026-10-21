package pe.edu.upeu.pharmamobile.data.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import pe.edu.upeu.pharmamobil.db.PharmaDb
import pe.edu.upeu.pharmamobile.domain.model.Producto

class ProductoLocal(base: PharmaDb) {
    private val consultas = base.productoQueries

    fun observar(): Flow<List<Producto>> =
        consultas.listarTodos()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { filas -> filas.map { it.aDominio() } }

    fun pendientes(): List<Producto> =
        consultas.listarPendientes().executeAsList()
            .map { it.aDominio() }

    fun guardarPendiente(producto: Producto): Producto {
        val menor = consultas.siguienteIdLocal().executeAsOne()
        val local = producto.copy(id = minOf(menor, 0L) - 1L)
        consultas.insertar(
            local.id, local.nombre, local.precio,
            local.stock.toLong(), 1L
        )
        return local
    }

    fun reemplazarSincronizados(productos: List<Producto>) {
        consultas.transaction {
            consultas.borrarSincronizados()
            productos.forEach {
                consultas.insertar(
                    it.id, it.nombre, it.precio,
                    it.stock.toLong(), 0L
                )
            }
        }
    }

    fun borrar(id: Long) {
        consultas.borrarPorId(id)
    }
}

private fun pe.edu.upeu.pharmamobil.db.Producto.aDominio(): Producto = Producto(
    id = id,
    nombre = nombre,
    precio = precio,
    stock = stock.toInt()
)
