package pe.edu.upeu.pharmamobile.data.repository

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import pe.edu.upeu.pharmamobil.db.PharmaDb
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ProductoMigracionAndroidHostTest {
    @Test
    fun migrarDesdeVersionUnoConservaProductosYEstadoDeSincronizacion() {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        try {
            driver.execute(
                null,
                """
                CREATE TABLE producto (
                    id INTEGER NOT NULL PRIMARY KEY,
                    nombre TEXT NOT NULL,
                    precio REAL NOT NULL,
                    stock INTEGER NOT NULL,
                    pendiente INTEGER NOT NULL DEFAULT 0
                )
                """.trimIndent(),
                0
            )
            driver.execute(null, "INSERT INTO producto VALUES (-1, 'Pendiente', 2.5, 7, 1)", 0)
            driver.execute(null, "INSERT INTO producto VALUES (42, 'Sincronizado', 3.0, 4, 0)", 0)
            driver.execute(
                null,
                "CREATE TABLE sincronizacion (id INTEGER NOT NULL PRIMARY KEY DEFAULT 1, timestamp INTEGER NOT NULL)",
                0
            )
            driver.execute(null, "INSERT INTO sincronizacion VALUES (1, 1234)", 0)

            assertEquals(2L, PharmaDb.Schema.version)
            PharmaDb.Schema.migrate(driver, oldVersion = 1, newVersion = PharmaDb.Schema.version)

            val base = PharmaDb(driver)
            val productos = base.productoQueries
            val filas = productos.listarTodos().executeAsList()
            assertEquals(listOf(-1L, 42L), filas.map { it.id })
            assertEquals(listOf(1L, 0L), filas.map { it.pendiente })
            assertEquals(listOf(0L, 0L), filas.map { it.intentos })
            assertEquals(listOf(null, null), filas.map { it.ultimoError })
            assertEquals(1234L, base.sincronizacionQueries.obtenerInstante().executeAsOne())

            productos.insertar(-2, "Nuevo pendiente", 4.0, 8, 1)
            assertEquals(listOf(-2L, -1L), productos.pendientesPorEnviar(2).executeAsList().map { it.id })
            assertEquals(2L, productos.contarPendientes(2).executeAsOne())
            assertEquals(0L, productos.contarFallidas(2).executeAsOne())

            productos.sumarIntento("Sin conexión", -1)
            assertEquals(2L, productos.contarPendientes(2).executeAsOne())
            productos.sumarIntento("Tiempo agotado", -1)
            productos.sumarIntento("No aplica", 42)

            assertEquals(listOf(-2L), productos.pendientesPorEnviar(2).executeAsList().map { it.id })
            assertEquals(1L, productos.contarPendientes(2).executeAsOne())
            assertEquals(1L, productos.contarFallidas(2).executeAsOne())
            val actualizadas = productos.listarTodos().executeAsList().associateBy { it.id }
            val pendiente = actualizadas.getValue(-1)
            assertEquals(2L, pendiente.intentos)
            assertEquals("Tiempo agotado", pendiente.ultimoError)
            assertEquals(0L, actualizadas.getValue(42).intentos)
            assertNull(actualizadas.getValue(42).ultimoError)
        } finally {
            driver.close()
        }
    }
}
