package mx.edu.unpa.calificacionesunpa.data.model

import com.google.gson.annotations.SerializedName
import mx.edu.unpa.calificacionesunpa.models.Alumno
import mx.edu.unpa.calificacionesunpa.models.Materia
import mx.edu.unpa.calificacionesunpa.models.Usuario

data class AlumnoResponse(
    // Nota: Quité "id" porque en tu JSON no venía, pero si lo necesitas déjalo.

    @SerializedName("matricula")
    val matricula: String,

    @SerializedName("nombre")     // <--- ESTO FALTABA
    val nombre: String?,

    @SerializedName("apPaterno")  // <--- ESTO FALTABA
    val apPaterno: String?,

    @SerializedName("apMaterno")  // <--- ESTO FALTABA
    val apMaterno: String?,

    @SerializedName("esRegular")  // <--- ESTO FALTABA
    val esRegular: Boolean?,

    @SerializedName("nombreCarrera")
    val nombreCarrera: String?,

    @SerializedName("usuario")
    val usuario: Usuario?,

    @SerializedName("materias")
    val materias: List<Materia>?
) {
    fun toDomainModel(): Alumno {
        return Alumno(
            matricula = this.matricula,
            // Aquí asignamos los valores que acabamos de capturar del JSON
            nombre = this.nombre ?: "",
            apPaterno = this.apPaterno ?: "",
            apMaterno = this.apMaterno ?: "",
            esRegular = this.esRegular ?: true,
            nombreCarrera = this.nombreCarrera,
            usuario = this.usuario,
            materias = this.materias
        )
    }
}