package pe.edu.upeu.pharmamobile.domain.model

data class Producto(
    val id: Long,
    val nombre: String,
    val precio: Double,
    val stock: Int
){

    // Regla de negocio: requiere reposición si stock <= 5
    val requiereReposicion: Boolean
        get() = stock <= STOCK_MINIMO

    companion object {
        const val STOCK_MINIMO = 5
    }
}

