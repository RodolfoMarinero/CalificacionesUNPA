package mx.edu.unpa.calificacionesunpa.service
/*
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.unpa.calificaciones.MainActivity
import com.unpa.calificaciones.R
import kotlin.apply
import kotlin.jvm.java
import kotlin.text.toBoolean
import kotlin.to

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val titulo = remoteMessage.notification?.title ?: remoteMessage.data["titulo"] ?: "Sin título"
        val mensaje = remoteMessage.notification?.body ?: remoteMessage.data["mensaje"] ?: "Sin mensaje"
        val esGlobal = remoteMessage.data["esGlobal"]?.toBoolean() ?: false

        showNotification(titulo, mensaje)
        guardarNotificacionEnFirestore(titulo, mensaje, esGlobal)
    }

    override fun onNewToken(token: String) {
        Log.i("FCM", "Refreshed token: $token")
    }

    private fun showNotification(title: String?, message: String?) {
        val channelId = "default_channel_id"
        val notificationManager = ContextWrapper.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Notificaciones",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            Intent.setFlags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.notification)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun guardarNotificacionEnFirestore(titulo: String, mensaje: String, esGlobal: Boolean) {
        val db = FirebaseFirestore.getInstance()
        val matricula = UsuarioService.alumnoActual?.matricula

        if (!esGlobal && matricula == null) return

        val notificacion = hashMapOf(
            "titulo" to titulo,
            "mensaje" to mensaje,
            "esGlobal" to esGlobal,
            "destinatarios" to if (esGlobal) emptyList<String>() else listOf(matricula!!),
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("notificaciones")
            .add(notificacion)
            .addOnSuccessListener {
                Log.d("Firestore", "Notificación guardada")
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error al guardar la notificación", e)
            }
    }
}
*/