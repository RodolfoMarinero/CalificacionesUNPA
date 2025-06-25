package mx.edu.unpa.calificacionesunpa.providers

class CalendarioProvider {

    private val firestore = FirebaseFirestore.getInstance()

    fun obtenerCalendarioBase64(
        idDocumento: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        firestore.collection("storage").document(idDocumento)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val base64 = document.getString("base64")
                    if (!base64.isNullOrEmpty()) {
                        onSuccess(base64)
                    } else {
                        onError("El PDF está vacío")
                    }
                } else {
                    onError("No se encontró el documento")
                }
            }
            .addOnFailureListener {
                onError("Error: ${it.message}")
            }
    }
}
