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
import pe.edu.upeu.pharmamobile.domain.usecase.EliminarProductoUseCase
import pe.edu.upeu.pharmamobile.domain.usecase.ActualizarProductoUseCase
import pe.edu.upeu.pharmamobile.domain.error.ErrorApi
import pe.edu.upeu.pharmamobile.domain.error.ErrorApiException
import pe.edu.upeu.pharmamobile.domain.usecase.comoTextoParaCompartir

class ProductoViewModel(
    private val repositorio: ProductoRepository,
    private val registrarProductoUseCase: RegistrarProductoUseCase,
    private val actualizarProductoUseCase: ActualizarProductoUseCase,
    private val eliminarProductoUseCase: EliminarProductoUseCase,
    private val compartidor: pe.edu.upeu.pharmamobile.domain.platform.Compartidor? = null
) : ViewModel() {

    private val portapapeles = pe.edu.upeu.pharmamobile.platform.Portapapeles()

    fun copiar(productoUi: pe.edu.upeu.pharmamobile.presentation.producto.ProductoUi) {
        val texto = "ID: ${productoUi.id} - ${productoUi.nombre}"
        portapapeles.copiar(texto)
    }

    fun compartir(productoUi: pe.edu.upeu.pharmamobile.presentation.producto.ProductoUi) {
        val producto = pe.edu.upeu.pharmamobile.domain.model.Producto(
            id = productoUi.id,
            nombre = productoUi.nombre,
            precio = productoUi.precioOriginal,
            stock = productoUi.stock
        )
        compartidor?.compartir(producto.comoTextoParaCompartir())
    }

    private val _uiState = MutableStateFlow(ProductoUiState())
    val uiState: StateFlow<ProductoUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repositorio.observar().collect { productos ->
                _uiState.update {
                    it.copy(
                        productos = productos,
                        fase = faseDe(productos)
                    )
                }
            }
        }
        sincronizar()
    }

    fun sincronizar() {
        viewModelScope.launch {
            _uiState.update { it.copy(sincronizando = true) }
            val resultado = resultadoDe { repositorio.sincronizar() }
            _uiState.update {
                it.copy(
                    sincronizando = false,
                    avisoRed = resultado.exceptionOrNull()?.let { fallo ->
                        "Sin conexión: mostrando datos guardados"
                    }
                )
            }
        }
    }

    private suspend fun <T> resultadoDe(bloque: suspend () -> T): Result<T> = runCatching { bloque() }

    private fun faseDe(productos: List<pe.edu.upeu.pharmamobile.domain.model.Producto>): ProductoUiState.Fase =
        if (productos.isEmpty()) ProductoUiState.Fase.SinProductos
        else ProductoUiState.Fase.ConProductos(productos.map { it.toUi() })

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
                    _uiState.update {
                        it.copy(
                            operacion = ProductoUiState.Operacion.Inactiva,
                            mensajeExito = "Producto registrado: ${producto.nombre}",
                            formulario = ProductoUiState.FormularioProducto()
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