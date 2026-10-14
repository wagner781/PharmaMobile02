package pe.edu.upeu.pharmamobile.domain.usecase

import pe.edu.upeu.pharmamobile.domain.model.Sesion
import pe.edu.upeu.pharmamobile.domain.repository.AuthRepository

class ObtenerSesionGuardadaUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Sesion? = authRepository.sesionGuardada()
}
