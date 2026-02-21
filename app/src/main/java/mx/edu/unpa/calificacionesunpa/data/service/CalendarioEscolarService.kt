package mx.edu.unpa.calificacionesunpa.data.service

import mx.edu.unpa.calificacionesunpa.data.api.calendarioescolar.CalendarioEscolarAPI
import javax.inject.Inject
import javax.inject.Singleton


class CalendarioEscolarService @Inject constructor(private val api: CalendarioEscolarAPI) {

    private var cache: ByteArray? = null
    suspend fun descargarCalendario(ciclo: String): ByteArray? {
        return try {
            val response = api.getCalendarioPdf(ciclo)
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