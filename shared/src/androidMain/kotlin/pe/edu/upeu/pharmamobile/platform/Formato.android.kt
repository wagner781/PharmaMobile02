package pe.edu.upeu.pharmamobile.platform

import java.text.NumberFormat
import java.util.Locale

actual fun formatearSoles(valor: Double): String =
    NumberFormat.getCurrencyInstance(Locale("es", "PE")).format(valor)
