package pe.edu.upeu.pharmamobile.data.remote.dto

import kotlinx.serialization.Serializable
import pe.edu.upeu.pharmamobile.domain.model.Sesion

@Serializable
data class LoginRequestDto(
    val username: String,
    val password: String
)

@Serializable
data class LoginResponseDto(
    val token: String,
    val tipo: String,
    val expiraEnSegundos: Long,
    val username: String,
    val nombreCompleto: String,
    val roles: List<String>,
    val permisos: List<String>
) {
    fun aSesion(): Sesion = Sesion(
        token = token,
        usuario = username,
        nombreCompleto = nombreCompleto,
        permisos = permisos.toSet()
    )
}

@Serializable
data class UsuarioResponseDto(
    val id: Long? = null,
    val username: String,
    val empleadoId: Long? = null,
    val empleadoNombre: String? = null,
    val cargo: String? = null,
    val roles: List<String> = emptyList(),
    val permisos: List<String> = emptyList()
)
