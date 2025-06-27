package mx.edu.unpa.calificacionesunpa.providers

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.app
import mx.edu.unpa.calificacionesunpa.ui.calificacionesanteriores.OnResultCallback
import java.util.*
import androidx.core.content.edit
import mx.edu.unpa.calificacionesunpa.models.PdfData

class StorageProvider {

    private val firestore = FirebaseFirestore.getInstance()
    private val collection = firestore.collection("storage")


    fun uploadImage(imageB64: String, imageName: String, userId: String, onResult: (Boolean) -> Unit) {
        val data = hashMapOf(
            "userId" to userId,
            "fileName" to imageName,
            "base64" to imageB64,
            "timestamp" to Date()
        )


        collection.document(userId).set(data)
            .addOnSuccessListener {
                onResult(true)
                // Actualizar último timestamp de modificación
                val prefs = Firebase.app.getApplicationContext()
                    .getSharedPreferences("profile_prefs", Context.MODE_PRIVATE)
                prefs.edit { putLong("last_update", System.currentTimeMillis()) }
            }
            .addOnFailureListener {
                onResult(false)
            }
    }

    fun uploadFile(
        fileB64: String?,
        fileName: String,
        seleccionado: Boolean,
        onResult: (Boolean) -> Unit
    ) {
        // 1) Null-check inmediato
        if (fileB64 == null) {
            onResult(false)
            return
        }

        // 2) Preparar data
        val data = hashMapOf(
            "base64" to fileB64,
            "seleccionado" to seleccionado,
            "tipo" to "pdf",
            "timestamp" to Date()
        )

        // 3) Si hay que desmarcar antes, hacerlo y luego subir
        if (seleccionado) {
            resetSelected { resetOk ->
                if (resetOk) {
                    collection.document(fileName)
                        .set(data)
                        .addOnSuccessListener { onResult(true) }
                        .addOnFailureListener { onResult(false) }
                } else {
                    onResult(false)
                }
            }
        } else {
            // 4) Si NO está marcado, solo sube el documento sin resetear nada
            collection.document(fileName)
                .set(data)
                .addOnSuccessListener { onResult(true) }
                .addOnFailureListener { onResult(false) }
        }
    }

    fun getFile(fileId: String, onResult: (String?) -> Unit) {
        collection.document(fileId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val base64 = document.getString("base64")
                    onResult(base64)
                } else {
                    onResult(null)
                }
            }
            .addOnFailureListener {
                onResult(null)
            }
    }
    fun getSeleccionado(onResult: (String?) -> Unit) {
        collection
            .whereEqualTo("seleccionado", true)
            .limit(1)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val document = querySnapshot.documents[0]
                    val base64 = document.getString("base64")
                    onResult(base64)
                } else {
                    onResult(null)
                }
            }
            .addOnFailureListener {
                onResult(null)
            }
    }

    fun getImageByUserId(userId: String, onResult: OnResultCallback) {
        collection.document(userId).get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val base64 = documentSnapshot.getString("base64")
                    onResult.onResult(base64)
                } else {
                    onResult.onResult(null)
                }
            }
            .addOnFailureListener {
                onResult.onResult(null)
            }
    }
    fun resetSelected(onResult: (Boolean) -> Unit) {
        collection
            .whereEqualTo("tipo", "pdf")
            .whereEqualTo("seleccionado", true)
            .get()
            .addOnSuccessListener { snapshot ->
                val batch = firestore.batch()
                for (doc in snapshot.documents) {
                    batch.update(doc.reference, "seleccionado", false)
                }
                batch.commit()
                    .addOnSuccessListener { onResult(true) }
                    .addOnFailureListener { onResult(false) }
            }
            .addOnFailureListener {
                onResult(false)
            }
    }

    fun fetchPdfList(onResult: (List<PdfData>?) -> Unit) {
        collection
            .whereEqualTo("tipo", "pdf")
            .orderBy("timestamp")  // asegúrate de que exista índice compuesto en Firestore si te lo pide
            .get()
            .addOnSuccessListener { snapshot ->
                val lista = snapshot.documents.map { doc ->
                    PdfData(
                        id = doc.id,
                        timestamp = doc.getDate("timestamp") ?: Date(0),
                        seleccionado = doc.getBoolean("seleccionado") ?: false
                    )
                }
                onResult(lista)
            }
            .addOnFailureListener { e ->
                // Loguea el error para ver el mensaje concreto
                Log.e("StorageProvider", "Error fetching PDF list", e)
                onResult(null)
            }
    }
    fun selectPdf(pdfId: String, onResult: (Boolean) -> Unit) {
        resetSelected { resetOk ->
            if (resetOk) {
                collection.document(pdfId)
                    .update("seleccionado", true)
                    .addOnSuccessListener {
                        onResult(true)
                    }
                    .addOnFailureListener {
                        onResult(false)
                    }
            }else{
                onResult(false)
            }
        }
    }

}
