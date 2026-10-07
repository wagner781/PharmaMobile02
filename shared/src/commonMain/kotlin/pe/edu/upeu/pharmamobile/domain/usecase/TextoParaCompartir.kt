package pe.edu.upeu.pharmamobile.domain.usecase

import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.platform.formatearSoles

fun Producto.comoTextoParaCompartir(): String =
    "$nombre — ${formatearSoles(precio)} · Stock: $stock"
