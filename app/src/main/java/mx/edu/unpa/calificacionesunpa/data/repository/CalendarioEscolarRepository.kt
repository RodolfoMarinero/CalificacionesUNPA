package mx.edu.unpa.calificacionesunpa.data.repository

import mx.edu.unpa.calificacionesunpa.data.api.calendarioescolar.CalendarioEscolarAPI
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class CalendarioEscolarRepository  @Inject constructor(
    private val api: CalendarioEscolarAPI
)  {
     suspend fun getFile(id: String): ByteArray? {
         return try {
             val response = api.getCalendarioPdf(id)
             if (response.isSuccessful) {
                 response.body()?.bytes()
             } else {
                 null
             }
         } catch (e: Exception) {
             e.printStackTrace()
             null
         }
    }
}