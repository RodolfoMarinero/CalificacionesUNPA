package mx.edu.unpa.calificacionesunpa.models

data class Calificaciones(
    val calificacionDefinitiva2: Double? = null,
    val calificacionDefinitiva: Double? = null,
    val especial: Double? = null,
    val examenFinal: Double? = null,
    val extra_1: Double? = null,
    val extra_2: Double? = null,
    val id: String? = null,
    val parcial_1: Double? = null,
    val parcial_2: Double? = null,
    val parcial_3: Double? = null,
    val promedioParciales: Double? = null,
    val tipo: Int? = null
)
