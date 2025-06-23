package mx.edu.unpa.calificacionesunpa.ui.perfil

import android.os.Bundle
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
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.journeyapps.barcodescanner.BarcodeEncoder
import mx.edu.unpa.calificacionesunpa.R
import mx.edu.unpa.calificacionesunpa.providers.AuthGoogleProvider
import mx.edu.unpa.calificacionesunpa.providers.AuthProvider

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
    private lateinit var authGoogleProvider: AuthGoogleProvider

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
}
