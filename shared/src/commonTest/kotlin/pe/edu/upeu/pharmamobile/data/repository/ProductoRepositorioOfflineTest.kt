package pe.edu.upeu.pharmamobile.data.repository

import io.ktor.client.HttpClient
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import pe.edu.upeu.pharmamobil.db.PharmaDb
import pe.edu.upeu.pharmamobile.data.local.ProductoLocal
import pe.edu.upeu.pharmamobile.data.remote.ProductoApi
import pe.edu.upeu.pharmamobile.domain.error.ErrorApi
import pe.edu.upeu.pharmamobile.domain.model.Producto
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ProductoRepositorioOfflineTest {

    private fun baseEnMemoria(): PharmaDb {
        return PharmaDb(testDriver())
    }

    private class FakeRemoteApi(
        val productosRemotos: MutableList<Producto> = mutableListOf(),
        var debeFallar: Boolean = false,
        var fallarSoloAlSubir: Boolean = false
    ) : ProductoApi(HttpClient()) {
        val llamadasRegistro = mutableListOf<Producto>()

        override suspend fun listar(): List<Producto> {
            if (debeFallar) throw ErrorApi.SinConexion
            return productosRemotos.toList()
        }

        override suspend fun registrar(producto: Producto, categoriaId: Long): Producto {
            if (debeFallar || fallarSoloAlSubir) throw ErrorApi.SinConexion
            llamadasRegistro.add(producto)
            // Evitar duplicar en la lista remota si ya existe por nombre
            val existente = productosRemotos.find { it.nombre == producto.nombre }
            if (existente != null) return existente
            val guardado = producto.copy(id = 100L + llamadasRegistro.size)
            productosRemotos.add(guardado)
            return guardado
        }
    }

    private fun repositorioCon(api: ProductoApi): Pair<ProductoRepositorioOffline, ProductoLocal> {
        val db = baseEnMemoria()
        val local = ProductoLocal(db)
        val repo = ProductoRepositorioOffline(local, api)
        return Pair(repo, local)
    }

    @Test
    fun test1_sincronizacionConRedDisponibleSubePendientesYReemplazaRemotos() = runTest {
        val remoto1 = Producto(10, "Paracetamol 500mg", 2.50, 100)
        val remoto2 = Producto(20, "Amoxicilina 250mg", 8.00, 50)
        val api = FakeRemoteApi(productosRemotos = mutableListOf(remoto1, remoto2), debeFallar = false)
        val (repo, local) = repositorioCon(api)

        repo.sincronizar()

        val lista = repo.observar().first()
        assertEquals(2, lista.size, "Debe traer los 2 productos del servidor")
        assertEquals(0, local.pendientes().size, "No debe haber pendientes")
        assertNotNull(repo.obtenerUltimaSincronizacion(), "Debe guardar la marca de tiempo de sincronización")
    }

    @Test
    fun test2_registroSinRedGuardaLocalConMarcaPendienteYEmiteEnFlow() = runTest {
        val apiSinRed = FakeRemoteApi(debeFallar = true)
        val (repo, local) = repositorioCon(apiSinRed)

        val registrado = repo.registrar(Producto(0, "Ibuprofeno 400mg", 4.50, 30))

        assertTrue(registrado.id < 0, "El ID retornado debe ser negativo")
        val listaFlow = repo.observar().first()
        assertEquals(1, listaFlow.size, "El Flow reactivo debe emitir el nuevo producto de inmediato")
        assertEquals("Ibuprofeno 400mg", listaFlow.first().nombre)
        assertEquals(1, local.pendientes().size, "Debe registrarse con bandera pendiente en SQLite local")
    }

    @Test
    fun test3_reconexionSubeEfectivamenteLosRegistrosLocalesAlServidor() = runTest {
        val api = FakeRemoteApi(debeFallar = true)
        val (repo, local) = repositorioCon(api)

        // Registro desconectado
        repo.registrar(Producto(0, "Alcohol 70%", 7.00, 40))
        assertEquals(1, local.pendientes().size)

        // Se restablece la conexión y sincroniza
        api.debeFallar = false
        repo.sincronizar()

        assertEquals(1, api.llamadasRegistro.size, "Debe llamar a la API remota para subir el pendiente")
        assertEquals(0, local.pendientes().size, "La cola de pendientes debe quedar en cero")
        val lista = repo.observar().first()
        assertEquals(1, lista.size)
        assertTrue(lista.first().id > 0, "El producto local ahora tiene el ID definitivo del servidor")
    }

    @Test
    fun test4_bajadaDelServidorNoSobreescribeNiBorraRegistrosLocalesPendientes() = runTest {
        val api = FakeRemoteApi(
            productosRemotos = mutableListOf(Producto(5, "Aspirina 100mg", 3.00, 20)),
            fallarSoloAlSubir = true // Simula fallo parcial en subida pero lectura posible
        )
        val (repo, local) = repositorioCon(api)

        // Guardamos un producto pendiente localmente
        val localPendiente = local.guardarPendiente(Producto(0, "Jarabe Tos", 15.00, 10))
        assertEquals(1, local.pendientes().size)

        // Bajamos datos del servidor reemplazando únicamente sincronizados
        local.reemplazarSincronizados(listOf(Producto(5, "Aspirina 100mg", 3.00, 20)))

        val listaTotal = repo.observar().first()
        assertEquals(2, listaTotal.size, "Debe contener tanto el remoto bajado como el local pendiente")
        assertEquals(1, local.pendientes().size, "El registro pendiente no fue borrado ni sobreescrito")
        assertTrue(listaTotal.any { it.id == localPendiente.id }, "El producto pendiente sigue presente")
    }

    @Test
    fun test5_identificadoresTemporalesSinColisionConIdsDelServidor() = runTest {
        val apiSinRed = FakeRemoteApi(debeFallar = true)
        val (repo, _) = repositorioCon(apiSinRed)

        val prod1 = repo.registrar(Producto(0, "Termómetro", 25.00, 15))
        val prod2 = repo.registrar(Producto(0, "Oxímetro", 55.00, 10))

        assertEquals(-1L, prod1.id, "Primer ID temporal debe ser -1")
        assertEquals(-2L, prod2.id, "Segundo ID temporal debe ser -2 decremental")
        assertTrue(prod1.id < 0 && prod2.id < 0, "Todos los IDs locales temporales son negativos")
    }

    @Test
    fun test6_idempotenciaDeSincronizacionNoDuplicaRegistros() = runTest {
        val api = FakeRemoteApi(
            productosRemotos = mutableListOf(Producto(50, "Gasa Estéril", 1.50, 200)),
            debeFallar = false
        )
        val (repo, local) = repositorioCon(api)

        // Primera sincronización
        repo.sincronizar()
        val conteo1 = repo.observar().first().size

        // Segunda sincronización consecutiva
        repo.sincronizar()
        val conteo2 = repo.observar().first().size

        assertEquals(1, conteo1)
        assertEquals(conteo1, conteo2, "Ejecutar sincronizar() dos veces seguidas no debe duplicar registros")
        assertEquals(0, local.pendientes().size, "La cola de pendientes se mantiene en 0")
    }
}
