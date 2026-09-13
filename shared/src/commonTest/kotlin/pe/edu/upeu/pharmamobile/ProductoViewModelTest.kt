package pe.edu.upeu.pharmamobile

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import pe.edu.upeu.pharmamobile.domain.usecase.RegistrarProductoUseCase
import pe.edu.upeu.pharmamobile.presentation.producto.ProductoUiState
import pe.edu.upeu.pharmamobile.presentation.producto.ProductoViewModel
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ProductoViewModelTest {

    // Un solo dispatcher controlado para todas las pruebas
    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // PRUEBA 1: repositorio vacío -> Fase.SinProductos
    @Test
    fun repositorioVacio_produceFaseSinProductos() = runTest(dispatcher) {
        val repo = RepositorioVacio()
        val useCase = RegistrarProductoUseCase(repo)
        // Orden real del constructor: (repository, useCase)
        val viewModel = ProductoViewModel(repo, useCase)

        advanceUntilIdle() // Espera a que corra el init { cargarProductos() }

        val estado = viewModel.uiState.value
        assertTrue(
            estado.fase is ProductoUiState.Fase.SinProductos,
            "Se esperaba SinProductos pero fue ${estado.fase}"
        )
    }

    // PRUEBA 2: repositorio con 3 productos -> Fase.ConProductos
    @Test
    fun repositorioConProductos_produceFaseConProductos() = runTest(dispatcher) {
        val repo = RepositorioConProductos()
        val useCase = RegistrarProductoUseCase(repo)
        val viewModel = ProductoViewModel(repo, useCase)

        advanceUntilIdle()

        val estado = viewModel.uiState.value
        assertTrue(estado.fase is ProductoUiState.Fase.ConProductos)
        assertEquals(3, estado.productos.size)
    }

    // PRUEBA 3: repositorio que lanza excepción -> Fase.Error
    @Test
    fun repositorioFalla_produceFaseError() = runTest(dispatcher) {
        val repo = RepositorioFalla()
        val useCase = RegistrarProductoUseCase(repo)
        val viewModel = ProductoViewModel(repo, useCase)

        advanceUntilIdle()

        val estado = viewModel.uiState.value
        assertTrue(
            estado.fase is ProductoUiState.Fase.Error,
            "Se esperaba Error pero fue ${estado.fase}"
        )
    }

    // PRUEBA 4: precio "0" -> error en formulario SIN llamar al repositorio
    @Test
    fun registroConPrecioCero_muestraErrorSinLlamarRepositorio() = runTest(dispatcher) {
        val repo = RepositorioConProductos()
        val useCase = RegistrarProductoUseCase(repo)
        val viewModel = ProductoViewModel(repo, useCase)

        advanceUntilIdle() // Deja que termine la carga inicial

        // Forzamos formulario con precio inválido
        viewModel.actualizarFormulario(
            nombre = "Paracetamol",
            precio = "0",
            stock = "100"
        )
        viewModel.registrarProducto()
        advanceUntilIdle()

        val estado = viewModel.uiState.value

        // Debe haber mensaje de error
        assertNotNull(estado.mensaje, "Debe existir un mensaje de error")

        // El repositorio NO debió ser llamado (la validación lo impidió)
        assertFalse(
            repo.registrarLlamado,
            "El repositorio no debió ser llamado con precio 0"
        )
    }
}