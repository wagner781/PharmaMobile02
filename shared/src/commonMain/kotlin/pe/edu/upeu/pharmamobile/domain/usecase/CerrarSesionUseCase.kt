package pe.edu.upeu.pharmamobile.domain.usecase

import pe.edu.upeu.pharmamobile.domain.repository.AuthRepository

class CerrarSesionUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Result<Unit> = runCatching {
        authRepository.cerrarSesion()
    }
}
