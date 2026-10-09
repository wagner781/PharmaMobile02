package pe.edu.upeu.pharmamobile.platform

import platform.UIKit.UIPasteboard
import platform.UIKit.UIAlertController
import platform.UIKit.UIAlertAction
import platform.UIKit.UIAlertActionStyleDefault
import platform.UIKit.UIAlertControllerStyleAlert
import platform.UIKit.UIApplication

actual class Portapapeles actual constructor() {
    actual fun copiar(texto: String) {
        UIPasteboard.generalPasteboard.string = texto
        
        // Intentar mostrar confirmación visual simple en iOS
        val alert = UIAlertController.alertControllerWithTitle("Copiado", "Texto copiado al portapapeles", UIAlertControllerStyleAlert)
        alert.addAction(UIAlertAction.actionWithTitle("OK", UIAlertActionStyleDefault, null))
        
        UIApplication.sharedApplication.keyWindow?.rootViewController?.presentViewController(alert, true, null)
    }
}
