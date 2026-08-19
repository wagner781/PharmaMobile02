package pe.edu.upeu.pharmamobile.demo

import pe.edu.upeu.pharmamobile.domain.result.ResultadoProductos

fun mostrarResultado(resultado: ResultadoProductos){
    when(resultado){
        ResultadoProductos.Cargando ->{
            print(
                "Cargando Productos"
            )
        }
        is ResultadoProductos.Exito -> {
            print(
                "Productos Entontrados: ${resultado.productos.size}"
            )
        }
        is ResultadoProductos.Error -> {
            print(
                "Error: ${resultado.mensaje}"
            )
        }

    }
}