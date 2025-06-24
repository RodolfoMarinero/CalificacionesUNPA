package mx.edu.unpa.calificacionesunpa.providers

import android.util.Log
import android.view.View
import com.google.firebase.firestore.FirebaseFirestore
import mx.edu.unpa.calificacionesunpa.adapters.NotificationItem
import mx.edu.unpa.calificacionesunpa.service.NotificacionesService
import mx.edu.unpa.calificacionesunpa.service.UsuarioService

class NotificacionProvider {
    private val db = FirebaseFirestore.getInstance()
    // private val alumnoActual = UsuarioService.alumnoActual?.matricula
    var alumnoActual = "";

    fun cargarNotificacionesDesdeFirestore() {

        alumnoActual = UsuarioService.alumnoActual?.matricula.toString();

        val lista: MutableList<NotificationItem> = mutableListOf() // MutableList en vez de List
        db.collection("notificaciones")
            .orderBy("timestamp")
            .get()
            .addOnSuccessListener { documents ->
                lista.clear()  // Ahora puedes limpiar la lista
                for (doc in documents) {
                    val notificacion = doc.toObject(NotificationItem::class.java)

                    notificacion.destinatarios.forEach {
                        // Log.d("Firestore", "Destinatario: $it")
                        if (alumnoActual == it || it == "11111111") {
                            lista.add(notificacion) // Añadir notificación a la lista mutable
                        }
                    }
                }
                NotificacionesService.listaNotificaciones = lista
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error al cargar notificaciones", e)
            }
    }

    fun enviarNotificacion(notificacion : NotificationItem){
        db.collection("notificaciones").document()
            .set(notificacion)
    }
}