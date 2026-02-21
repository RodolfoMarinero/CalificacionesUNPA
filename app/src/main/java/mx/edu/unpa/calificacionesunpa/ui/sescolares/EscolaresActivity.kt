package mx.edu.unpa.calificacionesunpa.ui.sescolares

//import mx.edu.unpa.calificacionesunpa.api.RetrofitClient
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Base64
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import mx.edu.unpa.calificacionesunpa.R
import mx.edu.unpa.calificacionesunpa.data.repository.LoginRepository
import mx.edu.unpa.calificacionesunpa.data.repository.StorageRepository
import mx.edu.unpa.calificacionesunpa.ui.login.LoginActivity
import mx.edu.unpa.calificacionesunpa.ui.notificaciones.notificaciones_escolares
import java.io.InputStream

class EscolaresActivity : AppCompatActivity() {

    private var uriPDF: Uri? = null

    private lateinit var btnNotificaciones: Button
    private lateinit var btnSeleccionarPdf: Button
    private lateinit var btnCerrarSesion: Button

    private lateinit var btnVerDocs: Button
    private lateinit var txtNombreArchivo: TextView
    private lateinit var cbCalendarioActual: CheckBox

    private lateinit var storageProvider: StorageRepository
    private lateinit var authProvider: LoginRepository

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

            lifecycleScope.launch {
                val exito = storageProvider.uploadFile(
                    convertirA_Base64(it),
                    obtenerNombreArchivo(it),
                    esActual
                )

                if (exito) {
                    Toast.makeText(this@EscolaresActivity, "Archivo subido correctamente", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this@EscolaresActivity, "Error al subir el archivo", Toast.LENGTH_LONG).show()
                }
            }
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




        val token = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxODA4MDA2OCIsImlhdCI6MTc1NjMxNjI2MSwiZXhwIjoxNzU2MzUyMjYxfQ.bR6CliCRb4wnXSoRaZJJ5GFiHpl-X43tmuZlrZgm3Xu8BKI41FXRqQBTrKYL0jUCJywlNtHjIoe5kL7d-kSF9Q"
        //val api = RetrofitClient.create(token)
        //storageProvider = StorageRepository(CalendarioEscolarService(api),this)

        //authProvider = LoginRepository(this, AuthApiMock())

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
            //startActivity(Intent(this, ListaDocumentosActivity::class.java))
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
