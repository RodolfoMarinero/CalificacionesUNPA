package mx.edu.unpa.calificacionesunpa.ui.calendarioescolar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.github.barteksc.pdfviewer.PDFView
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import mx.edu.unpa.calificacionesunpa.R
import java.io.File

class FragmentCalendarioEscolar : Fragment() {

    private lateinit var pdfView: PDFView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_calendario_escolar, container, false)
        pdfView = view.findViewById(R.id.pdfView)
        cargarPdfDesdeFirebase()
        return view
    }

    private fun cargarPdfDesdeFirebase() {
        val storageRef = Firebase.storage.reference
        val pdfRef = storageRef.child("calendario_escolar_2024_2025.pdf") // Cambia el nombre según tu archivo en Firebase

        val localFile = File.createTempFile("tempPdf", "pdf")

        pdfRef.getFile(localFile)
            .addOnSuccessListener {
                pdfView.fromFile(localFile)
                    .enableSwipe(true)
                    .swipeHorizontal(false)
                    .enableDoubletap(true)
                    .load()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error al cargar el PDF: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}
