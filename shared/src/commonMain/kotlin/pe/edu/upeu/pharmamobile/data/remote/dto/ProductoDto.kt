package pe.edu.upeu.pharmamobile.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProductoDto(
    val id: Int,
    val title: String,
    val price: Double,
    val description: String = "",
    val images: List<String> = emptyList(),
    @SerialName("category") val categoria: CategoriaDto? = null
)

@Serializable
data class CategoriaDto(val id: Int, val name: String)
