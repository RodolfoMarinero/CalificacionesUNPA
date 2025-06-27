package mx.edu.unpa.calificacionesunpa.providers

import android.util.Log
import android.view.View
import com.google.firebase.firestore.FirebaseFirestore
import mx.edu.unpa.calificacionesunpa.adapters.NotificationItem
import mx.edu.unpa.calificacionesunpa.service.NotificacionesService
import mx.edu.unpa.calificacionesunpa.service.UsuarioService
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class NotificacionProvider {
    private val db = FirebaseFirestore.getInstance()
    init {
        Log.d("Notificaciones", "Inicializando NotificacionesProvider")
        Log.d("Notificaciones", "comenzando eliminar notificaciones expiradas")
        eliminarNotificacionesExpiradas()
    }
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
    private fun eliminarNotificacionesExpiradas() {
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE
        val hoy = LocalDate.now()

        db.collection("notificaciones")
            .get()
            .addOnSuccessListener { documents ->
                val batch = db.batch()
                var cambios = false

                for (doc in documents) {
                    val data = doc.toObject(NotificationItem::class.java)
                    val fechaExpira = try {
                        LocalDate.parse(data.expiraEn, formatter)
                    } catch (e: Exception) {
                        Log.e("Notificaciones", "Fecha mal formateada: ${data.expiraEn}")
                        continue
                    }

                    if (fechaExpira.isBefore(hoy) || fechaExpira.isEqual(hoy)) {
                        batch.delete(doc.reference)
                        cambios = true
                    }
                }

                if (cambios) {
                    batch.commit()
                        .addOnSuccessListener {
                            Log.d("Notificaciones", "Notificaciones expiradas eliminadas (en init)")
                        }
                        .addOnFailureListener { e ->
                            Log.e("Notificaciones", "Error al eliminar notificaciones expiradas", e)
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error al consultar notificaciones (init)", e)
            }
    }

}