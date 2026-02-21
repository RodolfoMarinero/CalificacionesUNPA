package mx.edu.unpa.calificacionesunpa.utils

import android.content.Context
import android.net.Uri
import android.util.Base64
import java.io.File
import java.io.InputStream

object ArchivoUtils {
    fun convertirA_Base64(context: Context, uri: Uri): String? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val bytes = inputStream?.readBytes()
            inputStream?.close()
            bytes?.let { Base64.encodeToString(it, Base64.NO_WRAP) }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun guardarBase64ComoArchivo(base64: String, nombreArchivo: String, context: Context): Uri? {
        return try {
            val bytes = Base64.decode(base64, Base64.DEFAULT)
            val file = File(context.cacheDir, nombreArchivo)
            file.writeBytes(bytes)
            Uri.fromFile(file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

}