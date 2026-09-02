package pe.edu.upeu.pharmamobile.navegation

//Destinos de navegacion de la aplicacion
sealed class Screen {
    data object Inicio : Screen()

    data object Productos : Screen()

    data object Clientes : Screen()

    data object Pedidos : Screen()
}