package pe.edu.upeu.pharmamobile.data.repository

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.inMemoryDriver
import pe.edu.upeu.pharmamobil.db.PharmaDb

actual fun testDriver(): SqlDriver {
    return inMemoryDriver(PharmaDb.Schema)
}
