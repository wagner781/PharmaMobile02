package pe.edu.upeu.pharmamobile.presentation.producto


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upeu.pharmamobile.domain.repository.ProductoRepository
import pe.edu.upeu.pharmamobile.domain.usecase.RegistrarProductoUseCase

class ProductoViewModel(
    private val repository: ProductoRepository,
    private val registrarProductoUseCase: RegistrarProductoUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductoUiState())
    val uiState: StateFlow<ProductoUiState> = _uiState.asStateFlow()

    init {
        cargarProductos()
    }

    fun cargarProductos() {
        viewModelScope.launch {
            _uiState.update { it.copy(fase = ProductoUiState.Fase.Cargando) }
            runCatching { repository.listar() }
                .onSuccess { lista ->
                    _uiState.update {
                        it.copy(
                            fase = if (lista.isEmpty()) ProductoUiState.Fase.SinProductos
                            else ProductoUiState.Fase.ConProductos,
                            productos = lista
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
            _uiState.update { it.copy(guardando = true, mensaje = null) }
            val resultado = registrarProductoUseCase(
                nombre = uiState.value.formulario.nombre,
                precioStr = uiState.value.formulario.precio,
                stockStr = uiState.value.formulario.stock
            )
            resultado.fold(
                onSuccess = { producto ->
                    _uiState.update {
                        it.copy(
                            guardando = false,
                            mensaje = "Producto registrado: ${producto.nombre}",
                            formulario = ProductoUiState.FormularioProducto() // Limpiar formulario
                        )
                    }
                    cargarProductos() // Actualizar lista
                },
                onFailure = { error ->
                    // Extraer errores del mensaje (simplificado)
                    val mensaje = error.message ?: "Error al registrar"
                    _uiState.update {
                        it.copy(
                            guardando = false,
                            mensaje = mensaje
                        )
                    }
                }
            )
        }
    }
}