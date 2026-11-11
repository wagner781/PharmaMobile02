package pe.edu.upeu.pharmamobile.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.headersOf
import io.ktor.http.content.OutgoingContent
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import pe.edu.upeu.pharmamobil.db.PharmaDb
import pe.edu.upeu.pharmamobile.data.local.ProductoLocal
import pe.edu.upeu.pharmamobile.data.remote.ProductoApi
import pe.edu.upeu.pharmamobile.domain.error.ErrorApi
import pe.edu.upeu.pharmamobile.domain.error.ErrorApiException
import pe.edu.upeu.pharmamobile.domain.model.EstadoEnvioProductos
import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.domain.port.EventoTelemetria
import pe.edu.upeu.pharmamobile.domain.port.FalloTelemetria
import pe.edu.upeu.pharmamobile.domain.port.OperacionTelemetria
import pe.edu.upeu.pharmamobile.domain.port.Telemetria
import pe.edu.upeu.pharmamobile.domain.port.TelemetriaDesactivada
import pe.edu.upeu.pharmamobile.domain.usecase.ActualizarProductoUseCase
import pe.edu.upeu.pharmamobile.domain.usecase.EliminarProductoUseCase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SincronizacionProductosTest {
    @Test
    fun falloAntesDelEnvioSumaIntentosConEsperaCrecienteHastaElLimite() = runTest {
        val comprobaciones = mutableListOf<Long>()
        val motor = MockEngine { solicitud ->
            assertEquals(HttpMethod.Get, solicitud.method)
            if (solicitud.url.parameters["tamanio"] == "1") {
                comprobaciones += currentTime
                respond("", HttpStatusCode.ServiceUnavailable)
            } else {
                respond(PAGINA_VACIA, headers = JSON)
            }
        }
        conRepositorio(motor) { base, local, repo ->
            local.guardarPendiente(Producto(0, "Producto de prueba", 2.5, 7))

            repo.sincronizar()
            assertEquals(1L, base.productoQueries.listarPendientes().executeAsOne().intentos)
            assertEquals(0L, base.productoQueries.contarFallidas(5).executeAsOne())

            repeat(5) { repo.sincronizar() }

            assertEquals(listOf(0L, 1_000L, 3_000L, 7_000L, 15_000L), comprobaciones)
            assertEquals(0L, base.productoQueries.contarPendientes(5).executeAsOne())
            assertEquals(1L, base.productoQueries.contarFallidas(5).executeAsOne())
            assertEquals("Servicio temporalmente no disponible",
                base.productoQueries.listarPendientes().executeAsOne().ultimoError)
        }
    }

    @Test
    fun unConflictoConservaLaFilaYPermiteSubirLasDemasAntesDeDescargar() = runTest {
        val peticiones = mutableListOf<String>()
        var altas = 0
        val motor = MockEngine { solicitud ->
            when {
                solicitud.method == HttpMethod.Post -> {
                    peticiones += "alta"
                    if (++altas == 1) {
                        respond("""{"message":"Nombre duplicado"}""", HttpStatusCode.Conflict, JSON)
                    } else {
                        respond(PRODUCTO_REMOTO, HttpStatusCode.Created, JSON)
                    }
                }
                solicitud.url.parameters["tamanio"] == "1" -> {
                    peticiones += "comprobación"
                    respond(PAGINA_VACIA, headers = JSON)
                }
                else -> {
                    peticiones += "descarga"
                    respond(PAGINA_VACIA.replace("\"contenido\":[]", "\"contenido\":[$PRODUCTO_REMOTO]"), headers = JSON)
                }
            }
        }
        conRepositorio(motor) { base, local, repo ->
            local.guardarPendiente(Producto(0, "Confirmado", 2.5, 7))
            val conflicto = local.guardarPendiente(Producto(0, "Duplicado", 2.5, 7))

            repo.sincronizar()
            repo.sincronizar()

            assertEquals(listOf("comprobación", "alta", "comprobación", "alta", "descarga", "descarga"), peticiones)
            val fallida = base.productoQueries.listarPendientes().executeAsOne()
            assertEquals(conflicto.id, fallida.id)
            assertEquals("Nombre duplicado", fallida.ultimoError)
            assertEquals(5L, fallida.intentos)
            assertTrue(base.productoQueries.listarTodos().executeAsList().any { it.id == 42L })
        }
    }

    @Test
    fun unFalloDeServidorDespuesDelPostRequiereRevisionSinReenvio() = runTest {
        var altas = 0
        val motor = MockEngine { solicitud ->
            if (solicitud.method == HttpMethod.Post) {
                altas++
                respond("", HttpStatusCode.ServiceUnavailable)
            } else {
                respond(PAGINA_VACIA, headers = JSON)
            }
        }
        conRepositorio(motor) { base, local, repo ->
            local.guardarPendiente(Producto(0, "Alta sin confirmar", 2.5, 7))

            repo.sincronizar()
            repo.sincronizar()

            assertEquals(1, altas)
            assertEquals(1L, base.productoQueries.contarFallidas(5).executeAsOne())
            assertTrue(base.productoQueries.listarPendientes().executeAsOne().ultimoError.orEmpty()
                .contains("Envío sin confirmar"))
        }
    }

    @Test
    fun sesionVencidaImpideElAltaYConservaElMotivo() = runTest {
        val motor = MockEngine { solicitud ->
            assertEquals(HttpMethod.Get, solicitud.method)
            respond("", HttpStatusCode.Unauthorized)
        }
        conRepositorio(motor) { base, local, repo ->
            local.guardarPendiente(Producto(0, "Alta con sesión vencida", 2.5, 7))

            val fallo = assertFailsWith<ErrorApiException> { repo.sincronizar() }

            assertEquals(ErrorApi.NoAutorizado, fallo.error)
            assertEquals(1L, base.productoQueries.contarFallidas(5).executeAsOne())
            assertEquals(
                "Sesión vencida. Inicia sesión nuevamente",
                base.productoQueries.listarPendientes().executeAsOne().ultimoError
            )
        }
    }

    @Test
    fun sincronizacionRegistraCategoriasTecnicasYOmiteRechazosEsperados() = runTest {
        val errores = mutableListOf<Pair<OperacionTelemetria, FalloTelemetria>>()
        val eventos = mutableListOf<EventoTelemetria>()
        val telemetria = object : Telemetria {
            override fun registrarError(operacion: OperacionTelemetria, fallo: FalloTelemetria) {
                errores.add(operacion to fallo)
            }

            override fun registrarEvento(evento: EventoTelemetria) {
                eventos.add(evento)
            }
        }
        var estado = HttpStatusCode.Unauthorized
        val motor = MockEngine { respond(PAGINA_VACIA, estado, JSON) }

        conRepositorio(motor, telemetria) { _, _, repo ->
            assertFailsWith<ErrorApiException> { repo.sincronizar() }
            assertTrue(errores.isEmpty())

            estado = HttpStatusCode.ServiceUnavailable
            assertFailsWith<ErrorApiException> { repo.sincronizar() }
            assertEquals(listOf(OperacionTelemetria.SINCRONIZAR_PRODUCTOS to FalloTelemetria.SERVIDOR), errores)

            estado = HttpStatusCode.OK
            repo.sincronizar()
            assertEquals(listOf(EventoTelemetria.SINCRONIZACION_PRODUCTOS_COMPLETADA), eventos)
        }
    }

    @Test
    fun laColaObservableNotificaElPendienteElFalloYLaUltimaDescarga() = runTest {
        val motor = MockEngine { solicitud ->
            if (solicitud.method == HttpMethod.Post) {
                respond("""{"message":"Nombre duplicado"}""", HttpStatusCode.Conflict, JSON)
            } else {
                respond(PAGINA_VACIA, headers = JSON)
            }
        }
        conRepositorio(motor) { _, local, repo ->
            val estados = Channel<EstadoEnvioProductos>(Channel.UNLIMITED)
            val observacion = launch { repo.observarEstadoEnvio().collect { estados.send(it) } }
            try {
                assertEquals(EstadoEnvioProductos(), estados.receive())
                val producto = local.guardarPendiente(Producto(0, "Pendiente", 2.5, 7))
                val pendiente = estados.receive()
                assertEquals(1, pendiente.pendientes)
                assertTrue(pendiente.fallidas.isEmpty())

                repo.sincronizar()

                var resumen = estados.receive()
                while (resumen.ultimaSincronizacion == null || resumen.fallidas.isEmpty()) {
                    resumen = estados.receive()
                }
                assertEquals(0, resumen.pendientes)
                val fallida = resumen.fallidas.single()
                assertEquals(producto.id, fallida.id)
                assertEquals("Nombre duplicado", fallida.motivo)
            } finally {
                observacion.cancelAndJoin()
            }
        }
    }

    @Test
    fun unAltaConfirmadaSobreviveAlFalloDeDescargaSinReenviarse() = runTest {
        var altas = 0
        val motor = MockEngine { solicitud ->
            when {
                solicitud.method == HttpMethod.Post -> {
                    altas++
                    respond(PRODUCTO_REMOTO, HttpStatusCode.Created, JSON)
                }
                solicitud.url.parameters["tamanio"] == "1" -> respond(PAGINA_VACIA, headers = JSON)
                else -> respond("", HttpStatusCode.ServiceUnavailable)
            }
        }
        conRepositorio(motor) { base, local, repo ->
            local.guardarPendiente(Producto(0, "Confirmado", 2.5, 7))

            repeat(2) {
                val fallo = assertFailsWith<ErrorApiException> { repo.sincronizar() }
                assertEquals(ErrorApi.Servidor, fallo.error)
            }

            assertEquals(1, altas)
            assertEquals(42L, base.productoQueries.listarTodos().executeAsOne().id)
            assertTrue(local.pendientes().isEmpty())
            assertEquals(null, repo.obtenerUltimaSincronizacion())
        }
    }

    @Test
    fun editarMantieneLaCategoriaYActualizaLaCopiaTrasConfirmacion() = runTest {
        val motor = MockEngine { solicitud ->
            assertEquals("/api/v1/productos/42", solicitud.url.encodedPath)
            when (solicitud.method) {
                HttpMethod.Get -> respond(
                    """{"id":42,"nombre":"Original","precio":2.5,"stock":7,"categoriaId":9,"estado":true}""",
                    headers = JSON
                )
                HttpMethod.Put -> {
                    val cuerpo = (solicitud.body as OutgoingContent.ByteArrayContent).bytes().decodeToString()
                    val datos = Json.parseToJsonElement(cuerpo).jsonObject
                    assertEquals(9L, datos.getValue("categoriaId").jsonPrimitive.long)
                    assertTrue(datos.getValue("estado").jsonPrimitive.boolean)
                    respond(
                        """{"id":42,"nombre":"Editado","precio":3.5,"stock":8,"categoriaId":9}""",
                        headers = JSON
                    )
                }
                else -> error("Método inesperado")
            }
        }
        conRepositorio(motor) { base, local, repo ->
            local.guardarSincronizado(Producto(42, "Original", 2.5, 7))

            val editado = Producto(42, "Editado", 3.5, 8, estado = false)
            val confirmado = ActualizarProductoUseCase(repo)(editado).getOrThrow()

            assertEquals("Editado", confirmado.nombre)
            assertEquals("Editado", base.productoQueries.listarTodos().executeAsOne().nombre)
        }
    }

    @Test
    fun unaBajaRechazadaConservaLaFilaHastaConfirmacion() = runTest {
        var respuesta = HttpStatusCode.Forbidden
        val motor = MockEngine { solicitud ->
            assertEquals(HttpMethod.Delete, solicitud.method)
            assertEquals("/api/v1/productos/42", solicitud.url.encodedPath)
            respond("", respuesta)
        }
        conRepositorio(motor) { base, local, repo ->
            local.guardarSincronizado(Producto(42, "Original", 2.5, 7))
            val eliminar = EliminarProductoUseCase(repo)

            val error = assertFailsWith<ErrorApiException> { eliminar(42).getOrThrow() }
            assertEquals(ErrorApi.SinPrivilegio, error.error)
            assertEquals(42L, base.productoQueries.listarTodos().executeAsOne().id)

            respuesta = HttpStatusCode.NoContent
            eliminar(42).getOrThrow()
            assertTrue(base.productoQueries.listarTodos().executeAsList().isEmpty())
        }
    }

    private suspend fun conRepositorio(
        motor: MockEngine,
        telemetria: Telemetria = TelemetriaDesactivada,
        prueba: suspend (PharmaDb, ProductoLocal, ProductoRepositorioOffline) -> Unit
    ) {
        val driver = testDriver()
        val cliente = HttpClient(motor) {
            expectSuccess = true
            defaultRequest { contentType(ContentType.Application.Json) }
            install(ContentNegotiation) { json(Json { encodeDefaults = true }) }
        }
        try {
            val base = PharmaDb(driver)
            val local = ProductoLocal(base)
            prueba(base, local, ProductoRepositorioOffline(local, ProductoApi(cliente), telemetria))
        } finally {
            cliente.close()
            driver.close()
        }
    }

    private companion object {
        val JSON = headersOf(HttpHeaders.ContentType, "application/json")
        const val PRODUCTO_REMOTO = """{"id":42,"nombre":"Confirmado","precio":2.5,"stock":7}"""
        const val PAGINA_VACIA =
            """{"contenido":[],"pagina":0,"tamanio":1,"totalElementos":0,"totalPaginas":0,"ultima":true}"""
    }
}
