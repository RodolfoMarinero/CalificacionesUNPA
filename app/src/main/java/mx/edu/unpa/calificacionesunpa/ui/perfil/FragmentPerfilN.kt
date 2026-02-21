package mx.edu.unpa.calificacionesunpa.ui.perfil

import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
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
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mx.edu.unpa.calificacionesunpa.R
import mx.edu.unpa.calificacionesunpa.data.api.reportes.ReportesAPI
import mx.edu.unpa.calificacionesunpa.data.persistent.UsuarioService
import mx.edu.unpa.calificacionesunpa.ui.actualizarpassword.ActualizarPasswordActivity
import mx.edu.unpa.calificacionesunpa.ui.fragments.LoadingFragment
import mx.edu.unpa.calificacionesunpa.utils.Utilerias
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import javax.inject.Inject

@AndroidEntryPoint // <--- IMPORTANTE: Esto permite inyectar la API
class FragmentPerfilN : Fragment() {

    // Inyectamos la API que acabamos de crear
    @Inject
    lateinit var reportesAPI: ReportesAPI

    private lateinit var tvNombre: TextView
    private lateinit var tvMatricula: TextView
    private lateinit var tvCarrera: TextView
    private lateinit var tvPromedio: TextView
    private lateinit var tvCodigoBarras: TextView
    private lateinit var ivCodigoBarras: ImageView
    private lateinit var ivProfile: ImageView

    // Botones nuevos
    private lateinit var btnHistorial: MaterialButton
    private lateinit var btnConstancia: MaterialButton

    private val PICK_IMAGE_REQUEST = 1001
    private val REQUEST_PERMISSION_CODE = 2001

    // Cache del promedio
    private var promedioCache: Double? = null

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                abrirGaleria()
            } else {
                Toast.makeText(requireContext(), "Permiso denegado", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_perfil, container, false)

        // Referencias UI
        ivProfile = view.findViewById(R.id.ivProfile)
        tvNombre = view.findViewById(R.id.tvNombre)
        tvMatricula = view.findViewById(R.id.tvMatriculaPerfil)
        tvCarrera = view.findViewById(R.id.tvCarreraPerfil)
        tvPromedio = view.findViewById(R.id.tvPromedioPerfil)
        ivCodigoBarras = view.findViewById(R.id.ivBarcode)
        tvCodigoBarras = view.findViewById(R.id.tvBarcodeNumber)
        val btnBack = view.findViewById<Button>(R.id.btnBack)
        val btnCambiarPass = view.findViewById<TextView>(R.id.cambiarPass)

        // --- NUEVOS BOTONES ---
        btnHistorial = view.findViewById(R.id.btnDescargarHistorial)
        btnConstancia = view.findViewById(R.id.btnDescargarConstancia)

        // Configuración inicial
        loadProfilePicture()

        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        ivProfile.setOnClickListener {
            checkAndRequestPermissions()
        }

        btnCambiarPass.setOnClickListener {
            val intent = Intent(activity, ActualizarPasswordActivity::class.java)
            startActivity(intent)
        }

        // Cargar datos del alumno
        UsuarioService.alumnoActual?.let { alumno ->
            tvNombre.text = "${alumno.nombre} ${alumno.apPaterno} ${alumno.apMaterno}"
            tvMatricula.text = alumno.matricula
            tvCarrera.text = alumno.nombreCarrera
            tvCodigoBarras.text = alumno.matricula
            generarCodigoBarras(alumno.matricula)
        }

        // --- LISTENERS DE DESCARGA ---
        btnHistorial.setOnClickListener {
            descargarDocumento("historial")
        }

        btnConstancia.setOnClickListener {
            descargarDocumento("constancia")
        }

        return view
    }

    // ------------------------------------------------------------------------
    // LÓGICA DE DESCARGA (CONECTADA AL BACKEND)
    // ------------------------------------------------------------------------
