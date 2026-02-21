package mx.edu.unpa.calificacionesunpa.data.api.alumno

import mx.edu.unpa.calificacionesunpa.data.model.AlumnoResponse
import mx.edu.unpa.calificacionesunpa.data.model.ApiResponse
import mx.edu.unpa.calificacionesunpa.data.model.PrimerAccesoResponse
import mx.edu.unpa.calificacionesunpa.models.Calendario
import retrofit2.http.GET
import retrofit2.http.Path

interface AlumnoAPIretrofit {
    @GET("alumnos/usuario/{usuarioId}/completo")
    suspend fun getAlumnoConMaterias(@Path("usuarioId") usuarioId: String): AlumnoResponse

    @GET("usuarios/{usuarioId}/primer-acceso")
    suspend fun getPrimerAcceso(@Path("usuarioId") usuarioId: String): ApiResponse<PrimerAccesoResponse>



    @GET("examenes/materia/{materiaId}")
    suspend fun getExamenPorMateria(@Path("materiaId") materiaId: String): ApiResponse<Calendario>
}