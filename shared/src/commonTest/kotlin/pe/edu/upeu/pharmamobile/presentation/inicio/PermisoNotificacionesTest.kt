package pe.edu.upeu.pharmamobile.presentation.inicio

import kotlinx.coroutines.test.runTest
import pe.edu.upeu.pharmamobile.domain.permiso.EstadoPermiso
import pe.edu.upeu.pharmamobile.domain.permiso.GestorPermisos
import pe.edu.upeu.pharmamobile.domain.permiso.Permiso
import kotlin.test.Test
import kotlin.test.assertEquals

class PermisoNotificacionesTest {
    @Test
    fun denegacionReintentoAjustesYEnvioConPermiso() = runTest {
        val gestor = GestorFalso()
        var envios = 0
        val enviar = { envios += 1 }

        assertEquals(EstadoPermiso.Denegado, actuarSobreNotificaciones(gestor, EstadoPermiso.Denegado, enviar))
        gestor.respuesta = EstadoPermiso.DenegadoParaSiempre
        assertEquals(
            EstadoPermiso.DenegadoParaSiempre,
            actuarSobreNotificaciones(gestor, EstadoPermiso.Denegado, enviar)
        )
        assertEquals(2, gestor.solicitudes)
        assertEquals(0, envios)

        actuarSobreNotificaciones(gestor, EstadoPermiso.DenegadoParaSiempre, enviar)
        assertEquals(1, gestor.aperturasDeAjustes)
        assertEquals(2, gestor.solicitudes)

        gestor.actual = EstadoPermiso.Concedido
        actuarSobreNotificaciones(gestor, EstadoPermiso.Concedido, enviar)
        assertEquals(1, envios)
    }

    @Test
    fun permisoRevocadoActualizaElEstadoAntesDeEnviar() = runTest {
        val gestor = GestorFalso().apply { actual = EstadoPermiso.DenegadoParaSiempre }

        val estado = actuarSobreNotificaciones(gestor, EstadoPermiso.Concedido) {
            error("Se intentó enviar sin permiso")
        }

        assertEquals(EstadoPermiso.DenegadoParaSiempre, estado)
        assertEquals(0, gestor.aperturasDeAjustes)
        assertEquals(0, gestor.solicitudes)
    }

    private class GestorFalso : GestorPermisos {
        var actual: EstadoPermiso = EstadoPermiso.Denegado
        var respuesta: EstadoPermiso = EstadoPermiso.Denegado
        var solicitudes = 0
        var aperturasDeAjustes = 0

        override suspend fun estado(permiso: Permiso): EstadoPermiso = actual

        override suspend fun solicitar(permiso: Permiso): EstadoPermiso {
            solicitudes++
            actual = respuesta
            return actual
        }

        override fun abrirAjustes() {
            aperturasDeAjustes++
        }
    }
}
