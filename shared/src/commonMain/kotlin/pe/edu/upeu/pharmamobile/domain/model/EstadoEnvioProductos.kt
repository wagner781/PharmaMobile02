package pe.edu.upeu.pharmamobile.domain.model

data class EstadoEnvioProductos(
    val pendientes: Int = 0,
    val fallidas: List<EnvioProductoFallido> = emptyList(),
    val ultimaSincronizacion: Long? = null
)

data class EnvioProductoFallido(val id: Long, val nombre: String, val motivo: String)

sealed interface ActividadSincronizacion {
    data object SinComprobar : ActividadSincronizacion
    data object EnCurso : ActividadSincronizacion
    data object Completada : ActividadSincronizacion
    data class Fallida(val causa: Exception) : ActividadSincronizacion
}
