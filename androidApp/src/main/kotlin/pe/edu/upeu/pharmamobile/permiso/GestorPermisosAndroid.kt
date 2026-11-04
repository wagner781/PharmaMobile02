package pe.edu.upeu.pharmamobile.permiso

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CompletableDeferred
import pe.edu.upeu.pharmamobile.domain.permiso.EstadoPermiso
import pe.edu.upeu.pharmamobile.domain.permiso.GestorPermisos
import pe.edu.upeu.pharmamobile.domain.permiso.Permiso

class GestorPermisosAndroid(private val actividad: ComponentActivity) : GestorPermisos {
    private val preferencias = actividad.getSharedPreferences("permisos", Context.MODE_PRIVATE)
    private var solicitud: CompletableDeferred<EstadoPermiso>? = null

    private val lanzador = actividad.registerForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Cerrar el diálogo sin elegir no cuenta como una denegación.
            if (concedido || actividad.shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                preferencias.edit().putBoolean(RESPUESTA_REGISTRADA, true).apply()
            }
        }
        val pendiente = solicitud
        solicitud = null
        pendiente?.complete(estadoNotificaciones())
    }

    override suspend fun estado(permiso: Permiso): EstadoPermiso = when (permiso) {
        Permiso.NOTIFICACIONES -> estadoNotificaciones()
    }

    override suspend fun solicitar(permiso: Permiso): EstadoPermiso {
        val actual = estado(permiso)
        if (actual != EstadoPermiso.Denegado || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return actual
        }
        solicitud?.let { return it.await() }

        val pendiente = CompletableDeferred<EstadoPermiso>()
        solicitud = pendiente
        try {
            lanzador.launch(Manifest.permission.POST_NOTIFICATIONS)
        } catch (fallo: Exception) {
            solicitud = null
            throw fallo
        }
        return pendiente.await()
    }

    private fun estadoNotificaciones(): EstadoPermiso {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permiso = Manifest.permission.POST_NOTIFICATIONS
            if (ContextCompat.checkSelfPermission(actividad, permiso) != PackageManager.PERMISSION_GRANTED) {
                val sePuedePedir = actividad.shouldShowRequestPermissionRationale(permiso) ||
                    !preferencias.getBoolean(RESPUESTA_REGISTRADA, false)
                return if (sePuedePedir) EstadoPermiso.Denegado else EstadoPermiso.DenegadoParaSiempre
            }
        }
        return if (NotificationManagerCompat.from(actividad).areNotificationsEnabled()) {
            EstadoPermiso.Concedido
        } else {
            EstadoPermiso.DenegadoParaSiempre
        }
    }

    override fun abrirAjustes() {
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                .putExtra(Settings.EXTRA_APP_PACKAGE, actividad.packageName)
        } else {
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", actividad.packageName, null))
        }
        actividad.startActivity(intent)
    }

    private companion object {
        const val RESPUESTA_REGISTRADA = "respuesta_notificaciones"
    }
}
