package mx.edu.unpa.calificacionesunpa.models


import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Alumno(
    val apMaterno: String = "",
    val apPaterno: String = "",
    val nombre: String = "",
    var esRegular: Boolean=true,
    @Transient
    var materias: List<Materia>? = null, // Se ignora al guardar en Firestore
    @Transient
    var nombreCarrera : String? = null,
    @Transient
    var matricula: String = "",

    var usuario: Usuario? = null


)

