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
    private val calendarioService = CalendarioService(CalendarioProvider())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_calendario_escolar, container, false)

        pdfView = view.findViewById(R.id.pdfView)

        cargarCalendario("calendario_2025")

        return view
    }

    private fun cargarCalendario(idDocumento: String) {
        calendarioService.obtenerCalendarioPdf(idDocumento,
            onSuccess = { pdfBytes ->
                val inputStream = pdfBytes.inputStream()
                pdfView.fromStream(inputStream)
                    .enableSwipe(true)
                    .swipeHorizontal(false)
                    .enableDoubletap(true)
                    .load()
            },
            onError = { error ->
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
            }
        )
    }
}
