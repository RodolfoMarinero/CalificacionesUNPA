package mx.edu.unpa.calificacionesunpa.data.api.reportes

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Streaming

interface ReportesAPI {

    // Coincide con tu Controller: @GetMapping(value = "/historialacademico")
    @Streaming
    @GET("reportes/historialacademico")
    suspend fun descargarHistorial(
        @Query("matricula") matricula: String
    ): Response<ResponseBody>

    // Coincide con tu Controller: @GetMapping(value = "/constanciaestudios")
    @Streaming
    @GET("reportes/constanciaestudios")
    suspend fun descargarConstancia(
        @Query("matricula") matricula: String
    ): Response<ResponseBody>
}