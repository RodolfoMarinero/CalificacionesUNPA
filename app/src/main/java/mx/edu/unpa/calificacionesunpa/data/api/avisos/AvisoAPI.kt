package mx.edu.unpa.calificacionesunpa.data.api.avisos

import mx.edu.unpa.calificacionesunpa.models.Aviso
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface AvisoAPI {

    @GET("notificaciones")
    suspend fun listarAvisos(): List<Aviso>

    @GET("api/avisos/{id}")
    suspend fun obtenerAviso(@Path("id") id: Int): Aviso

    @POST("api/avisos")
    suspend fun crearAviso(@Body aviso: Aviso): Aviso

    @PUT("api/avisos/{id}")
    suspend fun actualizarAviso(
        @Path("id") id: Int,
        @Body aviso: Aviso
    ): Aviso

    @DELETE("api/avisos/{id}")
    suspend fun eliminarAviso(@Path("id") id: Int)
}
