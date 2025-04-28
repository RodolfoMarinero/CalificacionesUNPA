// MateriaProvider.kt
package mx.edu.unpa.calificacionesunpa.providers

import com.google.firebase.firestore.FirebaseFirestore
import mx.edu.unpa.calificacionesunpa.models.Materia

class MateriaProvider {
    private val db = FirebaseFirestore.getInstance()

    /**
     * Trae el documento 'Materia' con ID = materiaId.
     * Callback recibe null si no existe o hay error.
     */
    fun getMateriaById(materiaId: String, callback: (Materia?) -> Unit) {
        db.collection("Materia")
            .document(materiaId)
            .get()
            .addOnSuccessListener { snap ->
                callback(snap.toObject(Materia::class.java))
            }
            .addOnFailureListener {
                callback(null)
            }
    }
}
