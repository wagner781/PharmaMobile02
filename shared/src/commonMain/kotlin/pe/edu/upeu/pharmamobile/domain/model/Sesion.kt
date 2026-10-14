package pe.edu.upeu.pharmamobile.domain.model

data class Credenciales(val usuario: String, val clave: String)

data class Sesion(
    val token: String,
    val usuario: String,
    val nombreCompleto: String,
    val permisos: Set<String>
) {
    fun puede(permiso: String): Boolean = permiso in permisos
}
