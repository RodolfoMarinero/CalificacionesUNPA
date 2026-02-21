package mx.edu.unpa.calificacionesunpa.data.repository

import mx.edu.unpa.calificacionesunpa.data.api.avisos.NotificacionAPI
import mx.edu.unpa.calificacionesunpa.data.model.NotificationItem
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificacionRepository @Inject constructor(
    private val api: NotificacionAPI
)  {

    suspend fun obtenerNotificaciones(alumnoId: String): Result<List<NotificationItem>> {
        return try {
            val response = api.cargarNotificaciones(alumnoId) // ApiResponse<List<NotificationItem>>

            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.success(emptyList()) // O Result.failure(Exception(response.message))
            }

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun enviarNotificacion(notificacion: NotificationItem): Result<Boolean> {
        return try {
            val response = api.enviarNotificacion(notificacion)
            if (response.success) {
                Result.success(true)
            } else {
                Result.failure(Exception(response.message ?: "Error al enviar la notificación"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /*
    suspend fun eliminarNotificacionesExpiradas(hastaFecha: String): Result<Int> {
        return try {
            val response = api.eliminarNotificacionesExpiradas(hastaFecha)
            if (response.success && response.data != null) {
                Result.success(response.data.eliminadas)
            } else {
                Result.success(0)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }*/

    suspend fun marcarNotificacionComoLeida(notificacionId: String): Result<Boolean> {
        return try {
            val response = api.marcarComoLeida(notificacionId)
            if (response.success) {
                Result.success(true)
            } else {
                Result.failure(Exception(response.message ?: "Error al marcar como leída"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun obtenerContadorNoLeidas(alumnoId: String): Result<Int> {
        return try {
            val response = api.obtenerContadorNoLeidas(alumnoId)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.success(0)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun eliminarNotificacion(notificacionId: String): Result<Boolean> {
        return try {
            val response = api.eliminarNotificacion(notificacionId)
            if (response.success) {
                Result.success(true)
            } else {
                Result.failure(Exception(response.message ?: "Error al eliminar la notificación"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}
