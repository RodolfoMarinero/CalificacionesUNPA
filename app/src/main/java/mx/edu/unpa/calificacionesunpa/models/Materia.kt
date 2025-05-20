package mx.edu.unpa.calificacionesunpa.models

data class Materia(
    val id: String? = null,
    val nombre: String? = null,
    val semestre: String? = null,
    val calificaciones: String? = null, // Referencia a documento
    val activo: Boolean = true,
    val planDeEstudio: String? = null,
    val cicloEscolar: String? = null

)
