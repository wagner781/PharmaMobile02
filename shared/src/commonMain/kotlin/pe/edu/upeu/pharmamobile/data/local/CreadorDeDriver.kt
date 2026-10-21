package pe.edu.upeu.pharmamobile.data.local

import app.cash.sqldelight.db.SqlDriver

interface CreadorDeDriver {
    fun crear(): SqlDriver
}
