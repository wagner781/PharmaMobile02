package pe.edu.upeu.pharmamobile.domain.port

interface Telemetria {
    fun registrarError(operacion: OperacionTelemetria, fallo: FalloTelemetria)
    fun registrarEvento(evento: EventoTelemetria)
}

enum class OperacionTelemetria { SINCRONIZAR_PRODUCTOS }
enum class FalloTelemetria { SERVIDOR, TIEMPO_AGOTADO, INESPERADO }
enum class EventoTelemetria { SINCRONIZACION_PRODUCTOS_COMPLETADA }

object TelemetriaDesactivada : Telemetria {
    override fun registrarError(operacion: OperacionTelemetria, fallo: FalloTelemetria) = Unit
    override fun registrarEvento(evento: EventoTelemetria) = Unit
}
