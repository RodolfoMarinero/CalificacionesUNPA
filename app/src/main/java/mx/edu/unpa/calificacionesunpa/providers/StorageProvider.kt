package mx.edu.unpa.calificacionesunpa.providers

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.app
import java.util.*

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
                prefs.edit().putLong("last_update", System.currentTimeMillis()).apply()
            }
            .addOnFailureListener {
                onResult(false)
            }
    }
    interface OnResultCallback {
        fun onResult(base64: String?)
    }
    fun uploadFile(fileB64: String, fileName: String, onResult: (Boolean) -> Unit) {
        val data = hashMapOf(
            "fileName" to fileName,
            "base64" to fileB64,
            "timestamp" to Date()
        )

        // Buscar si ya existe un archivo con ese fileName
        collection.whereEqualTo("fileName", fileName).get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    // Ya existe: actualizar el primer documento encontrado
                    val docId = fileName
                    collection.document(docId).set(data)
                        .addOnSuccessListener { onResult(true) }
                        .addOnFailureListener { onResult(false) }
                } else {
                    // No existe: crear nuevo
                    collection.add(data)
                        .addOnSuccessListener { onResult(true) }
                        .addOnFailureListener { onResult(false) }
                }
            }
            .addOnFailureListener { onResult(false) }
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

}
