package mx.edu.unpa.calificacionesunpa.providers

import android.util.Log
import android.view.View
import com.google.firebase.firestore.FirebaseFirestore
import mx.edu.unpa.calificacionesunpa.adapters.NotificationItem
import mx.edu.unpa.calificacionesunpa.service.NotificacionesService
import mx.edu.unpa.calificacionesunpa.service.UsuarioService

class NotificacionProvider {
    private val db = FirebaseFirestore.getInstance()

    fun cargarNotificacionesDesdeFirestore(onSuccess: (List<NotificationItem>) -> Unit) {
        val alumnoActual = UsuarioService.alumnoActual?.matricula.toString()
        val lista: MutableList<NotificationItem> = mutableListOf()

        db.collection("notificaciones")
            .orderBy("timestamp")
            .get()
            .addOnSuccessListener { documents ->
                lista.clear()
                for (doc in documents) {
                    val notificacion = doc.toObject(NotificationItem::class.java)
                    if (notificacion.destinatarios.contains(alumnoActual) || notificacion.destinatarios.contains("11111111")) {
                        lista.add(notificacion)
                    }
                }
                onSuccess(lista)
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