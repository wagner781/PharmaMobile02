package pe.edu.upeu.pharmamobile.data.firebase

import android.content.Context
import android.util.Log
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.apps
import dev.gitlive.firebase.crashlytics.FirebaseCrashlytics
import dev.gitlive.firebase.crashlytics.crashlytics
import pe.edu.upeu.pharmamobile.domain.port.EventoTelemetria
import pe.edu.upeu.pharmamobile.domain.port.FalloTelemetria
import pe.edu.upeu.pharmamobile.domain.port.OperacionTelemetria
import pe.edu.upeu.pharmamobile.domain.port.Telemetria

class TelemetriaFirebase(private val contexto: Context) : Telemetria {
    override fun registrarError(operacion: OperacionTelemetria, fallo: FalloTelemetria) {
        publicar {
            recordException(
                FalloTecnico(fallo),
                mapOf("operacion" to operacion.name, "tipo_fallo" to fallo.name)
            )
        }
    }

    override fun registrarEvento(evento: EventoTelemetria) {
        publicar { log(evento.name) }
    }

    private fun publicar(accion: FirebaseCrashlytics.() -> Unit) {
        try {
            val app = Firebase.apps(contexto).firstOrNull { it.name == "[DEFAULT]" } ?: return
            Firebase.crashlytics(app).accion()
        } catch (_: Exception) {
            Log.w("Telemetria", "Crashlytics no está disponible")
        }
    }

    // El informe contiene categorías técnicas fijas y omite la excepción original.
    private class FalloTecnico(fallo: FalloTelemetria) : RuntimeException(fallo.name)
}
