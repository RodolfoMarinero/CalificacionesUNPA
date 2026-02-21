package mx.edu.unpa.calificacionesunpa.models

data class UploadRequest(
    val userId: String,
    val fileName: String,
    val base64: String?,
    val seleccionado: Boolean = false
)