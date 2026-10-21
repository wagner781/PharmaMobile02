package pe.edu.upeu.pharmamobile.domain.repository

import kotlinx.coroutines.flow.Flow
import pe.edu.upeu.pharmamobile.domain.model.Producto

interface ProductoRepository {
    /** La pantalla se suscribe una vez y se actualiza sola. */
    fun observar(): Flow<List<Producto>>

    /** Guarda en local; intenta enviar si hay conexión. */
    suspend fun registrar(producto: Producto): Producto

    /** Sube lo pendiente y luego trae la lista del servidor. */
    suspend fun sincronizar()

    suspend fun listar(): List<Producto> = emptyList()
    suspend fun obtener(id: Long): Producto = throw UnsupportedOperationException()
    suspend fun actualizar(producto: Producto): Producto = throw UnsupportedOperationException()
    suspend fun eliminar(id: Long) {}
}