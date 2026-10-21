package pe.edu.upeu.pharmamobile.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.delete
import io.ktor.client.request.setBody
import pe.edu.upeu.pharmamobile.data.remote.dto.ProductoDto
import pe.edu.upeu.pharmamobile.data.remote.dto.ProductoRequestDto
import pe.edu.upeu.pharmamobile.data.remote.dto.ProductoResponseDto
import pe.edu.upeu.pharmamobile.data.remote.dto.PaginaResponseDto
import pe.edu.upeu.pharmamobile.data.mapper.toDomain
import pe.edu.upeu.pharmamobile.data.mapper.toRequest
import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.domain.error.ErrorApi

class ProductoApi(private val client: HttpClient) {
    suspend fun obtenerProductos(limite: Int = 10): List<ProductoDto> =
        client.get("/v1/products") {
            parameter("limit", limite)
        }.body()

    suspend fun listar(pagina: Int = 0, tamanio: Int = 20): PaginaResponseDto<ProductoResponseDto> =
        client.get("/api/v1/productos") {
            parameter("pagina", pagina)
            parameter("tamanio", tamanio)
        }.body()

    suspend fun listar(): List<Producto> = try {
        listar(pagina = 0, tamanio = 100).contenido.map { it.toDomain() }.filter { it.estado }
    } catch (e: Exception) {
        if (e is io.ktor.utils.io.errors.IOException || e is io.ktor.client.plugins.HttpRequestTimeoutException) {
            throw ErrorApi.SinConexion
        }
        throw e
    }

    suspend fun registrar(producto: Producto, categoriaId: Long = 1L): Producto = try {
        crear(producto.toRequest(categoriaId)).toDomain()
    } catch (e: Exception) {
        if (e is io.ktor.utils.io.errors.IOException || e is io.ktor.client.plugins.HttpRequestTimeoutException) {
            throw ErrorApi.SinConexion
        }
        throw e
    }

    suspend fun obtener(id: Long): ProductoResponseDto =
        client.get("/api/v1/productos/$id").body()

    suspend fun crear(request: ProductoRequestDto): ProductoResponseDto =
        client.post("/api/v1/productos") { setBody(request) }.body()

    suspend fun actualizar(id: Long, request: ProductoRequestDto): ProductoResponseDto =
        client.put("/api/v1/productos/$id") { setBody(request) }.body()

    suspend fun eliminar(id: Long) {
        client.delete("/api/v1/productos/$id")
    }
}
