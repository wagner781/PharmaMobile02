package pe.edu.upeu.pharmamobile.data.local

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.value
import platform.CoreFoundation.CFDictionaryRef
import platform.CoreFoundation.CFTypeRefVar
import platform.CoreFoundation.kCFBooleanTrue
import platform.Foundation.NSData
import platform.Foundation.NSMutableDictionary
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.dataWithBytes
import platform.Security.SecItemAdd
import platform.Security.SecItemCopyMatching
import platform.Security.SecItemDelete
import platform.Security.kSecAttrAccount
import platform.Security.kSecAttrService
import platform.Security.kSecClass
import platform.Security.kSecClassGenericPassword
import platform.Security.kSecMatchLimit
import platform.Security.kSecMatchLimitOne
import platform.Security.kSecReturnData
import platform.Security.kSecValueData
import platform.darwin.noErr

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
class AlmacenSeguroIos : AlmacenSeguro {

    private fun consultaBase(): NSMutableDictionary {
        val consulta = NSMutableDictionary()
        consulta.setObject(kSecClassGenericPassword, kSecClass)
        consulta.setObject("PharmaMobileService", kSecAttrService)
        consulta.setObject("jwt", kSecAttrAccount)
        return consulta
    }

    private fun ByteArray.toNSData(): NSData = memScoped {
        if (isEmpty()) return NSData()
        usePinned { pinned ->
            NSData.dataWithBytes(pinned.addressOf(0), size.toULong())
        }
    }

    override fun guardarToken(token: String) {
        borrarToken()
        val datos = token.encodeToByteArray().toNSData()
        val consulta = consultaBase().apply {
            setObject(datos, kSecValueData)
        }
        SecItemAdd(consulta as CFDictionaryRef, null)
    }

    override fun leerToken(): String? {
        val consulta = consultaBase().apply {
            setObject(kSecMatchLimitOne, kSecMatchLimit)
            setObject(kCFBooleanTrue, kSecReturnData)
        }
        return memScoped {
            val resultado = alloc<CFTypeRefVar>()
            val estado = SecItemCopyMatching(consulta as CFDictionaryRef, resultado.ptr)
            if (estado == noErr) {
                val data = resultado.value as? NSData ?: return null
                NSString.create(data, NSUTF8StringEncoding)?.toString()
            } else {
                null
            }
        }
    }

    override fun borrarToken() {
        SecItemDelete(consultaBase() as CFDictionaryRef)
    }
}
