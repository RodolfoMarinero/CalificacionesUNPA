package mx.edu.unpa.calificacionesunpa.models

import com.google.gson.annotations.SerializedName

data class Alumno(
    @SerializedName("matricula")
    var matricula: String = "",

    @SerializedName("apMaterno") // En el log viene como "apMaterno"
    val apMaterno: String = "",

    @SerializedName("apPaterno") // En el log viene como "apPaterno"
    val apPaterno: String = "",

    @SerializedName("nombre")    // En el log viene como "nombre"
    val nombre: String = "",

    @SerializedName("esRegular")
    var esRegular: Boolean = true,

    @SerializedName("nombreCarrera")
    var nombreCarrera : String? = null, // En el log viene "nombreCarrera"

    @Transient
    var materias: List<Materia>? = null,

    @Transient
    var usuario: Usuario? = null
)