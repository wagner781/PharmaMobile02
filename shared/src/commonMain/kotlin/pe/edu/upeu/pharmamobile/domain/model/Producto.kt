package pe.edu.upeu.pharmamobile.domain.model

data class Producto(
    val id: Long,
    val nombre: String,
    val precio: Double,
    val stock: Int
)

