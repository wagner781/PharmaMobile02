package pe.edu.upeu.pharmamobile.domain.error

sealed class ErrorApi : Exception() {
    data class Validacion(val porCampo: Map<String, String>) : ErrorApi()
    data object NoEncontrado : ErrorApi()
    data class Conflicto(val mensaje: String) : ErrorApi()
    data object Servidor : ErrorApi()
    data object SinConexion : ErrorApi()
    data object TiempoAgotado : ErrorApi()
}

class ErrorApiException(val error: ErrorApi) : Exception(error.message)
