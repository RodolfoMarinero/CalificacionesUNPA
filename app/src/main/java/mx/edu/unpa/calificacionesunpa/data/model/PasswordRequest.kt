package mx.edu.unpa.calificacionesunpa.data.model


data class PasswordRequest(
    val passwordActual: String,
    val passwordNueva: String
)