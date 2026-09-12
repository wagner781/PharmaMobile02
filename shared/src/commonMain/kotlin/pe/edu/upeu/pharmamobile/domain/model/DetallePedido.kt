package pe.edu.upeu.pharmamobile.domain.model

data class DetallePedido(
    val producto: Producto,
    val cantidad: Int
) {
    init {
        require(cantidad > 0) { "La cantidad debe ser mayor que cero" }
    }
    val subtotal: Double get() = producto.precio * cantidad
}