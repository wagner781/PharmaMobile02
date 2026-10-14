package pe.edu.upeu.pharmamobile.presentation.sesion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upeu.pharmamobile.domain.model.Credenciales
import pe.edu.upeu.pharmamobile.domain.model.ErrorSesion
import pe.edu.upeu.pharmamobile.domain.model.SesionException
import pe.edu.upeu.pharmamobile.domain.usecase.CerrarSesionUseCase
import pe.edu.upeu.pharmamobile.domain.usecase.IniciarSesionUseCase
import pe.edu.upeu.pharmamobile.domain.usecase.ObtenerSesionGuardadaUseCase

class SesionViewModel(
    private val iniciarSesionUseCase: IniciarSesionUseCase,
    private val cerrarSesionUseCase: CerrarSesionUseCase,
    private val obtenerSesionGuardadaUseCase: ObtenerSesionGuardadaUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SesionUiState(cargando = true))
    val uiState: StateFlow<SesionUiState> = _uiState.asStateFlow()

    init {
        verificarSesionGuardada()
    }

    fun onUsuarioChange(usuario: String) {
        _uiState.update { it.copy(usuario = usuario, error = null) }
    }

    fun onClaveChange(clave: String) {
        _uiState.update { it.copy(clave = clave, error = null) }
    }

    fun verificarSesionGuardada() {
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true) }
            val sesion = obtenerSesionGuardadaUseCase()
            _uiState.update {
                it.copy(
                    cargando = false,
                    sesion = sesion,
                    error = null
                )
            }
        }
    }

    fun iniciarSesion() {
        val usuario = _uiState.value.usuario.trim()
        val clave = _uiState.value.clave

        if (usuario.isBlank() || clave.isBlank()) {
            _uiState.update { it.copy(error = "Debe ingresar usuario y contraseña") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, error = null) }
            val resultado = iniciarSesionUseCase(Credenciales(usuario, clave))
            resultado.fold(
                onSuccess = { sesion ->
                    _uiState.update {
                        it.copy(
                            cargando = false,
                            sesion = sesion,
                            clave = "",
                            error = null
                        )
                    }
                },
                onFailure = { error ->
                    val mensaje = when (error) {
                        is SesionException -> when (error.error) {
                            ErrorSesion.CredencialesInvalidas -> "Credenciales inválidas. Verifique usuario y contraseña."
                            ErrorSesion.SesionExpirada -> "La sesión ha expirado. Inicie sesión nuevamente."
                            ErrorSesion.SinPermiso -> "No tiene permisos para acceder."
                            ErrorSesion.SinConexion -> "No se pudo conectar con el servidor."
                        }
                        else -> error.message ?: "Error al autenticar"
                    }
                    _uiState.update {
                        it.copy(
                            cargando = false,
                            error = mensaje
                        )
                    }
                }
            )
        }
    }

    fun cerrarSesion() {
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true) }
            cerrarSesionUseCase()
            _uiState.update {
                SesionUiState(
                    usuario = "",
                    clave = "",
                    cargando = false,
                    sesion = null,
                    error = null
                )
            }
        }
    }
}
