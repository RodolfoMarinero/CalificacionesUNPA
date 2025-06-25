package mx.edu.unpa.calificacionesunpa.service

class CalendarioService(private val provider: CalendarioProvider) {

    private var calendarioCache: ByteArray? = null

    fun obtenerCalendarioPdf(
        idDocumento: String,
        onSuccess: (ByteArray) -> Unit,
        onError: (String) -> Unit
    ) {
        if (calendarioCache != null) {
            onSuccess(calendarioCache!!)
            return
        }

        provider.obtenerCalendarioBase64(idDocumento,
            onSuccess = { base64 ->
                val pdfBytes = Base64.decode(base64, Base64.DEFAULT)
                calendarioCache = pdfBytes
                onSuccess(pdfBytes)
            },
            onError = { error ->
                onError(error)
            }
        )
    }

    fun limpiarCache() {
        calendarioCache = null
    }
}
