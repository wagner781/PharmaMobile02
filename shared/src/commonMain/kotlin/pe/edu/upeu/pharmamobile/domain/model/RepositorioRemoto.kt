package pe.edu.upeu.pharmamobile.domain.model

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

suspend fun obtenerProductosRemoto(): List<Producto> {
    delay(1000)

    return listOf(
        Producto(10, "Omeprazol", 15.00, 200),
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