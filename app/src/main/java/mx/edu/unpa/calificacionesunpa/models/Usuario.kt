package mx.edu.unpa.calificacionesunpa.models

import com.google.firebase.firestore.DocumentReference

data class Usuario(
    val email: String = "",
    val password: String = "",
    val alumnoRef: DocumentReference? = null
)