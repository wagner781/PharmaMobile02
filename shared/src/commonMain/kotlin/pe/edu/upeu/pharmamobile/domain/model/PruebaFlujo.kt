package pe.edu.upeu.pharmamobile.domain.model

import kotlinx.coroutines.runBlocking
import pe.edu.upeu.pharmamobile.domain.result.ResultadoProductos

fun main() = runBlocking {
    println("=== PRUEBA DE FLUJO (cargandoProductos ===")

    cargarProductos().collect { resultadoProductos ->
    when (resultadoProductos){
        ResultadoProductos.Cargando -> println("Estado: Cargando...")
        is ResultadoProductos.Exito -> {
            println("Estado: Exito. Productos encontrados: ${resultadoProductos.productos.size}")
            resultadoProductos.productos.forEach { println(" - ${it.nombre} (Stock:${it.stock})")}

        }

        is ResultadoProductos.Error -> println("Error: ${resultadoProductos.mensaje}")
    }
    }
}