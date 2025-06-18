package mx.edu.unpa.calificacionesunpa.ui.sescolares

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Base64
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import mx.edu.unpa.calificacionesunpa.R
import mx.edu.unpa.calificacionesunpa.notificaciones_escolares
import java.io.InputStream

class EscolaresActivity : AppCompatActivity() {

    private var uriPDF: Uri? = null

    private lateinit var btnNotificaciones: Button
    private lateinit var btnSeleccionarPdf: Button
    private lateinit var btnConvertir: Button
    private lateinit var txtNombreArchivo: TextView

    // Nuevo launcher para seleccionar archivo PDF
    private val activityResultLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            uriPDF = it
            btnConvertir.isEnabled = true
            txtNombreArchivo.text = "Seleccionado: ${obtenerNombreArchivo(it)}"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.attivity_escolares)

        btnNotificaciones = findViewById(R.id.btnNotificaciones)
        btnSeleccionarPdf = findViewById(R.id.btnSeleccionarPdf)
        btnConvertir = findViewById(R.id.btnConvertir)
        txtNombreArchivo = findViewById(R.id.txtNombreArchivo)

        btnConvertir.isEnabled = false

        btnNotificaciones.setOnClickListener {
            startActivity(Intent(this, notificaciones_escolares::class.java))
        }

        btnSeleccionarPdf.setOnClickListener {
            activityResultLauncher.launch("application/pdf")
        }

        btnConvertir.setOnClickListener {
            uriPDF?.let {
                val base64 = convertirA_Base64(it)
                if (base64 != null) {
                    Toast.makeText(this, "Convertido (longitud: ${base64.length})", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Error al convertir archivo", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun obtenerNombreArchivo(uri: Uri): String {
        var nombre = "archivo.pdf"
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (it.moveToFirst() && index >= 0) {
                nombre = it.getString(index)
            }
        }
        return nombre
    }

    private fun convertirA_Base64(uri: Uri): String? {
        return try {
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            val bytes = inputStream?.readBytes()
            inputStream?.close()
            bytes?.let { Base64.encodeToString(it, Base64.NO_WRAP) }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
