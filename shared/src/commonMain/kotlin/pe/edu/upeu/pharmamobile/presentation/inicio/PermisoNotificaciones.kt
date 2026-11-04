package pe.edu.upeu.pharmamobile.presentation.inicio

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import kotlinx.coroutines.launch
import pe.edu.upeu.pharmamobile.domain.permiso.EstadoPermiso
import pe.edu.upeu.pharmamobile.domain.permiso.GestorPermisos
import pe.edu.upeu.pharmamobile.domain.permiso.Permiso

@Composable
fun PermisoNotificaciones(gestor: GestorPermisos, enviarPrueba: () -> Unit) {
    val scope = rememberCoroutineScope()
    var estado by remember(gestor) { mutableStateOf<EstadoPermiso?>(null) }
    var solicitando by remember(gestor) { mutableStateOf(false) }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        scope.launch { estado = gestor.estado(Permiso.NOTIFICACIONES) }
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Notificaciones", style = MaterialTheme.typography.titleMedium)
            when (estado) {
                null -> Text("Comprobando permiso…")
                EstadoPermiso.Concedido -> {
                    Text("Permiso concedido.")
                    Button(onClick = enviarPrueba) { Text("Enviar notificación de prueba") }
                }
                EstadoPermiso.Denegado -> {
                    Text("Permiso denegado. Actívalo si quieres recibir avisos de PharmaMobile en este dispositivo.")
                    Button(
                        enabled = !solicitando,
                        onClick = {
                            solicitando = true
                            scope.launch {
                                try {
                                    estado = gestor.solicitar(Permiso.NOTIFICACIONES)
                                } finally {
                                    solicitando = false
                                }
                            }
                        }
                    ) { Text("Permitir notificaciones") }
                }
                EstadoPermiso.DenegadoParaSiempre -> {
                    Text("Permiso denegado permanentemente. Puedes activar las notificaciones en Ajustes.")
                    Button(onClick = gestor::abrirAjustes) { Text("Abrir Ajustes") }
                }
            }
        }
    }
}
