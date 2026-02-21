package mx.edu.unpa.calificacionesunpa.models

import java.util.Date

data class FileResponse(
    val id: String,
    val base64: String?,
    val timestamp: Date,
    val seleccionado: Boolean
)