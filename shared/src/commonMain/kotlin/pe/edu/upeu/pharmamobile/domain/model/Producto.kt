package pe.edu.upeu.pharmamobile.domain.model

data class Producto(
    val id: Long,
    val nombre: String,
    val precio: Double,
    val stock: Int = 10,
    val descripcion: String = "",
    val imagen: String = "",
    val categoria: String = "",
    val estado: Boolean = true
){

    // Regla de negocio: requiere reposición si stock <= 5
    val requiereReposicion: Boolean
        get() = stock <= STOCK_MINIMO

    companion object {
        const val STOCK_MINIMO = 5
    }
}

