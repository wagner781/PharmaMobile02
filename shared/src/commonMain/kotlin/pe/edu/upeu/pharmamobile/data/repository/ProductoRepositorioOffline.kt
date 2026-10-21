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
    }

    private suspend fun subirPendientes() {
        local.pendientes().forEach { pendiente ->
            remoto.registrar(pendiente)
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
