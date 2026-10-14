package pe.edu.upeu.pharmamobile.presentation.sesion

import pe.edu.upeu.pharmamobile.domain.model.Sesion

data class SesionUiState(
    val usuario: String = "",
    val clave: String = "",
    val cargando: Boolean = false,
    val error: String? = null,
    val sesion: Sesion? = null
)
