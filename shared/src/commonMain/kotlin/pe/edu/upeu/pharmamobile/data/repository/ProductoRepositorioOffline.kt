package pe.edu.upeu.pharmamobile.data.repository

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import pe.edu.upeu.pharmamobile.data.local.ProductoLocal
import pe.edu.upeu.pharmamobile.data.local.aDominio
import pe.edu.upeu.pharmamobile.data.remote.ProductoApi
import pe.edu.upeu.pharmamobile.data.remote.ejecutarLlamada
import pe.edu.upeu.pharmamobile.domain.error.ErrorApi
import pe.edu.upeu.pharmamobile.domain.error.ErrorApiException
import pe.edu.upeu.pharmamobile.domain.model.ActividadSincronizacion
import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.domain.repository.ProductoRepository
import pe.edu.upeu.pharmamobile.platform.tiempoActualMillis

class ProductoRepositorioOffline(
    private val local: ProductoLocal,
    private val remoto: ProductoApi
) : ProductoRepository {
    private val mutex = Mutex()
    private val idEnEnvio = MutableStateFlow<Long?>(null)
    private val actividad = MutableStateFlow<ActividadSincronizacion>(ActividadSincronizacion.SinComprobar)

    override fun observar(): Flow<List<Producto>> = local.observar()
    override fun observarEstadoEnvio() = local.observarEstadoEnvio(MAX_INTENTOS, idEnEnvio)
    override fun observarActividadSincronizacion() = actividad.asStateFlow()

    override suspend fun registrar(producto: Producto): Producto {
        val guardado = mutex.withLock { local.guardarPendiente(producto) }
        intentar { sincronizar() }
        return guardado
    }

    override suspend fun sincronizar() = mutex.withLock {
        val anterior = actividad.value
        actividad.value = ActividadSincronizacion.EnCurso
        try {
            subirPendientes()
            val productos = ejecutarLlamada { remoto.listar() }.getOrThrow()
            local.reemplazarSincronizados(productos)
            local.guardarUltimaSincronizacion(tiempoActualMillis())
            actividad.value = ActividadSincronizacion.Completada
        } catch (cancelacion: CancellationException) {
            actividad.value = anterior
            throw cancelacion
        } catch (fallo: Exception) {
            actividad.value = ActividadSincronizacion.Fallida(fallo)
            throw fallo
        }
    }

    override fun obtenerUltimaSincronizacion(): Long? = local.obtenerUltimaSincronizacion()
    override fun guardarUltimaSincronizacion(timestamp: Long) = local.guardarUltimaSincronizacion(timestamp)

    override suspend fun eliminar(id: Long) = mutex.withLock {
        local.borrar(id)
        if (id > 0) {
            intentar { remoto.eliminar(id) }
        }
    }

    override suspend fun limpiarTodoLocal() = mutex.withLock {
        local.limpiarTodo()
    }

    private suspend fun subirPendientes() {
        var esperaAplicada = 0L
        for (fila in local.pendientesPorEnviar(MAX_INTENTOS).sortedBy { it.intentos }) {
            if (fila.intentos > 0) {
                val espera = 1_000L shl (fila.intentos - 1).toInt()
                delay(espera - esperaAplicada)
                esperaAplicada = espera
            }
            val comprobacion = evaluarFallo { remoto.comprobarEnvio() }
            if (comprobacion != null) {
                val permanente = motivoPermanente(comprobacion)
                if (permanente != null) {
                    local.marcarFallida(fila.id, MAX_INTENTOS, permanente)
                } else {
                    local.sumarIntento(fila.id, motivoTransitorio(comprobacion))
                }
                continue
            }

            idEnEnvio.value = fila.id
            try {
                // Un cierre durante el POST también deja el alta pendiente de revisión.
                local.marcarFallida(fila.id, MAX_INTENTOS, ENVIO_INCIERTO)
                val producto = try {
                    ejecutarLlamada { remoto.registrar(fila.aDominio()) }.getOrThrow()
                } catch (cancelacion: CancellationException) {
                    throw cancelacion
                } catch (fallo: Exception) {
                    val error = errorApi(fallo)
                    if (error != null) {
                        val motivo = motivoPermanente(error) ?: "${motivoTransitorio(error)}. $ENVIO_INCIERTO"
                        local.marcarFallida(fila.id, MAX_INTENTOS, motivo)
                    }
                    continue
                }
                local.confirmarEnvio(fila.id, producto)
            } finally {
                idEnEnvio.value = null
            }
        }
    }

    private suspend fun evaluarFallo(bloque: suspend () -> Unit): ErrorApi? = try {
        ejecutarLlamada(bloque).getOrThrow()
        null
    } catch (fallo: Exception) {
        errorApi(fallo) ?: throw fallo
    }

    private fun errorApi(fallo: Exception): ErrorApi? =
        (fallo as? ErrorApiException)?.error ?: fallo as? ErrorApi

    private fun motivoPermanente(error: ErrorApi): String? = when (error) {
        is ErrorApi.Validacion -> error.porCampo.values.joinToString(". ")
            .ifBlank { "El servidor rechazó los datos del producto" }
        is ErrorApi.Conflicto -> error.mensaje
        is ErrorApi.SolicitudRechazada -> error.motivo
        ErrorApi.NoEncontrado -> "Recurso no encontrado"
        ErrorApi.NoAutorizado -> "Sesión vencida. Inicia sesión nuevamente"
        ErrorApi.SinPrivilegio -> "No tienes permiso para enviar el producto"
        ErrorApi.SinConexion, ErrorApi.TiempoAgotado, ErrorApi.Servidor -> null
    }

    private fun motivoTransitorio(error: ErrorApi): String = when (error) {
        ErrorApi.SinConexion -> "Sin conexión al servidor"
        ErrorApi.TiempoAgotado -> "Tiempo de espera agotado"
        else -> "Servicio temporalmente no disponible"
    }

    private suspend fun intentar(bloque: suspend () -> Unit) {
        try {
            bloque()
        } catch (cancelacion: CancellationException) {
            throw cancelacion
        } catch (_: Exception) {
            // La operación local ya se guardó.
        }
    }

    private companion object {
        const val MAX_INTENTOS = 5L
        const val ENVIO_INCIERTO = "Envío sin confirmar. Verifica el producto en el servidor"
    }
}
