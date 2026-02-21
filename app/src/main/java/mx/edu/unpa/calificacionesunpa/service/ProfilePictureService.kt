package mx.edu.unpa.calificacionesunpa.service


import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mx.edu.unpa.calificacionesunpa.data.repository.StorageRepository
import mx.edu.unpa.calificacionesunpa.utils.ArchivoUtils
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfilePictureService @Inject constructor(context: Context, private val repository : StorageRepository) {

    private val appContext = context.applicationContext
    //private  val repository : StorageRepository

    //private val repository=repository
    val token = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxODA4MDA2OCIsImlhdCI6MTc1NjMxNjI2MSwiZXhwIjoxNzU2MzUyMjYxfQ.bR6CliCRb4wnXSoRaZJJ5GFiHpl-X43tmuZlrZgm3Xu8BKI41FXRqQBTrKYL0jUCJywlNtHjIoe5kL7d-kSF9Q"
    //val api = RetrofitClient.create(token)
    //val calendarioEscolarAPI= CalendarioEscolarAPI()
    //val calendarioEscolarService= CalendarioEscolarService(StorageApi())

    //private val storageRepositoryr = StorageRepository(CalendarioEscolarService(), context)
    private val sharedPrefs = appContext.getSharedPreferences("profile_prefs", Context.MODE_PRIVATE)

    /*companion object {
        @Volatile
        private var INSTANCE: ProfilePictureService? = null

        fun getInstance(context: Context,repository : StorageRepository): ProfilePictureService {
            return INSTANCE ?: synchronized(this) {
                val instance = ProfilePictureService(context,repository)
                INSTANCE = instance
                instance
            }
        }
    }*/

     fun saveProfilePicture(context: Context, uri: Uri, userId: String, callback: OnBooleanResultCallback) {
        Thread {
            try {
                // Convertir a base64
                val base64 = ArchivoUtils.convertirA_Base64(context.applicationContext, uri)
                if (base64 == null) {
                    callback.onResult(false)
                    return@Thread
                }

                // Subir a Firestore
                CoroutineScope(Dispatchers.IO).launch {
                    val success = try {

                        repository.uploadFile(
                            fileB64 = base64,
                            fileName = "profile_$userId",
                            seleccionado = true // o false según tu lógica
                        )
                    } catch (e: Exception) {
                        false
                    }

                    withContext(Dispatchers.Main) {
                        if (success) {
                            saveToLocalCache(base64, userId)
                            sharedPrefs.edit().putLong("last_update", System.currentTimeMillis()).apply()
                        }
                        callback.onResult(success)
                    }
                }
            } catch (e: Exception) {
                callback.onResult(false)
            }
        }.start()
    }

    fun getProfilePicture(userId: String, callback: OnBitmapResultCallback) {
        // 1. Intentar desde caché
        val cached = getFromLocalCache(userId)
        if (cached != null) {
            callback.onResult(cached)
            return
        }

        // 2. Buscar en Firestore
       /* storageProvider.getImageByUserId(userId, object : StorageProvider.OnResultCallback {
            override fun onResult(base64: String?) {
                val bitmap = base64?.let {
                    convertBase64ToBitmap(it)?.also {
                        saveToLocalCache(base64, userId)
                    }
                }
                callback.onResult(bitmap)
            }
        })*/
    }

    fun shouldRefreshProfile(): Boolean {
        val lastUpdate = sharedPrefs.getLong("last_update", 0)
        return System.currentTimeMillis() - lastUpdate > 60000 // 1 minuto
    }

    private fun saveToLocalCache(base64: String, userId: String) {
        try {
            val file = File(appContext.filesDir, "profile_$userId.png")
            val bitmap = convertBase64ToBitmap(base64)
            FileOutputStream(file).use { stream ->
                bitmap?.compress(Bitmap.CompressFormat.PNG, 100, stream)
            }
        } catch (e: Exception) {
            // Manejar error
        }
    }

    private fun getFromLocalCache(userId: String): Bitmap? {
        val file = File(appContext.filesDir, "profile_$userId.png")
        return if (file.exists()) {
            BitmapFactory.decodeFile(file.absolutePath)
        } else {
            null
        }
    }

    private fun convertBase64ToBitmap(base64: String): Bitmap? {
        return try {
            val decodedBytes = android.util.Base64.decode(base64, android.util.Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: Exception) {
            null
        }
    }
}

interface OnResultCallback {
    fun onResult(base64: String?)
}
interface OnBitmapResultCallback {
    fun onResult(bitmap: Bitmap?)
}
interface OnBooleanResultCallback {
    fun onResult(success: Boolean)
}