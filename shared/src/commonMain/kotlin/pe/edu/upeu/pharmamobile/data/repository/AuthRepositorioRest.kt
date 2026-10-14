package pe.edu.upeu.pharmamobile.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.auth.authProvider
import io.ktor.client.plugins.auth.providers.BearerAuthProvider
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CancellationException
import pe.edu.upeu.pharmamobile.data.local.AlmacenSeguro
import pe.edu.upeu.pharmamobile.data.remote.dto.ErrorResponseDto
import pe.edu.upeu.pharmamobile.data.remote.dto.LoginRequestDto
import pe.edu.upeu.pharmamobile.data.remote.dto.LoginResponseDto
import pe.edu.upeu.pharmamobile.data.remote.dto.UsuarioResponseDto
import pe.edu.upeu.pharmamobile.domain.model.Credenciales
import pe.edu.upeu.pharmamobile.domain.model.ErrorSesion
import pe.edu.upeu.pharmamobile.domain.model.Sesion
import pe.edu.upeu.pharmamobile.domain.model.SesionException
import pe.edu.upeu.pharmamobile.domain.repository.AuthRepository

fun mapearErrorSesion(estado: HttpStatusCode, mensaje: String?): ErrorSesion =
    when (estado) {
        HttpStatusCode.Unauthorized ->
            if (mensaje?.contains("expirado", ignoreCase = true) == true)
                ErrorSesion.SesionExpirada
            else ErrorSesion.CredencialesInvalidas
        HttpStatusCode.Forbidden -> ErrorSesion.SinPermiso
        else -> ErrorSesion.SinConexion
    }

class AuthRepositorioRest(
    private val cliente: HttpClient,
    private val almacen: AlmacenSeguro
) : AuthRepository {

    override suspend fun iniciarSesion(credenciales: Credenciales): Sesion {
        try {
            val dto: LoginResponseDto = cliente.post("/api/v1/auth/login") {
                setBody(LoginRequestDto(credenciales.usuario, credenciales.clave))
            }.body()

            almacen.guardarToken(dto.token)
            cliente.authProvider<BearerAuthProvider>()?.clearToken()

            return dto.aSesion()
        } catch (e: ClientRequestException) {
            val mensaje = runCatching { e.response.body<ErrorResponseDto>().message }.getOrNull()
            throw SesionException(mapearErrorSesion(e.response.status, mensaje))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw SesionException(ErrorSesion.SinConexion)
        }
    }

    override suspend fun sesionGuardada(): Sesion? {
        val token = almacen.leerToken() ?: return null
        return try {
            val usuarioDto: UsuarioResponseDto = cliente.get("/api/v1/auth/me").body()
            Sesion(
                token = token,
                usuario = usuarioDto.username,
                nombreCompleto = usuarioDto.empleadoNombre ?: usuarioDto.username,
                permisos = usuarioDto.permisos.toSet()
            )
        } catch (e: ClientRequestException) {
            if (e.response.status == HttpStatusCode.Unauthorized) {
                cerrarSesion()
            }
            null
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Sesion(
                token = token,
                usuario = "",
                nombreCompleto = "",
                permisos = emptySet()
            )
        }
    }

    override suspend fun cerrarSesion() {
        almacen.borrarToken()
        cliente.authProvider<BearerAuthProvider>()?.clearToken()
    }
}
