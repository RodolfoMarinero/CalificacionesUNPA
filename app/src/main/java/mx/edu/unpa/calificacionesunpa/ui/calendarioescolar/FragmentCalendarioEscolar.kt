package mx.edu.unpa.calificacionesunpa.ui.calendarioescolar

import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.github.barteksc.pdfviewer.PDFView
import com.google.firebase.firestore.FirebaseFirestore
import mx.edu.unpa.calificacionesunpa.R

class FragmentCalendarioEscolar : Fragment() {

    private lateinit var pdfView: PDFView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_calendario_escolar, container, false)

        pdfView = view.findViewById(R.id.pdfView)

        obtenerYMostrarPdfDesdeFirestore("")

        return view
    }

    private fun obtenerYMostrarPdfDesdeFirestore(fileName: String) {
        val db = FirebaseFirestore.getInstance()
        val docId=fileName
        db.collection("storage").document(docId)
            .get()
            .addOnSuccessListener { document ->
                val base64 = document.getString("base64")
                if (base64 != null) {
                    val pdfBytes = Base64.decode(base64, Base64.DEFAULT)
                    val inputStream = pdfBytes.inputStream()
                    pdfView.fromStream(inputStream)
                        .enableSwipe(true)
                        .swipeHorizontal(false)
                        .enableDoubletap(true)
                        .load()
                } else {
                    Toast.makeText(context, "No se encontró el PDF", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }
}
