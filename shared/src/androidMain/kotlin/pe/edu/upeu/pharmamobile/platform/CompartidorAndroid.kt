package pe.edu.upeu.pharmamobile.platform

import android.content.Context
import android.content.Intent
import pe.edu.upeu.pharmamobile.domain.platform.Compartidor

class CompartidorAndroid(
    private val contexto: Context
) : Compartidor {
    override fun compartir(texto: String) {
        val envio = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, texto)
        }
        val selector = Intent.createChooser(envio, null).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        contexto.startActivity(selector)
    }
}
