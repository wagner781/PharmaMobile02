package pe.edu.upeu.pharmamobile.presentation.producto

import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.platform.formatearSoles

data class ProductoUi(
    val id: Long,
    val nombre: String,
    val precio: String,
    val precioOriginal: Double,
    val stock: Int,
    val requiereReposicion: Boolean
)

fun Producto.toUi() = ProductoUi(
    id = id,
    nombre = nombre,
    precio = formatearSoles(precio),
    precioOriginal = precio,
    stock = stock,
    requiereReposicion = requiereReposicion
)
