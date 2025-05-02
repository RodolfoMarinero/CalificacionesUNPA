package mx.edu.unpa.calificacionesunpa.providers

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import mx.edu.unpa.calificacionesunpa.models.Materia

class MateriaProvider {
    private val db = FirebaseFirestore.getInstance()

    /**
     * Trae el documento 'Materia' con ID = materiaId.
     * Callback recibe null si no existe o hay error.
     * Mapea manualmente el campo 'calificaciones' a String path.
     */
    fun getMateriaById(materiaId: String, callback: (Materia?) -> Unit) {
        db.collection("Materia")
            .document(materiaId)
            .get()
            .addOnSuccessListener { snap ->
                if (!snap.exists()) {
                    callback(null)
                    return@addOnSuccessListener
                }
                // Campos básicos
                val id           = snap.id
                val nombre       = snap.getString("nombre")
                val semestre     = snap.getString("semestre")
                val cicloEscolar = snap.getString("cicloEscolar")
                val activo       = snap.getBoolean("activo") ?: true

                // DocumentReference → path
                val calRef = snap.get("calificaciones")
                val calPath = when (calRef) {
                    is DocumentReference -> calRef.path
                    is String            -> calRef
                    else                 -> null
                }

                val materia = Materia(
                    id           = id,
                    nombre       = nombre,
                    semestre     = semestre,
                    cicloEscolar = cicloEscolar,
                    calificaciones = calPath,
                    activo       = activo
                )
                callback(materia)
            }
            .addOnFailureListener {
                callback(null)
            }
    }
}
