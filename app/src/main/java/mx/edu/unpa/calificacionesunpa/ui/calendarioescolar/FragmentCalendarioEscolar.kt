package mx.edu.unpa.calificacionesunpa.ui.calendarioescolar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.github.barteksc.pdfviewer.PDFView
import mx.edu.unpa.calificacionesunpa.R
import mx.edu.unpa.calificacionesunpa.providers.StorageProvider
import mx.edu.unpa.calificacionesunpa.service.CalendarioService

class FragmentCalendarioEscolar : Fragment() {

    private lateinit var pdfView: PDFView
    private lateinit var calendarioService: CalendarioService

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_calendario_escolar, container, false)
        pdfView = view.findViewById(R.id.pdfView)

        // Usar StorageProvider real
        calendarioService = CalendarioService(StorageProvider())

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
            onError = { mensaje ->
                Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show()
            }
        )
    }
}
