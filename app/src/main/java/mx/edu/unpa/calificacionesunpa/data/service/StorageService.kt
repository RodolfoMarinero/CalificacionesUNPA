package mx.edu.unpa.calificacionesunpa.data.service

import android.util.Base64
import mx.edu.unpa.calificacionesunpa.data.api.StorageApi
import mx.edu.unpa.calificacionesunpa.models.PdfData
import mx.edu.unpa.calificacionesunpa.models.UploadRequest

class StorageService(private val api: StorageApi) {

    //private val sharedPrefs = context.getSharedPreferences("storage_prefs", Context.MODE_PRIVATE)

    // ---------------- Perfil de usuario ----------------
    suspend fun uploadProfileImage(userId: String, imageB64: String): Boolean {
        return try {
            val response = api.uploadFile(
                UploadRequest(
                    userId = userId,
                    fileName = "profile_$userId",
                    base64 = imageB64,
                    seleccionado = false
                )
            )

            if (response.success) {
                saveToLocalCache(imageB64, userId)
            }
            response.success
        } catch (e: Exception) {
            false
        }
    }




    suspend fun getProfileImage(userId: String): String? {
        return try {
            api.getFile("profile_$userId").base64
        } catch (e: Exception) {
            null
        }
    }

    private fun saveToLocalCache(base64: String, userId: String) {
       // sharedPrefs.edit().putString("profile_$userId", base64).apply()
       // sharedPrefs.edit().putLong("last_update_$userId", System.currentTimeMillis()).apply()
    }

    // ---------------- PDF ----------------
    suspend fun uploadFile(fileB64: String?, fileName: String, seleccionado: Boolean): Boolean {
        return try {
            api.uploadFile(
                UploadRequest(
                    userId = "", // o el id del usuario actual
                    fileName = fileName,
                    base64 = fileB64,
                    seleccionado = seleccionado
                )
            ).success
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getSeleccionado(): ByteArray? {
        return try {
            val base64 =api.getSeleccionado()?.base64
            base64?.let { Base64.decode(it, Base64.DEFAULT) }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun fetchPdfList(): List<PdfData>? {
        return try {
            api.fetchPdfList().pdfs
        } catch (e: Exception) {
            null
        }
    }

    suspend fun selectPdf(pdfId: String): Boolean {
        return try {
            api.selectPdf(pdfId).success
        } catch (e: Exception) {
            false
        }
    }

    suspend fun resetSelected(): Boolean {
        return try {
            api.resetSelected().success
        } catch (e: Exception) {
            false
        }
    }




}