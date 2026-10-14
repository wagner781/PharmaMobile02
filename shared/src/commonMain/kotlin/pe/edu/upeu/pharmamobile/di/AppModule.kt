package pe.edu.upeu.pharmamobile.di

import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module
import org.koin.core.module.dsl.viewModelOf
import pe.edu.upeu.pharmamobile.data.remote.ProductoApi
import pe.edu.upeu.pharmamobile.data.remote.crearHttpClient
import pe.edu.upeu.pharmamobile.domain.repository.ProductoRepository
import pe.edu.upeu.pharmamobile.data.repository.ProductoRepositorioRest
import pe.edu.upeu.pharmamobile.domain.usecase.ListarProductosUseCase
import pe.edu.upeu.pharmamobile.domain.usecase.RegistrarProductoUseCase
import pe.edu.upeu.pharmamobile.domain.usecase.ActualizarProductoUseCase
import pe.edu.upeu.pharmamobile.domain.usecase.EliminarProductoUseCase
import pe.edu.upeu.pharmamobile.presentation.producto.ProductoViewModel
import pe.edu.upeu.pharmamobile.presentation.sesion.SesionViewModel

import pe.edu.upeu.pharmamobile.domain.repository.AuthRepository
import pe.edu.upeu.pharmamobile.data.repository.AuthRepositorioRest
import pe.edu.upeu.pharmamobile.domain.usecase.IniciarSesionUseCase
import pe.edu.upeu.pharmamobile.domain.usecase.CerrarSesionUseCase
import pe.edu.upeu.pharmamobile.domain.usecase.ObtenerSesionGuardadaUseCase

private const val CATEGORIA_POR_DEFECTO = 1L

expect val platformModule: Module

val dataModule = module {
    single { crearHttpClient(get(), get()) }
    single { ProductoApi(get()) }
    single<ProductoRepository> {
        ProductoRepositorioRest(get(), CATEGORIA_POR_DEFECTO)
    }
    single<AuthRepository> {
        AuthRepositorioRest(get(), get())
    }
}

val domainModule = module {
    factory { ListarProductosUseCase(get()) }
    factory { RegistrarProductoUseCase(get()) }
    factory { ActualizarProductoUseCase(get()) }
    factory { EliminarProductoUseCase(get()) }
    factory { IniciarSesionUseCase(get()) }
    factory { CerrarSesionUseCase(get()) }
    factory { ObtenerSesionGuardadaUseCase(get()) }
}
 
val presentationModule = module {
    viewModelOf(::ProductoViewModel)
    viewModelOf(::SesionViewModel)
}

fun initKoin(config: KoinAppDeclaration? = null) = startKoin {
    config?.invoke(this)
    modules(dataModule, domainModule, presentationModule, platformModule)
}