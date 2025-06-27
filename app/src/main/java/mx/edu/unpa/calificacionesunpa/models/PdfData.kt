package mx.edu.unpa.calificacionesunpa.models

import java.util.Date

data class PdfData(
    val id: String = "",
    val timestamp: Date = Date(0),
    var seleccionado : Boolean = false
)