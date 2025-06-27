package mx.edu.unpa.calificacionesunpa.service

import android.util.Base64
import mx.edu.unpa.calificacionesunpa.providers.StorageProvider

class CalendarioService(private val storageProvider: StorageProvider) {

    private var cache: ByteArray? = null

    /**
     * Obtiene el PDF del calendario escolar en formato ByteArray.
     * Usa cache para evitar múltiples lecturas si ya se ha cargado antes.
     */
    fun obtenerCalendarioPdf(
        onSuccess: (ByteArray) -> Unit,
        onError: (String) -> Unit
    ) {
        // Usa cache si ya fue cargado anteriormente
        cache?.let {
            onSuccess(it)
            return
        }

        // Solicita el archivo a Firestore usando StorageProvider
        storageProvider.getSeleccionado { base64 ->
            if (!base64.isNullOrEmpty()) {
                try {
                    val pdfBytes = Base64.decode(base64, Base64.DEFAULT)
                    cache = pdfBytes
                    onSuccess(pdfBytes)
                } catch (e: Exception) {
                    onError("Error al decodificar el PDF: ${e.message}")
                }
            } else {
                onError("No se pudo obtener el calendario.")
            }
        }
    }

    /**
     * Limpia la caché del calendario escolar.
     */
    fun limpiarCache() {
        cache = null
    }
}
