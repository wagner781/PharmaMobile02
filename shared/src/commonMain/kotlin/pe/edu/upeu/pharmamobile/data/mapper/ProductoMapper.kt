package pe.edu.upeu.pharmamobile.data.mapper

import pe.edu.upeu.pharmamobile.data.remote.dto.ProductoDto
import pe.edu.upeu.pharmamobile.domain.model.Producto

fun ProductoDto.toDomain(): Producto = Producto(
    id = id.toLong(),
    nombre = title,
    precio = price,
    descripcion = description,
    imagen = images.firstOrNull() ?: "",
    categoria = categoria?.name ?: "Sin categoría"
)
