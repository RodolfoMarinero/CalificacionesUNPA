package mx.edu.unpa.calificacionesunpa.data.api

import mx.edu.unpa.calificacionesunpa.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface TutorAPI {

    @GET("api/movil/tutor/ventana-abierta")
    suspend fun verificarVentana(
        @Query("periodo") periodo: String
    ): Response<VentanaSolicitud>

    @GET("api/movil/tutor/ventana-abierta-test")
    suspend fun verificarVentanaTest(
        @Query("periodo") periodo: String
    ): Response<VentanaSolicitud>
    @GET("api/movil/tutor/mi-tutor")
    suspend fun obtenerMiTutor(
        @Query("matricula") matricula: String,
        @Query("periodo") periodo: String
    ): Response<TutorAsignado>

    @GET("api/movil/tutor/docentes-disponibles")
    suspend fun obtenerDocentesDisponibles(
        @Query("matricula") matricula: String,
        @Query("periodo") periodo: String
    ): Response<List<DocenteDisponible>>

    @GET("api/movil/tutor/historial")
    suspend fun obtenerHistorial(
        @Query("matricula") matricula: String
    ): Response<List<HistorialTutor>>

    @POST("api/movil/tutor/solicitar")
    suspend fun solicitarTutor(
        @Body request: SolicitudTutorRequest
    ): Response<SolicitudResponse>

    @PUT("api/movil/tutor/actualizar-correo")
    suspend fun actualizarCorreo(
        @Body request: ActualizarCorreoRequest
    ): Response<ActualizarCorreoResponse>
}