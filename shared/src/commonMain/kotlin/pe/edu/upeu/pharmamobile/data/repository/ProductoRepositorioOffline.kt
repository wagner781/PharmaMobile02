package pe.edu.upeu.pharmamobile.data.repository

import kotlinx.coroutines.flow.Flow
import pe.edu.upeu.pharmamobile.data.local.ProductoLocal
import pe.edu.upeu.pharmamobile.data.remote.ProductoApi
import pe.edu.upeu.pharmamobile.domain.error.ErrorApi
import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.domain.repository.ProductoRepository

class ProductoRepositorioOffline(
    private val local: ProductoLocal,
    private val remoto: ProductoApi
) : ProductoRepository {

    override fun observar(): Flow<List<Producto>> = local.observar()

    override suspend fun registrar(producto: Producto): Producto {
        val guardado = local.guardarPendiente(producto)
        intentar { sincronizar() }
        return guardado
    }

    override suspend fun sincronizar() {
        subirPendientes()
        local.reemplazarSincronizados(remoto.listar())
        local.guardarUltimaSincronizacion(pe.edu.upeu.pharmamobile.platform.tiempoActualMillis())
    }

    override fun obtenerUltimaSincronizacion(): Long? = local.obtenerUltimaSincronizacion()
    override fun guardarUltimaSincronizacion(timestamp: Long) = local.guardarUltimaSincronizacion(timestamp)

    override suspend fun eliminar(id: Long) {
        local.borrar(id)
        if (id > 0) {
            intentar { remoto.eliminar(id) }
        }
    }

    override suspend fun limpiarTodoLocal() {
        local.limpiarTodo()
    }

    private suspend fun subirPendientes() {
        local.pendientes().forEach { pendiente ->
            try {
                remoto.registrar(pendiente)
                local.borrar(pendiente.id)
            } catch (e: Exception) {
                if (e is io.ktor.client.plugins.ClientRequestException && e.response.status.value == 409) {
                    local.borrar(pendiente.id)
                } else {
                    throw e
                }
            }
        }
    }

    /** Un fallo de red no debe tumbar la operación local. */
    private suspend fun intentar(bloque: suspend () -> Unit) {
        try {
            bloque()
        } catch (sinRed: ErrorApi.SinConexion) {
            // Queda pendiente; se reintentará al sincronizar.
        } catch (e: Exception) {
            // Un fallo de red no debe tumbar la operación local
        }
    }
}
