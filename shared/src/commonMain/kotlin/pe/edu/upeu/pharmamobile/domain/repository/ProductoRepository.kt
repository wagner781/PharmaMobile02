package pe.edu.upeu.pharmamobile.domain.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import pe.edu.upeu.pharmamobile.domain.model.ActividadSincronizacion
import pe.edu.upeu.pharmamobile.domain.model.EstadoEnvioProductos
import pe.edu.upeu.pharmamobile.domain.model.Producto

interface ProductoRepository {
    /** La pantalla se suscribe una vez y se actualiza sola. */
    fun observar(): Flow<List<Producto>>

    /** Guarda en local; intenta enviar si hay conexión. */
    suspend fun registrar(producto: Producto): Producto

    /** Sube lo pendiente y luego trae la lista del servidor. */
    suspend fun sincronizar()

    fun observarEstadoEnvio(): Flow<EstadoEnvioProductos> = flowOf(EstadoEnvioProductos())
    fun observarActividadSincronizacion(): Flow<ActividadSincronizacion> =
        flowOf(ActividadSincronizacion.SinComprobar)

    fun obtenerUltimaSincronizacion(): Long? = null
    fun guardarUltimaSincronizacion(timestamp: Long) {}

    suspend fun listar(): List<Producto> = emptyList()
    suspend fun obtener(id: Long): Producto = throw UnsupportedOperationException()
    suspend fun actualizar(producto: Producto): Producto = throw UnsupportedOperationException()
    suspend fun eliminar(id: Long) {}
    suspend fun limpiarTodoLocal() {}
}
