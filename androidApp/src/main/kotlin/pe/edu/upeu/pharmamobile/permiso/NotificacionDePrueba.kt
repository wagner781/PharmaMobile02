package pe.edu.upeu.pharmamobile.permiso

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat

private const val CANAL_PRUEBA = "notificaciones_prueba"

fun Context.enviarNotificacionDePrueba() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
    ) return

    val gestor = getSystemService(NotificationManager::class.java)
    if (!gestor.areNotificationsEnabled()) return

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        gestor.createNotificationChannel(
            NotificationChannel(CANAL_PRUEBA, "Notificaciones de prueba", NotificationManager.IMPORTANCE_DEFAULT)
        )
    }

    val notificacion = NotificationCompat.Builder(this, CANAL_PRUEBA)
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setContentTitle("Notificación de prueba")
        .setContentText("Las notificaciones de PharmaMobile están activadas.")
        .build()
    gestor.notify(1, notificacion)
}
