package pe.edu.upeu.pharmamobile.domain.model

fun demostrarColeccion() {

    val productos = listOf(
        Producto(1L, "Paracetamol", 5.50, 100),
        Producto(2L, "Ibuprofeno", 8.20, 0),
        Producto(3L, "Amoxicilina", 12.30, 45)
    )

    // FILTER: Solo productos con stock mayor a 0 (disponibles)
    val disponibles = productos.filter { it.stock > 0}
    println("Productos disponibles: ${disponibles.size}")

    // 2. MAP: Extraer solo los nombres de todos los productos
    val nombresProductos = productos.map { it.nombre}
    println("Nombres: $nombresProductos")

    // 3. FIND: Buscar un producto por su ID (retorna null si no existe)
    val productosBuscados = productos.find { it.id == 2L }
    println("Productos con ID 2: ${productosBuscados?.nombre}")

    // Bonus: Suma de precios (sumOf)
    val valorTotalInventario = productos.sumOf { it.precio * it.stock }
    println("Valor total del investario: S/. $valorTotalInventario")

}