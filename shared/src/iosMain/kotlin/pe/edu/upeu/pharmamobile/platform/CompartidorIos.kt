package pe.edu.upeu.pharmamobile.platform

import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import pe.edu.upeu.pharmamobile.domain.platform.Compartidor

class CompartidorIos : Compartidor {
    override fun compartir(texto: String) {
        val controlador = UIActivityViewController(
            activityItems = listOf(texto),
            applicationActivities = null
        )
        UIApplication.sharedApplication
            .keyWindow
            ?.rootViewController
            ?.presentViewController(controlador, true, null)
    }
}
