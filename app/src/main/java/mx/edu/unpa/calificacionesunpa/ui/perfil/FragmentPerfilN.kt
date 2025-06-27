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
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
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
import mx.edu.unpa.calificacionesunpa.providers.StorageProvider
import mx.edu.unpa.calificacionesunpa.service.ArchivoUtils
import mx.edu.unpa.calificacionesunpa.service.ProfilePictureService
import mx.edu.unpa.calificacionesunpa.service.PromedioCalculatorService
import mx.edu.unpa.calificacionesunpa.ui.changePass.ChangePassword
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mx.edu.unpa.calificacionesunpa.fragments.LoadingFragment
import mx.edu.unpa.calificacionesunpa.service.CarreraPromedioService
import mx.edu.unpa.calificacionesunpa.service.OnBitmapResultCallback
import mx.edu.unpa.calificacionesunpa.service.OnBooleanResultCallback


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

    private lateinit var profilePictureService: ProfilePictureService


    private lateinit var authGoogleProvider: AuthGoogleProvider

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_perfil, container, false)

        ivProfile = view.findViewById(R.id.ivProfile)
        profilePictureService = ProfilePictureService.getInstance(requireContext())
        loadProfilePicture()
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

        val btnCambiarPass = view.findViewById<TextView>(R.id.cambiarPass)

        btnCambiarPass.setOnClickListener { v: View? ->
            val intent = Intent(activity, ChangePassword::class.java)
            startActivity(intent)
        }
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
    private fun loadProfilePicture() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            ivProfile.setImageResource(R.drawable.ic_perfil)
            return
        }

        profilePictureService.getProfilePicture(user.uid, object : OnBitmapResultCallback {
            override fun onResult(bitmap: Bitmap?) {
                requireActivity().runOnUiThread {
                    if (bitmap != null) {
                        ivProfile.setImageBitmap(bitmap)
                    } else {
                        ivProfile.setImageResource(R.drawable.ic_perfil)
                    }
                }
            }
        })
    }
    private var promedioCache: Double? = null

    override fun onStart() {
        super.onStart()
        authGoogleProvider.updateUI(auth.currentUser,requireActivity());

        if(promedioCache == null) {
            calcularPromedio()
        } else {
            tvPromedio.text = String.format("%.1f", promedioCache)
        }
    }
    private fun calcularPromedio() {
        lifecycleScope.launch {
            showLoading()
            promedioCache = CarreraPromedioService.obtenerPromedioCarrera()
            tvPromedio.text = String.format("%.1f", promedioCache)
            hideLoading()
        }
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
    private fun showLoading() {
        Log.d(TAG, "Mostrar loading")
        val container = view?.findViewById<FrameLayout>(R.id.loadingContainer)
        container?.visibility = View.VISIBLE

        if (childFragmentManager.findFragmentByTag("loading_fragment") == null) {
            childFragmentManager.beginTransaction()
                .add(R.id.loadingContainer, LoadingFragment(), "loading_fragment")
                .commitAllowingStateLoss()
        }
    }

    private fun hideLoading() {
        Log.d(TAG, "Ocultar loading")
        val container = view?.findViewById<FrameLayout>(R.id.loadingContainer)
        container?.visibility = View.GONE

        val fragment = childFragmentManager.findFragmentByTag("loading_fragment")
        if (fragment != null) {
            childFragmentManager.beginTransaction()
                .remove(fragment)
                .commitAllowingStateLoss()
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




    companion object {
        private const val TAG = "FragmentPerfil"
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            val imageUri = data.data ?: return

            // Mostrar spinner
            showLoading()

            // Guardar imagen y actualizar UI solo si el guardado fue exitoso
            lifecycleScope.launch {
                val userId = FirebaseAuth.getInstance().currentUser?.uid
                if (userId != null) {
                    profilePictureService.saveProfilePicture(requireContext(), imageUri, userId, object : OnBooleanResultCallback {
                        override fun onResult(success: Boolean) {
                            requireActivity().runOnUiThread {
                                hideLoading()
                                if (success) {
                                    // Actualiza la imagen con la que guardaste en Firebase
                                    loadProfilePicture()
                                } else {
                                    // Opcional: mensaje de error
                                    Toast.makeText(requireContext(), "Error al guardar la imagen", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    })
                } else {
                    hideLoading()
                }
            }
        }
    }


}
