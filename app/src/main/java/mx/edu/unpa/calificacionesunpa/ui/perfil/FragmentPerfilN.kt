package mx.edu.unpa.calificacionesunpa.ui.perfil

import android.content.ContentValues
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mx.edu.unpa.calificacionesunpa.R
import mx.edu.unpa.calificacionesunpa.data.api.reportes.ReportesAPI
import mx.edu.unpa.calificacionesunpa.data.persistent.UsuarioService
import mx.edu.unpa.calificacionesunpa.ui.actualizarpassword.ActualizarPasswordActivity
import mx.edu.unpa.calificacionesunpa.ui.fragments.LoadingFragment
import mx.edu.unpa.calificacionesunpa.ui.notificaciones.AvisoViewModel
import mx.edu.unpa.calificacionesunpa.utils.Utilerias
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import javax.inject.Inject

@AndroidEntryPoint
class FragmentPerfilN : Fragment() {

    @Inject
    lateinit var reportesAPI: ReportesAPI

    private val viewModel: AvisoViewModel by viewModels() // Asumiendo que usas el mismo o similar

    private lateinit var tvNombre: TextView
    private lateinit var tvMatricula: TextView
    private lateinit var tvCarrera: TextView
    private lateinit var tvPromedio: TextView
//    private lateinit var tvCodigoBarras: TextView

    // Botones (ahora son RelativeLayouts que parecen celdas)
    private lateinit var btnHistorial: RelativeLayout
    private lateinit var btnConstancia: RelativeLayout

    // Botones de acción final
    private lateinit var btnChangePass: MaterialButton
    private lateinit var btnLogout: MaterialButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_perfil, container, false)

        // 1. Vincular Referencias UI (IDs actualizados del nuevo XML)
        tvNombre = view.findViewById(R.id.tvNombre)
        tvMatricula = view.findViewById(R.id.tvMatriculaPerfil)
        tvCarrera = view.findViewById(R.id.tvCarreraPerfil)
        tvPromedio = view.findViewById(R.id.tvPromedioPerfil)
//        tvCodigoBarras = view.findViewById(R.id.tvBarcodeNumber)

        btnHistorial = view.findViewById(R.id.btnHistorial)
        btnConstancia = view.findViewById(R.id.btnConstancia)
        btnChangePass = view.findViewById(R.id.btnChangePass)
        btnLogout = view.findViewById(R.id.btnLogout)

        // 2. Cargar datos del alumno desde el Service
        UsuarioService.alumnoActual?.let { alumno ->
            // El diseño de Figma usa saltos de línea para el nombre grande
            tvNombre.text = "${alumno.nombre}\n${alumno.apPaterno}\n${alumno.apMaterno}"
            tvMatricula.text = alumno.matricula
            tvCarrera.text = alumno.nombreCarrera
//            tvCodigoBarras.text = alumno.matricula

            // Si quieres generar el código de barras real, descomenta la función abajo
            // generarCodigoBarras(alumno.matricula, view.findViewById(R.id.ivBarcode))
        }

        // 3. Configurar Click Listeners
        btnChangePass.setOnClickListener {
            startActivity(Intent(activity, ActualizarPasswordActivity::class.java))
        }

        btnLogout.setOnClickListener {
            mostrarDialogoConfirmacion()
        }

        btnHistorial.setOnClickListener { descargarDocumento("historial") }
        btnConstancia.setOnClickListener { descargarDocumento("constancia") }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        calcularYMostrarPromedio()
    }

    private fun calcularYMostrarPromedio() {
        UsuarioService.alumnoActual?.materias?.let { materias ->
            val promedio = Utilerias.calcularPromedioMaterias(materias)
            tvPromedio.text = String.format("%.1f", promedio)
        }
    }

    // ------------------------------------------------------------------------
    // LÓGICA DE DESCARGA
    // ------------------------------------------------------------------------
    private fun descargarDocumento(tipo: String) {
        val matricula = UsuarioService.alumnoActual?.matricula ?: return
        showLoading()

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = if (tipo == "historial") {
                    reportesAPI.descargarHistorial(matricula)
                } else {
                    reportesAPI.descargarConstancia(matricula)
                }

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        val nombreArchivo = if (tipo == "historial") "Historial_$matricula.pdf" else "Constancia_$matricula.pdf"
                        val guardado = guardarStreamEnDispositivo(response.body()!!.byteStream(), nombreArchivo)

                        hideLoading()
                        if (guardado) {
                            Toast.makeText(requireContext(), "Archivo guardado en Descargas", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        hideLoading()
                        Toast.makeText(requireContext(), "Error: No se encontraron datos para este reporte.", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    hideLoading()
                    Toast.makeText(requireContext(), "Error de conexión", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun guardarStreamEnDispositivo(inputStream: InputStream, nombreArchivo: String): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, nombreArchivo)
                    put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
                    put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val resolver = requireContext().contentResolver
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values) ?: return false
                resolver.openOutputStream(uri)?.use { inputStream.copyTo(it) }
                true
            } else {
                val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), nombreArchivo)
                FileOutputStream(file).use { inputStream.copyTo(it) }
                true
            }
        } catch (e: IOException) { false }
    }

    private fun showLoading() {
        view?.findViewById<FrameLayout>(R.id.loadingContainer)?.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        view?.findViewById<FrameLayout>(R.id.loadingContainer)?.visibility = View.GONE
    }

    companion object {
        private const val TAG = "FragmentPerfilN"
    }
    private fun mostrarDialogoConfirmacion() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_confirmar_salida, null)

        val dialog = com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext(),R.style.CustomDialogTheme)
            .setView(dialogView)
            .create()

        // Fondo transparente para ver los bordes redondeados del XML
        dialog.window?.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))

        dialogView.findViewById<Button>(R.id.btnCancelarSalida).setOnClickListener {
            dialog.dismiss()
        }

        dialogView.findViewById<Button>(R.id.btnConfirmarSalida).setOnClickListener {
            // 1. Limpiar datos de sesión
            UsuarioService.alumnoActual = null

            // 2. Opcional: Limpiar SharedPreferences si guardas el login automático
            // requireContext().getSharedPreferences("login", Context.MODE_PRIVATE).edit().clear().apply()

            dialog.dismiss()

            // 3. Redirigir al Login y cerrar la actividad actual
            activity?.finish()
        }

        dialog.show()
    }
}