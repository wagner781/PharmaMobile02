package pe.edu.upeu.pharmamobile

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import pe.edu.upeu.pharmamobile.domain.error.ErrorApi
import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.domain.usecase.*
import pe.edu.upeu.pharmamobile.presentation.producto.ProductoUiState
import pe.edu.upeu.pharmamobile.presentation.producto.ProductoViewModel
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@OptIn(ExperimentalCoroutinesApi::class)
class ProductoViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun crearViewModel(repositorio: FakeProductoRepository): ProductoViewModel {
        return ProductoViewModel(
            ListarProductosUseCase(repositorio),
            RegistrarProductoUseCase(repositorio),
            ActualizarProductoUseCase(repositorio),
            EliminarProductoUseCase(repositorio)
        )
    }

    @Test
    fun `el listado vacio termina en SinProductos`() = runTest(dispatcher) {
        val repositorio = FakeProductoRepository(initialProductos = emptyList())
        val viewModel = crearViewModel(repositorio)

        advanceUntilIdle()

        assertIs<ProductoUiState.Fase.SinProductos>(viewModel.uiState.value.fase)
    }

    @Test
    fun `carga exitosa pasa a ConProductos con la lista esperada`() = runTest(dispatcher) {
        val productosIniciales = listOf(Producto(1L, "Test", 10.0, 5))
        val repositorio = FakeProductoRepository(initialProductos = productosIniciales)
        val viewModel = crearViewModel(repositorio)

        advanceUntilIdle()

        val fase = viewModel.uiState.value.fase
        assertIs<ProductoUiState.Fase.ConProductos>(fase)
        assertEquals(1, fase.productos.size)
        assertEquals("Test", fase.productos[0].nombre)
    }

    @Test
    fun `error de validacion deja mensajes en el formulario y no cambia fase a Error`() = runTest(dispatcher) {
        val repositorio = FakeProductoRepository(initialProductos = listOf(Producto(1L, "Test", 10.0, 5)))
        val viewModel = crearViewModel(repositorio)
        
        advanceUntilIdle() // cargar lista
        
        // Forzamos un error de validación simulando lo que haría el UseCase
        // (nombre en blanco y precio nulo disparará las validaciones cliente/useCase)
        viewModel.actualizarFormulario(nombre = "", precio = "abc", stock = "10")
        viewModel.guardarProducto()
        
        advanceUntilIdle()

        val estado = viewModel.uiState.value
        assertEquals("El nombre es obligatorio", estado.formulario.nombreError)
        assertEquals("Ingrese un precio numérico", estado.formulario.precioError)
        // La fase sigue siendo ConProductos porque la lista no se afectó por el intento de guardar fallido
        assertIs<ProductoUiState.Fase.ConProductos>(estado.fase)
    }

    @Test
    fun `durante la eliminacion el estado es EnCurso y luego Inactiva`() = runTest(dispatcher) {
        val productoA = Producto(1L, "Test A", 10.0, 5)
        val productoB = Producto(2L, "Test B", 10.0, 5)
        val repositorio = FakeProductoRepository(initialProductos = listOf(productoA, productoB))
        val viewModel = crearViewModel(repositorio)

        advanceUntilIdle() // carga inicial -> ConProductos con 2 items
        
        // Disparamos eliminación
        viewModel.eliminar(1L)
        kotlinx.coroutines.yield()
        
        // El estado debe ser EnCurso inmediatamente
        val estadoDurante = viewModel.uiState.value
        assertIs<ProductoUiState.Operacion.EnCurso>(estadoDurante.operacion)
        assertEquals(ProductoUiState.Operacion.Tipo.Eliminar, (estadoDurante.operacion as ProductoUiState.Operacion.EnCurso).tipo)

        // Avanzamos hasta que termine
        advanceUntilIdle()

        val estadoDespues = viewModel.uiState.value
        // El estado de operación vuelve a Inactiva
        assertIs<ProductoUiState.Operacion.Inactiva>(estadoDespues.operacion)
        // La lista se debe haber recargado con 1 solo elemento
        val fase = estadoDespues.fase
        assertIs<ProductoUiState.Fase.ConProductos>(fase)
        assertEquals(1, fase.productos.size)
        assertEquals("Test B", fase.productos[0].nombre) // Test A fue eliminado
    }
}