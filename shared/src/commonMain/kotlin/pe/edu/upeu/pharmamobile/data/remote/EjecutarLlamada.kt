package pe.edu.upeu.pharmamobile.data.remote

import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.call.body
import io.ktor.utils.io.errors.IOException
import kotlinx.coroutines.CancellationException
import pe.edu.upeu.pharmamobile.domain.error.ErrorApi
import pe.edu.upeu.pharmamobile.domain.error.ErrorApiException
import pe.edu.upeu.pharmamobile.data.remote.dto.ErrorResponseDto

suspend fun <T> ejecutarLlamada(bloque: suspend () -> T): Result<T> =
    try {
        Result.success(bloque())
    } catch (cancelacion: CancellationException) {
        throw cancelacion
    } catch (e: ClientRequestException) {
        Result.failure(ErrorApiException(traducirCliente(e)))
    } catch (e: ServerResponseException) {
        Result.failure(ErrorApiException(ErrorApi.Servidor))
    } catch (e: HttpRequestTimeoutException) {
        Result.failure(ErrorApiException(ErrorApi.TiempoAgotado))
    } catch (e: IOException) {
        Result.failure(ErrorApiException(ErrorApi.SinConexion))
    }

private suspend fun traducirCliente(e: ClientRequestException): ErrorApi {
    val cuerpo = try {
        e.response.body<ErrorResponseDto>()
    } catch (cancelacion: CancellationException) {
        throw cancelacion
    } catch (_: Exception) {
        null
    }
    return when (e.response.status.value) {
        400, 422 -> ErrorApi.Validacion(cuerpo?.validationErrors.orEmpty())
        401 -> ErrorApi.NoAutorizado
        403 -> ErrorApi.SinPrivilegio
        404 -> ErrorApi.NoEncontrado
        408 -> ErrorApi.TiempoAgotado
        409 -> ErrorApi.Conflicto(cuerpo?.message ?: "Operación no permitida")
        429 -> ErrorApi.Servidor
        else -> ErrorApi.SolicitudRechazada(cuerpo?.message ?: "Solicitud rechazada")
    }
}
