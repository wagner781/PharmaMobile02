package pe.edu.upeu.pharmamobile.domain.usecase

import pe.edu.upeu.pharmamobile.domain.model.Credenciales
import pe.edu.upeu.pharmamobile.domain.model.Sesion
import pe.edu.upeu.pharmamobile.domain.repository.AuthRepository

class IniciarSesionUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(credenciales: Credenciales): Result<Sesion> = runCatching {
        authRepository.iniciarSesion(credenciales)
    }
}
