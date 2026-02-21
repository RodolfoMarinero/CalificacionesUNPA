package mx.edu.unpa.calificacionesunpa.ui.calendarioescolar

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.github.barteksc.pdfviewer.PDFView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import mx.edu.unpa.calificacionesunpa.R
import java.io.File

@AndroidEntryPoint
class FragmentCalendarioEscolar : Fragment() {
    private lateinit var pdfView: PDFView
    private val viewModel: CalendarioEscolarViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_calendario_escolar, container, false)
        pdfView = view.findViewById(R.id.pdfView)

        //ARIEL Este token se debe actualizar de acuerdo
        //ARIEL al token obtenido al registrarse
        //val token = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxODA4MDA2OCIsImlhdCI6MTc1NjMxNjI2MSwiZXhwIjoxNzU2MzUyMjYxfQ.bR6CliCRb4wnXSoRaZJJ5GFiHpl-X43tmuZlrZgm3Xu8BKI41FXRqQBTrKYL0jUCJywlNtHjIoe5kL7d-kSF9Q"
        //val api = RetrofitClient.create(token)
        cargarCalendario()
        return view
    }

    private fun cargarCalendario() {
        lifecycleScope.launch {
            try {
                val file = File(requireContext().cacheDir, "documento.pdf")
                lifecycleScope.launch {
                    viewModel.file.collect { byteArray ->
                        byteArray?.let {
                            file.writeBytes(it)
                            val inputStream = it.inputStream()
                            pdfView.fromStream(inputStream)
                                .enableSwipe(true)
                                .swipeHorizontal(false)
                                .enableDoubletap(true)
                                .load()
                        }
                    }
                }
                viewModel.getFile("2024-2025")

            } catch (e: Exception) {
                Log.e("ARIEL PDF_DEBUG", "Error al descargar PDF", e)
            }
        }
    }
}
