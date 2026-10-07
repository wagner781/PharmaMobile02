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
import pe.edu.upeu.pharmamobile.domain.usecase.comoTextoParaCompartir

class ProductoViewModel(
    private val listarProductosUseCase: ListarProductosUseCase,
    private val registrarProductoUseCase: RegistrarProductoUseCase,
    private val actualizarProductoUseCase: pe.edu.upeu.pharmamobile.domain.usecase.ActualizarProductoUseCase,
    private val eliminarProductoUseCase: EliminarProductoUseCase,
    private val compartidor: pe.edu.upeu.pharmamobile.domain.platform.Compartidor
) : ViewModel() {

    fun compartir(productoUi: pe.edu.upeu.pharmamobile.presentation.producto.ProductoUi) {
        // Mapear de vuelta a Producto para el usecase si fuera necesario, o simplemente usar las propiedades
        // Pero el usecase comoTextoParaCompartir espera un Producto.
        // Vamos a mapear temporalmente:
        val producto = pe.edu.upeu.pharmamobile.domain.model.Producto(
            id = productoUi.id,
            nombre = productoUi.nombre,
            precio = productoUi.precioOriginal,
            stock = productoUi.stock
        )
        compartidor.compartir(producto.comoTextoParaCompartir())
    }

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
                            else ProductoUiState.Fase.ConProductos(lista.map { p -> p.toUi() })
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
        id: Long? = uiState.value.formulario.id,
        nombre: String = uiState.value.formulario.nombre,
        precio: String = uiState.value.formulario.precio,
        stock: String = uiState.value.formulario.stock
    ) {
        _uiState.update {
            it.copy(
                formulario = it.formulario.copy(
                    id = id,
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

    fun editarProducto(producto: pe.edu.upeu.pharmamobile.presentation.producto.ProductoUi) {
        actualizarFormulario(
            id = producto.id,
            nombre = producto.nombre,
            precio = producto.precioOriginal.toString(),
            stock = producto.stock.toString()
        )
    }

    fun guardarProducto() {
        val formulario = uiState.value.formulario
        if (formulario.id == null) {
            registrarProducto()
        } else {
            actualizarProducto(formulario.id)
        }
    }

    private fun registrarProducto() {
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

    private fun actualizarProducto(id: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(operacion = ProductoUiState.Operacion.EnCurso(ProductoUiState.Operacion.Tipo.Actualizar), mensajeExito = null) }
            
            // Validaciones manuales básicas que hacía el RegistrarProductoUseCase
            val nombre = uiState.value.formulario.nombre.trim()
            val precioStr = uiState.value.formulario.precio
            val stockStr = uiState.value.formulario.stock
            
            val mapaErrores = mutableMapOf<String, String>()
            if (nombre.isBlank()) mapaErrores["nombre"] = "El nombre es obligatorio"
            if (precioStr.toDoubleOrNull() == null) mapaErrores["precio"] = "Precio inválido"
            if (stockStr.toIntOrNull() == null) mapaErrores["stock"] = "Stock inválido"

            if (mapaErrores.isNotEmpty()) {
                manejarFallo(pe.edu.upeu.pharmamobile.domain.error.ErrorApiException(
                    pe.edu.upeu.pharmamobile.domain.error.ErrorApi.Validacion(mapaErrores)
                ))
                return@launch
            }
            
            val producto = pe.edu.upeu.pharmamobile.domain.model.Producto(
                id = id,
                nombre = nombre,
                precio = precioStr.toDouble(),
                stock = stockStr.toInt()
            )
            
            val resultado = actualizarProductoUseCase(producto)
            resultado.fold(
                onSuccess = { prod ->
                    cargarProductos()
                    _uiState.update {
                        it.copy(
                            operacion = ProductoUiState.Operacion.Inactiva,
                            mensajeExito = "Producto actualizado: ${prod.nombre}",
                            formulario = ProductoUiState.FormularioProducto()
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