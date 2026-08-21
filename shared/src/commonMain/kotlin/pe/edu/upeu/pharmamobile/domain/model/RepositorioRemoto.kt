package pe.edu.upeu.pharmamobile.domain.model

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import pe.edu.upeu.pharmamobile.domain.result.ResultadoProductos

private val productosSimulados = listOf(
    Producto(1L, "Pracetamol", 8.50, 10),
    Producto(2L, "Ibuprofeno", 12.00, 50),
    Producto(3L, "Amoxicilina", 18.50, 20)
)
suspend fun obtenerProductosRemoto(): List<Producto> {
    delay(1000)

    return listOf(
        Producto(10L, "Omeprazol", 15.00, 200),
        Producto(11L, "Losartan", 22.50, 150)

    )
}

fun observarInventario(): Flow<List<Producto>> = flow {
    emit(emptyList())

    delay(2000)

    emit(
        listOf(
            Producto(10L, "Omeprazol", 15.00, 200),
            Producto(11L, "Losartan", 22.50, 150)

        )
    )
}

// paso 4 y 5 funcion suspend que usa la Lista simulada
suspend fun obtenerProductos(): List<Producto>{
    delay(1000)
    return productosSimulados
}

// Paso 9 al 14  Flow básico de productos
fun observarProductos(): Flow<List<Producto>> = flow {
    emit(emptyList())
    delay(1000)
    emit(productosSimulados)
}

//Paso 15 a 17 Flow que emite estados (caragando -> Exito)
fun cargarProductos(): Flow<ResultadoProductos> = flow {
    emit(ResultadoProductos.Cargando)

    delay(1500)

    emit(ResultadoProductos.Exito(productosSimulados))
}

