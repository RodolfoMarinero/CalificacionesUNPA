package mx.edu.unpa.calificacionesunpa.data.model

data class ApiResponse<T>(
    val success: Boolean,
    val data: T?,
    val message: String?
)