//    private fun descargarDocumento(tipo: String) {
//        val matricula = UsuarioService.alumnoActual?.matricula
//        if (matricula.isNullOrEmpty()) {
//            Toast.makeText(requireContext(), "No se encontró matrícula", Toast.LENGTH_SHORT).show()
//            return
//        }
//
//        showLoading()
//
//        lifecycleScope.launch(Dispatchers.IO) {
//            try {
//                // Llamada a la API según el botón presionado
//                val response = if (tipo == "historial") {
//                    reportesAPI.descargarHistorial(matricula)
//                } else {
//                    reportesAPI.descargarConstancia(matricula)
//                }
//
//                withContext(Dispatchers.Main) {
//                    if (response.isSuccessful && response.body() != null) {
//                        // Nombre del archivo a guardar
//                        val nombreArchivo = if (tipo == "historial")
//                            "Historial_$matricula.pdf"
//                        else
//                            "Constancia_$matricula.pdf"
//
//                        // Guardar los bytes
//                        val guardado = guardarStreamEnDispositivo(response.body()!!.byteStream(), nombreArchivo)
//
//                        hideLoading()
//                        if (guardado) {
//                            Toast.makeText(requireContext(), "PDF guardado en Descargas", Toast.LENGTH_LONG).show()
//                            // Opcional: Abrir el PDF automáticamente aquí
//                        } else {
//                            Toast.makeText(requireContext(), "Error al guardar el archivo", Toast.LENGTH_SHORT).show()
//                        }
//                    } else {
//                        hideLoading()
//                        val errorMsg = "Error: ${response.code()} - Posiblemente no hay datos."
//                        Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_LONG).show()
//                        Log.e(TAG, "Error body: ${response.errorBody()?.string()}")
//                    }
//                }
//            } catch (e: Exception) {
//                Log.e(TAG, "Error descarga", e)
//                withContext(Dispatchers.Main) {
//                    hideLoading()
//                    Toast.makeText(requireContext(), "Error de conexión con el servidor", Toast.LENGTH_SHORT).show()
//                }
//            }
//        }
//    }

    // ------------------------------------------------------------------------
    // LÓGICA DE DESCARGA (CORREGIDA)
    // ------------------------------------------------------------------------
    private fun descargarDocumento(tipo: String) {
        val matricula = UsuarioService.alumnoActual?.matricula
        if (matricula.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "No se encontró matrícula", Toast.LENGTH_SHORT).show()
            return
        }


        showLoading()

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Llamada a la API con el TOKEN
                val response = if (tipo == "historial") {
                    reportesAPI.descargarHistorial(matricula)
                } else {
                    reportesAPI.descargarConstancia(matricula)
                }

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        // Nombre del archivo a guardar
                        val nombreArchivo = if (tipo == "historial")
                            "Historial_$matricula.pdf"
                        else
                            "Constancia_$matricula.pdf"

                        // Guardar los bytes
                        val guardado = guardarStreamEnDispositivo(response.body()!!.byteStream(), nombreArchivo)

                        hideLoading()
                        if (guardado) {
                            Toast.makeText(requireContext(), "PDF guardado en Descargas", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(requireContext(), "Error al guardar el archivo", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        hideLoading()
                        // Manejo de errores específicos
                        if (response.code() == 403 || response.code() == 401) {
                            Toast.makeText(requireContext(), "Sesión expirada. Vuelve a iniciar sesión.", Toast.LENGTH_LONG).show()
                        } else {
                            val errorMsg = "Error: ${response.code()} - Posiblemente no hay datos."
                            Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_LONG).show()
                        }
                        Log.e(TAG, "Error body: ${response.errorBody()?.string()}")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error descarga", e)
                withContext(Dispatchers.Main) {
                    hideLoading()
                    Toast.makeText(requireContext(), "Error de conexión con el servidor", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun guardarStreamEnDispositivo(inputStream: InputStream, nombreArchivo: String): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10+ (Scoped Storage)
                val values = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, nombreArchivo)
                    put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
                    put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val resolver = requireContext().contentResolver
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values) ?: return false

                resolver.openOutputStream(uri)?.use { output ->
                    inputStream.copyTo(output)
                }
                true
            } else {
                // Android 9 e inferiores
                val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), nombreArchivo)
                FileOutputStream(file).use { output ->
                    inputStream.copyTo(output)
                }
                true
            }
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    // ------------------------------------------------------------------------
    // FUNCIONES EXISTENTES (SIN CAMBIOS)
    // ------------------------------------------------------------------------

    private fun loadProfilePicture() {
        // Tu lógica de imagen
    }

    override fun onStart() {
        super.onStart()
        if(promedioCache == null) {
            calcularPromedio()
        } else {
            tvPromedio.text = String.format("%.1f", promedioCache)
        }
    }

    private fun calcularPromedio() {
        lifecycleScope.launch {
            // showLoading() // Opcional, ya que es rápido
            UsuarioService.alumnoActual?.materias?.let { materias ->
                promedioCache = Utilerias.calcularPromedioMaterias(materias)
                Log.d("Promediox", "Promedio calculado: $promedioCache")
                tvPromedio.text = String.format("%.1f", promedioCache)
            }
            // hideLoading()
        }
    }

    private fun checkAndRequestPermissions() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            android.Manifest.permission.READ_MEDIA_IMAGES
        } else {
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED) {
            abrirGaleria()
        } else {
            requestPermissionLauncher.launch(permission)
        }
    }

    private fun abrirGaleria() {
        val intent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    private fun generarCodigoBarras(texto: String) {
        if (texto.isEmpty()) return
        try {
            val encoder = BarcodeEncoder()
            val bitmap = encoder.encodeBitmap(texto, BarcodeFormat.CODE_128, 600, 200)
            ivCodigoBarras.setImageBitmap(bitmap)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showLoading() {
        view?.findViewById<FrameLayout>(R.id.loadingContainer)?.visibility = View.VISIBLE
        if (childFragmentManager.findFragmentByTag("loading_fragment") == null) {
            childFragmentManager.beginTransaction()
                .add(R.id.loadingContainer, LoadingFragment(), "loading_fragment")
                .commitAllowingStateLoss()
        }
    }

    private fun hideLoading() {
        view?.findViewById<FrameLayout>(R.id.loadingContainer)?.visibility = View.GONE
        childFragmentManager.findFragmentByTag("loading_fragment")?.let {
            childFragmentManager.beginTransaction().remove(it).commitAllowingStateLoss()
        }
    }

    // onActivityResult, onRequestPermissionsResult, etc...

    companion object {
        private const val TAG = "FragmentPerfilN"
    }


}