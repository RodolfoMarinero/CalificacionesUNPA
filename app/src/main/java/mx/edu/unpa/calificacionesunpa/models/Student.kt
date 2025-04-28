package mx.edu.unpa.calificacionesunpa.models

import com.beust.klaxon.Klaxon

private val klaxon = Klaxon()

data class Student(

    val matricula: String? = null,
    val nombre: String? = null,
    val apePaterno: String? = null,
    val apeMaterno: String? = null,
    val correo: String? = null,
    val activo: Boolean = true,
    val materias: List<Materia> = emptyList()
) {
    fun toJson(): String = klaxon.toJsonString(this)

    companion object {
        fun fromJson(json: String): Student? = klaxon.parse(json)
    }
}
