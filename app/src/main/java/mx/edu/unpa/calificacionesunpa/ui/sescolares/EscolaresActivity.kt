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
import mx.edu.unpa.calificacionesunpa.providers.AuthProvider
import mx.edu.unpa.calificacionesunpa.providers.StorageProvider
import mx.edu.unpa.calificacionesunpa.ui.documentos.ListaDocumentosActivity
import mx.edu.unpa.calificacionesunpa.ui.login.LoginActivity
import java.io.InputStream

class EscolaresActivity : AppCompatActivity() {

    private var uriPDF: Uri? = null

    private lateinit var btnNotificaciones: Button
    private lateinit var btnSeleccionarPdf: Button
    private lateinit var btnCerrarSesion: Button

    private lateinit var btnVerDocs: Button
    private lateinit var txtNombreArchivo: TextView
    private lateinit var cbCalendarioActual: CheckBox

    private lateinit var storageProvider: StorageProvider
    private lateinit var authProvider: AuthProvider

    // Nuevo launcher para seleccionar archivo PDF
    private val activityResultLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val esActual = cbCalendarioActual.isChecked
            uriPDF = it

            val respuesta: (Boolean) -> Unit = { exito ->
                val mensaje = if (exito) {
                    "Calendario subido con éxito"
                } else {
                    "No se pudo subir el archivo"
                }
                Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show()
            }

            storageProvider.uploadFile(
                convertirA_Base64(it),
                obtenerNombreArchivo(it),
                esActual,
                respuesta
            )
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.attivity_escolares)

        cbCalendarioActual = findViewById(R.id.cbCalendarioActual)
        btnNotificaciones = findViewById(R.id.btnNotificaciones)
        btnSeleccionarPdf = findViewById(R.id.btnSeleccionarPdf)
        btnCerrarSesion = findViewById(R.id.btnCerrarSesion)
        btnVerDocs = findViewById(R.id.btnVerDocumentos)
        txtNombreArchivo = findViewById(R.id.txtNombreArchivo)

        storageProvider = StorageProvider()
        authProvider = AuthProvider()

        btnCerrarSesion.setOnClickListener {
            authProvider.exitSession()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        btnNotificaciones.setOnClickListener {
            startActivity(Intent(this, notificaciones_escolares::class.java))
        }

        btnSeleccionarPdf.setOnClickListener {
            activityResultLauncher.launch("application/pdf")
        }

        btnVerDocs.setOnClickListener {
            startActivity(Intent(this, ListaDocumentosActivity::class.java))
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
