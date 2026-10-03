package pe.edu.upeu.pharmamobile.presentation.producto


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upeu.pharmamobile.domain.usecase.ListarProductosUseCase
import pe.edu.upeu.pharmamobile.domain.usecase.RegistrarProductoUseCase
import pe.edu.upeu.pharmamobile.domain.usecase.EliminarProductoUseCase
import pe.edu.upeu.pharmamobile.domain.error.ErrorApi
import pe.edu.upeu.pharmamobile.domain.error.ErrorApiException

class ProductoViewModel(
    private val listarProductosUseCase: ListarProductosUseCase,
    private val registrarProductoUseCase: RegistrarProductoUseCase,
    private val eliminarProductoUseCase: EliminarProductoUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductoUiState())
    val uiState: StateFlow<ProductoUiState> = _uiState.asStateFlow()

    init {
        cargarProductos()
    }

    fun cargarProductos() {
        viewModelScope.launch {
            _uiState.update { it.copy(fase = ProductoUiState.Fase.Cargando) }
            listarProductosUseCase()
                .onSuccess { lista ->
                    _uiState.update {
                        it.copy(
                            fase = if (lista.isEmpty()) ProductoUiState.Fase.SinProductos
                            else ProductoUiState.Fase.ConProductos(lista)
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            fase = ProductoUiState.Fase.Error(error.message ?: "Error desconocido")
                        )
                    }
                }
        }
    }

    fun actualizarFormulario(
        nombre: String = uiState.value.formulario.nombre,
        precio: String = uiState.value.formulario.precio,
        stock: String = uiState.value.formulario.stock
    ) {
        _uiState.update {
            it.copy(
                formulario = it.formulario.copy(
                    nombre = nombre,
                    precio = precio,
                    stock = stock,
                    nombreError = null,
                    precioError = null,
                    stockError = null
                )
            )
        }
    }

    fun registrarProducto() {
        viewModelScope.launch {
            _uiState.update { it.copy(operacion = ProductoUiState.Operacion.EnCurso(ProductoUiState.Operacion.Tipo.Crear), mensajeExito = null) }
            val resultado = registrarProductoUseCase(
                nombre = uiState.value.formulario.nombre,
                precioStr = uiState.value.formulario.precio,
                stockStr = uiState.value.formulario.stock
            )
            resultado.fold(
                onSuccess = { producto ->
                    cargarProductos() // Actualizar lista
                    _uiState.update {
                        it.copy(
                            operacion = ProductoUiState.Operacion.Inactiva,
                            mensajeExito = "Producto registrado: ${producto.nombre}",
                            formulario = ProductoUiState.FormularioProducto() // Limpiar formulario
                        )
                    }
                },
                onFailure = { fallo -> manejarFallo(fallo) }
            )
        }
    }

    fun eliminar(id: Long) = viewModelScope.launch {
        _uiState.update { it.copy(operacion = ProductoUiState.Operacion.EnCurso(ProductoUiState.Operacion.Tipo.Eliminar)) }
        eliminarProductoUseCase(id)
            .onSuccess {
                cargarProductos()
                _uiState.update {
                    it.copy(
                        operacion = ProductoUiState.Operacion.Inactiva,
                        mensajeExito = "Producto eliminado"
                    )
                }
            }
            .onFailure { fallo -> manejarFallo(fallo) }
    }

    private fun manejarFallo(fallo: Throwable) {
        val error = (fallo as? ErrorApiException)?.error
        when (error) {
            is ErrorApi.Validacion -> _uiState.update {
                it.copy(
                    operacion = ProductoUiState.Operacion.Inactiva,
                    formulario = it.formulario.copy(
                        nombreError = error.porCampo["nombre"],
                        precioError = error.porCampo["precio"],
                        stockError = error.porCampo["stock"]
                    )
                )
            }
            else -> _uiState.update {
                it.copy(operacion = ProductoUiState.Operacion.Fallida(mensajeDe(error ?: fallo)))
            }
        }
    }

    private fun mensajeDe(error: Any): String = when (error) {
        is ErrorApi.NoEncontrado -> "Producto no encontrado"
        is ErrorApi.Conflicto -> error.mensaje
        is ErrorApi.Servidor -> "Error interno del servidor"
        is ErrorApi.SinConexion -> "Sin conexión a internet"
        is ErrorApi.TiempoAgotado -> "Tiempo de espera agotado"
        is Throwable -> error.message ?: "Error desconocido"
        else -> "Error desconocido"
    }
}