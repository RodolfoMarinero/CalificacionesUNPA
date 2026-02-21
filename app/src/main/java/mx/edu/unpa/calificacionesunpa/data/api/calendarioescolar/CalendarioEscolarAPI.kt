package mx.edu.unpa.calificacionesunpa.data.api.calendarioescolar

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface CalendarioEscolarAPI {

    //funcionando con api rest de spring local host
    @GET("/reportes/calendarioescolar")
    suspend fun getCalendarioPdf(
        @Query("ciclo") ciclo: String
    ): Response<ResponseBody>


}