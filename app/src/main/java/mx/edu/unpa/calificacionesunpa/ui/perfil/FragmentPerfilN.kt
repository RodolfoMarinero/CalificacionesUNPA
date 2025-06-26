package mx.edu.unpa.calificacionesunpa.ui.perfil

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.credentials.CredentialManager
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.journeyapps.barcodescanner.BarcodeEncoder
import mx.edu.unpa.calificacionesunpa.R
import mx.edu.unpa.calificacionesunpa.providers.AuthGoogleProvider
import mx.edu.unpa.calificacionesunpa.providers.AuthProvider
import mx.edu.unpa.calificacionesunpa.service.PromedioCalculatorService
import mx.edu.unpa.calificacionesunpa.ui.changePass.ChangePassword
import java.io.File
import java.io.FileOutputStream

class FragmentPerfilN : Fragment() {

    private lateinit var tvNombre: TextView
    private lateinit var tvMatricula: TextView
    private lateinit var tvCarrera: TextView
    private lateinit var tvPromedio: TextView
    private lateinit var tvCodigoBarras: TextView
    private lateinit var ivCodigoBarras: ImageView
    private lateinit var auth: FirebaseAuth
    private lateinit var authProvider: AuthProvider
    private lateinit var credentialManager: CredentialManager
    private val promedioCalculatorService = PromedioCalculatorService
    private  lateinit var userGoogle: FirebaseUser
    var loginGoogle: Boolean = false

    private lateinit var ivProfile: ImageView
    private val PICK_IMAGE_REQUEST = 1001
    private val REQUEST_PERMISSION_CODE = 2001
    private val sharedPrefs: SharedPreferences by lazy {
        requireContext().getSharedPreferences("profile_prefs", Context.MODE_PRIVATE)
    }


    private lateinit var authGoogleProvider: AuthGoogleProvider

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_perfil, container, false)

        ivProfile = view.findViewById(R.id.ivProfile)

        ivProfile.setOnClickListener {
            if (hasPermission()) {
                abrirGaleria()
            } else {
                requestPermissions(arrayOf(
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU)
                        android.Manifest.permission.READ_MEDIA_IMAGES
                    else
                        android.Manifest.permission.READ_EXTERNAL_STORAGE
                ), REQUEST_PERMISSION_CODE)
            }
        }


        tvNombre = view.findViewById(R.id.tvNombre)
        tvMatricula = view.findViewById(R.id.tvMatriculaPerfil)
        tvCarrera = view.findViewById(R.id.tvCarreraPerfil)
        tvPromedio = view.findViewById(R.id.tvPromedioPerfil)
        ivCodigoBarras = view.findViewById(R.id.ivBarcode)
        tvCodigoBarras = view.findViewById(R.id.tvBarcodeNumber)

        /*val btnCambiarPass = view.findViewById<TextView>(R.id.cambiarPass)

        btnCambiarPass.setOnClickListener { v: View? ->
            val intent = Intent(activity, ChangePassword::class.java)
            startActivity(intent)
        }*/
        authProvider = AuthProvider()
        authGoogleProvider = AuthGoogleProvider()
        auth = FirebaseAuth.getInstance()
        credentialManager = CredentialManager.create(requireActivity())

        arguments?.let {
            val nombre = it.getString("nombre", "")
            val matricula = it.getString("matricula", "")
            val carrera = it.getString("carrera", "")

            tvNombre.text = nombre
            tvMatricula.text = matricula
            tvCarrera.text = carrera
            tvCodigoBarras.text = matricula
            generarCodigoBarras(matricula)
        }

        //tvPromedio.text = String.format("%.1f", promedioCalculatorService.getPromedioGeneral())

        val button = view.findViewById<TextView>(R.id.tvGoogle)
        button.setOnClickListener {
            // Tu acción aquí
            authGoogleProvider.callSignInGoogle(view,requireActivity(),lifecycleScope,credentialManager,requireActivity().getString(R.string.default_web_client_id),auth,true);
        }
        return view
    }

    override fun onStart() {
        super.onStart()
        authGoogleProvider.updateUI(auth.currentUser,requireActivity());
    }
    private fun hasPermission(): Boolean {
        val permission = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU)
            android.Manifest.permission.READ_MEDIA_IMAGES
        else
            android.Manifest.permission.READ_EXTERNAL_STORAGE

        return requireContext().checkSelfPermission(permission) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }
    private fun abrirGaleria() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION_CODE && grantResults.isNotEmpty() && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            abrirGaleria()
        } else {
            Log.e(TAG, "Permiso denegado para acceder a la galería")
        }
    }

    private fun generarCodigoBarras(texto: String) {
        try {
            val encoder = BarcodeEncoder()
            val bitmap = encoder.encodeBitmap(texto, BarcodeFormat.CODE_128, 600, 200)
            ivCodigoBarras.setImageBitmap(bitmap)
        } catch (e: WriterException) {
            e.printStackTrace()
        }
    }

    private fun guardarImagenLocal(bitmap: Bitmap) {
        try {
            val file = File(requireContext().filesDir, "imagen_perfil.png")
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.flush()
            outputStream.close()

            // Forzar recarga en ImageView
            ivProfile.setImageBitmap(bitmap)

            // Notificar cambio
            sharedPrefs.edit().putLong("last_update", System.currentTimeMillis()).apply()
        } catch (e: Exception) {
            Log.e("Perfil", "Error al guardar imagen", e)
        }
    }
    private fun cargarImagenLocal(): Boolean {
        val file = File(requireContext().filesDir, "imagen_perfil.png")
        return if (file.exists()) {
            val bitmap = BitmapFactory.decodeFile(file.absolutePath)
            ivProfile.setImageBitmap(bitmap)
            true
        } else {
            false
        }
    }


    companion object {
        private const val TAG = "FragmentPerfil"
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            val imageUri = data.data
            ivProfile.setImageURI(imageUri)

            // Convertir y guardar localmente inmediatamente
           /* val base64 = ArchivoUtils.convertirA_Base64(requireContext(), imageUri!!)
            if (base64 != null) {
                val imageBytes = Base64.decode(base64, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                guardarImagenLocal(bitmap) // Guardar localmente

                // Actualizar Firebase en segundo plano
                val userId = FirebaseAuth.getInstance().currentUser?.uid
                val fileName = "perfil_${System.currentTimeMillis()}.jpg"
                if (userId != null) {
                    val provider = StorageProvider()
                    provider.uploadImage(base64, fileName, userId) { success ->
                        if (success) {
                            Log.d("Perfil", "Imagen actualizada en Firebase")

                            // Notificar a toda la app sobre la actualización
                            sharedPrefs.edit().putLong("last_update", System.currentTimeMillis()).apply()
                        }
                    }
                }
            }*/
        }
    }

}
