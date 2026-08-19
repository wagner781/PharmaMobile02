package pe.edu.upeu.pharmamobile.domain.model

data class Producto(
    val id: Long,
    val nombre: String,
    val precio: Double,
    val stock: Int
){
    init {
        require(nombre.isNotBlank()){
            "El nombre no puede estar vacio"
        }
        require(precio>0){
            "El precio debe ser mayor que 0"
        }
        require(stock>=0){
            "El stock no puede ser negativo"
        }
    }
    fun verificarStock(cantidad: Int): Boolean{
        return stock >= cantidad
    }
    fun estadoDisponible(): Boolean{
        return stock > 0
    }
    fun valorInventario(): Double{
        return precio * stock
    }
    fun disminuirStock(cantidad: Int): Producto{
        require(cantidad > 0){
            "La cantidad debe ser mayor que 0"
        }
        require(verificarStock(cantidad)){
            "Stock insuficiente"
        }
        return copy(
            stock = stock - cantidad
        )
    }
}

