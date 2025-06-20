package mx.edu.unpa.calificacionesunpa.ui.perfil

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.ClearCredentialException
import androidx.credentials.exceptions.GetCredentialException
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.journeyapps.barcodescanner.BarcodeEncoder
import kotlinx.coroutines.launch
import mx.edu.unpa.calificacionesunpa.R
import mx.edu.unpa.calificacionesunpa.providers.AuthProvider
import mx.edu.unpa.calificacionesunpa.service.PromedioCalculatorService
import mx.edu.unpa.calificacionesunpa.ui.changePass.ChangePassword

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


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_perfil, container, false)

        tvNombre = view.findViewById(R.id.tvNombre)
        tvMatricula = view.findViewById(R.id.tvMatriculaPerfil)
        tvCarrera = view.findViewById(R.id.tvCarreraPerfil)
        tvPromedio = view.findViewById(R.id.tvPromedioPerfil)
        ivCodigoBarras = view.findViewById(R.id.ivBarcode)
        tvCodigoBarras = view.findViewById(R.id.tvBarcodeNumber)

        view.findViewById<MaterialButton>(R.id.btnVolver).setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
        val btnCambiarPass = view.findViewById<TextView>(R.id.cambiarPass)

        btnCambiarPass.setOnClickListener { v: View? ->
            val intent = Intent(activity, ChangePassword::class.java)
            startActivity(intent)
        }
        authProvider = AuthProvider()

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
            callSignInGoogle(view);
        }
        return view
    }

    override fun onStart() {
        super.onStart()
        updateUI(auth.currentUser)
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

    fun callSignInGoogle(view: View) {
        launchCredentialManager()
    }

    fun launchCredentialManager() {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setServerClientId(getString(R.string.default_web_client_id))
            .setFilterByAuthorizedAccounts(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        lifecycleScope.launch {
            try {
                val result = credentialManager.getCredential(requireActivity(), request)
                handleSignIn(result.credential)
            } catch (e: GetCredentialException) {
                Log.e(TAG, "Error obteniendo credenciales: ${e.localizedMessage}")
            }
        }
    }

     fun handleSignIn(credential: Credential) {
        if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            firebaseAuthWithGoogle(googleIdTokenCredential.idToken)
        } else {
            Log.w(TAG, "Credential no es de tipo Google ID")
        }
    }

     fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    updateUI(auth.currentUser)
                } else {
                    Log.w(TAG, "Fallo al autenticar", task.exception)
                    updateUI(null)
                }
            }
    }

    fun callSignOut(view: View) {
        signOut()
    }

    private fun signOut() {
        auth.signOut()
        lifecycleScope.launch {
            try {
                val clearRequest = ClearCredentialStateRequest()
                credentialManager.clearCredentialState(clearRequest)
                updateUI(null)
            } catch (e: ClearCredentialException) {
                Log.e(TAG, "No se pudieron limpiar las credenciales: ${e.localizedMessage}")
            }
        }
    }

    fun updateUI(user: FirebaseUser?) {
        val sharedPref: SharedPreferences = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        if(user != null) {
            val uid = user.uid
            val email = user.email

            Log.d("FirebaseUser", "Email: $email")

            if(!email.toString().endsWith("@unpaLoma")){
                with(sharedPref.edit()){
                    putBoolean("accesoConGoogle",true).commit()
                }
            }

        }
    }


    companion object {
        private const val TAG = "FragmentPerfil"
    }
}
