package pe.edu.upeu.pharmamobile.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponseDto(
    val message: String? = null,
    val validationErrors: Map<String, String>? = null
)
