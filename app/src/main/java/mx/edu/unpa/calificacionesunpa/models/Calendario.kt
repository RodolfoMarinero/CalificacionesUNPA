package mx.edu.unpa.calificacionesunpa.models

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Calendario(
    @Transient
    val materia: String = "",
    val e1: String = "",
    val e2: String = "",
    val esp: String = "",
    val f: String = "",
    val p1: String = "",
    val p2: String = "",
    val p3: String = "",
    val ciclo: String = "",
)
