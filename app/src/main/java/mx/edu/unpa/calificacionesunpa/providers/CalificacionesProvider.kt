package mx.edu.unpa.calificacionesunpa.providers

import com.google.firebase.firestore.FirebaseFirestore
import mx.edu.unpa.calificacionesunpa.models.Calificaciones

class CalificacionesProvider {
    private val db = FirebaseFirestore.getInstance()

    interface CalificacionesCallback {
        fun onCallback(calificaciones: Calificaciones?)
    }

    fun getCalificacionesByPath(path: String, callback: CalificacionesCallback) {
        val parts = path.split("/", limit = 2)
        if (parts.size == 2) {
            db.collection(parts[0])
                .document(parts[1])
                .get()
                .addOnSuccessListener { snap ->
                    callback.onCallback(snap.toObject(Calificaciones::class.java))
                }
                .addOnFailureListener {
                    callback.onCallback(null)
                }
        } else {
            callback.onCallback(null)
        }
    }
}
