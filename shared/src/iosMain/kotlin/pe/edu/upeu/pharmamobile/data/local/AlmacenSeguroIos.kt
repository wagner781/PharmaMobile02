package pe.edu.upeu.pharmamobile.data.local

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSUserDefaults

@OptIn(ExperimentalForeignApi::class)
class AlmacenSeguroIos : AlmacenSeguro {

    private val userDefaults = NSUserDefaults.standardUserDefaults

    override fun guardarToken(token: String) {
        userDefaults.setObject(token, CLAVE)
    }

    override fun leerToken(): String? {
        return userDefaults.stringForKey(CLAVE)
    }

    override fun borrarToken() {
        userDefaults.removeObjectForKey(CLAVE)
    }

    private companion object {
        const val CLAVE = "jwt_token"
    }
}
