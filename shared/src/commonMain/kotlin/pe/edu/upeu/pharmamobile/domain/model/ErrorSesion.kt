package pe.edu.upeu.pharmamobile.domain.model

sealed interface ErrorSesion {
    data object CredencialesInvalidas : ErrorSesion
    data object SesionExpirada : ErrorSesion
    data object SinPermiso : ErrorSesion
    data object SinConexion : ErrorSesion
}

class SesionException(val error: ErrorSesion) : Exception()
