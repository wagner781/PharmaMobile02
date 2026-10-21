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
import kotlin.test.assertTrue

class ProductoRepositorioOfflineTest {

    private fun baseEnMemoria(): PharmaDb {
        return PharmaDb(testDriver())
    }

    private class FakeRemoteApi(
        val productosRemotos: MutableList<Producto> = mutableListOf(),
        var debeFallar: Boolean = false
    ) : ProductoApi(HttpClient()) {
        val llamadasRegistro = mutableListOf<Producto>()

        override suspend fun listar(): List<Producto> {
            if (debeFallar) throw ErrorApi.SinConexion
            return productosRemotos.toList()
        }

        override suspend fun registrar(producto: Producto, categoriaId: Long): Producto {
            if (debeFallar) throw ErrorApi.SinConexion
            llamadasRegistro.add(producto)
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
    fun `sin red el producto queda pendiente y la pantalla lo ve`() = runTest {
        val apiFalla = FakeRemoteApi(debeFallar = true)
        val (repo, local) = repositorioCon(apiFalla)

        repo.registrar(Producto(0, "Ibuprofeno", 4.5, 20))

        val lista = repo.observar().first()
        assertEquals(1, lista.size)
        assertEquals("Ibuprofeno", lista.first().nombre)
        assertTrue(lista.first().id < 0, "El ID del producto creado sin red debe ser negativo")
        assertEquals(1, local.pendientes().size, "Debe quedar 1 producto en pendientes")
    }

    @Test
    fun `con red se sincroniza y actualiza la lista local`() = runTest {
        val productoRemoto = Producto(15, "Amoxicilina 500mg", 12.0, 30)
        val apiOk = FakeRemoteApi(productosRemotos = mutableListOf(productoRemoto), debeFallar = false)
        val (repo, _) = repositorioCon(apiOk)

        repo.sincronizar()

        val lista = repo.observar().first()
        assertEquals(1, lista.size)
        assertEquals(15L, lista.first().id)
        assertEquals("Amoxicilina 500mg", lista.first().nombre)
    }

    @Test
    fun `sincronizar sube pendientes antes de bajar del servidor`() = runTest {
        val api = FakeRemoteApi(debeFallar = true)
        val (repo, local) = repositorioCon(api)

        // 1. Registro sin red
        repo.registrar(Producto(0, "Paracetamol", 5.0, 10))
        assertEquals(1, local.pendientes().size)
        assertEquals(-1L, repo.observar().first().first().id)

        // 2. Vuelve la red y sincronizamos
        api.debeFallar = false
        repo.sincronizar()

        // 3. Verificamos que se envió el pendiente al servidor
        assertEquals(1, api.llamadasRegistro.size)
        assertEquals("Paracetamol", api.llamadasRegistro.first().nombre)

        // 4. Verificamos que la cola local de pendientes quedó limpia
        assertEquals(0, local.pendientes().size)

        // 5. Verificamos que ahora el producto tiene el ID positivo asignado por el servidor
        val productosFinales = repo.observar().first()
        assertEquals(1, productosFinales.size)
        assertTrue(productosFinales.first().id > 0, "El ID debe ser positivo tras sincronizar")
    }
}
