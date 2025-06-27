package mx.edu.unpa.calificacionesunpa.ui.documentos

import android.os.Bundle
import android.widget.Toast
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import mx.edu.unpa.calificacionesunpa.R
import mx.edu.unpa.calificacionesunpa.adapters.DocumentosAdapter
import mx.edu.unpa.calificacionesunpa.models.PdfData
import mx.edu.unpa.calificacionesunpa.providers.StorageProvider

class ListaDocumentosActivity : AppCompatActivity() {

    private lateinit var rv: RecyclerView
    private var documentos: MutableList<PdfData> = mutableListOf()

    private val storageProvider = StorageProvider()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_documentos)
        rv = findViewById(R.id.rvDocumentos)
        rv.layoutManager = LinearLayoutManager(this)
        findViewById<Button>(R.id.btnBack).setOnClickListener {
            finish()
        }
        cargarDocumentos()
    }

    private fun cargarDocumentos() {
        storageProvider.fetchPdfList { lista ->
            if (lista != null) {
                documentos = lista.toMutableList()
                rv.adapter = DocumentosAdapter(documentos) { meta ->
                    marcarComoActual(meta.id)
                }
            } else {
                Toast.makeText(this, "Error al cargar documentos", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun marcarComoActual(fileId: String) {
        storageProvider.selectPdf(fileId) {
            if (it) {
                // Actualizar la lista de documentos
                documentos.forEach { doc ->
                    doc.seleccionado = doc.id == fileId
                }
                rv.adapter?.notifyDataSetChanged()
                Toast.makeText(this, "Documento seleccionado", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Error al seleccionar documento", Toast.LENGTH_LONG).show()
            }
        }
    }
}
