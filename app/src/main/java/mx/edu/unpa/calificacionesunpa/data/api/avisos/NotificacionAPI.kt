package mx.edu.unpa.calificacionesunpa.data.api.avisos

import mx.edu.unpa.calificacionesunpa.data.model.ApiResponse
import mx.edu.unpa.calificacionesunpa.data.model.NotificationItem
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface NotificacionAPI {

    @GET("notificaciones/alumno/{alumnoId}")
    suspend fun cargarNotificaciones(
        @Path("alumnoId") alumnoId: String
    ): ApiResponse<List<NotificationItem>>

    @POST("notificaciones")
    suspend fun enviarNotificacion(
        @Body notificacion: NotificationItem
    ): ApiResponse<Boolean>
/*
    @DELETE("notificaciones/eliminar-expiradas")
    suspend fun eliminarNotificacionesExpiradas(
        @Query("fecha") hastaFecha: String
    ): ApiResponse<EliminarExpiradas>
*/
    @PUT("notificaciones/{notificacionId}/leer")
    suspend fun marcarComoLeida(
        @Path("notificacionId") notificacionId: String
    ): ApiResponse<Boolean>

    @GET("notificaciones/alumno/{alumnoId}/no-leidas/count")
    suspend fun obtenerContadorNoLeidas(
        @Path("alumnoId") alumnoId: String
    ): ApiResponse<Int>

    @DELETE("notificaciones/{notificacionId}")
    suspend fun eliminarNotificacion(
        @Path("notificacionId") notificacionId: String
    ): ApiResponse<Boolean>
}
