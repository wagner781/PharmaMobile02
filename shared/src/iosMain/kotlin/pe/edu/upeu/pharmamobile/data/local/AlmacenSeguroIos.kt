package pe.edu.upeu.pharmamobile.data.local

import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreFoundation.CFDictionaryRef
import platform.CoreFoundation.CFMutableDictionaryRef
import platform.Foundation.NSUserDefaults
import platform.Security.SecItemAdd
import platform.Security.SecItemDelete
import platform.Security.kSecClass
import platform.Security.kSecClassGenericPassword
import platform.Security.kSecValueData

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
