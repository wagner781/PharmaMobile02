package pe.edu.upeu.pharmamobile.data.mapper

import pe.edu.upeu.pharmamobile.data.remote.dto.ProductoDto
import pe.edu.upeu.pharmamobile.data.remote.dto.ProductoResponseDto
import pe.edu.upeu.pharmamobile.data.remote.dto.ProductoRequestDto
import pe.edu.upeu.pharmamobile.domain.model.Producto

fun ProductoDto.toDomain(): Producto = Producto(
    id = id.toLong(),
    nombre = title,
    precio = price,
    descripcion = description,
    imagen = images.firstOrNull() ?: "",
    categoria = categoria?.name ?: "Sin categoría"
)

fun ProductoResponseDto.toDomain(): Producto = Producto(
    id = id,
    nombre = nombre,
    precio = precio,
    stock = stock,
    categoria = categoriaNombre ?: "Sin categoría",
    estado = estado
)

fun Producto.toRequest(categoriaId: Long): ProductoRequestDto = ProductoRequestDto(
    nombre = nombre,
    precio = precio,
    stock = stock,
    categoriaId = categoriaId,
    estado = estado
)
