package mx.edu.unpa.calificacionesunpa.models



data class Usuario(
    val matricula: String = "",
    val password: String = "",
    var esPrimerAcceso: Boolean = false
)