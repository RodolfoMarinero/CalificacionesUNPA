package mx.edu.unpa.calificacionesunpa.ui.perfil;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.credentials.CredentialManager;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.exceptions.ClearCredentialException;
import androidx.fragment.app.Fragment;
import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.android.material.button.MaterialButton;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.util.Locale;

import mx.edu.unpa.calificacionesunpa.R;

import mx.edu.unpa.calificacionesunpa.service.PromedioCalculatorService;

public class FragmentPerfil extends Fragment {

    private TextView tvNombre, tvMatricula, tvCarrera, tvPromedio, tvCodigoBarras;
    private ImageView ivCodigoBarras;
    private PromedioCalculatorService promedioCalculatorService;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_perfil, container, false);

        tvNombre = view.findViewById(R.id.tvNombre);
        tvMatricula = view.findViewById(R.id.tvMatriculaPerfil);
        tvCarrera = view.findViewById(R.id.tvCarreraPerfil);
        tvPromedio = view.findViewById(R.id.tvPromedioPerfil);
        ivCodigoBarras = view.findViewById(R.id.ivBarcode);
        tvCodigoBarras = view.findViewById(R.id.tvBarcodeNumber);
        MaterialButton btnVolver = view.findViewById(R.id.btnVolver);
        btnVolver.setOnClickListener(v ->  {
            requireActivity().getSupportFragmentManager().popBackStack();
        });
        promedioCalculatorService = PromedioCalculatorService.INSTANCE;
        if (getArguments() != null) {
            String nombre = getArguments().getString("nombre", "");
            String matricula = getArguments().getString("matricula", "");
            String carrera = getArguments().getString("carrera", "");
            String promedio = getArguments().getString("promedio", "");

            tvNombre.setText(nombre);
            tvMatricula.setText(matricula);
            tvCarrera.setText(carrera);
            tvPromedio.setText(promedio);
            tvCodigoBarras.setText(matricula);

            generarCodigoBarras(matricula);
        }

        auth = Firebase.auth
        // [END initialize_auth]

        // [START initialize_credential_manager]
        // Initialize Credential Manager
        credentialManager = CredentialManager.create(requireActivity());
        // [END initialize_credential_manager]

        //launchCredentialManager();

        tvPromedio.setText(String.format("%.1f", promedioCalculatorService.getPromedioGeneral()));
        return view;
    }

    // [START on_start_check_user]
    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser;
        updateUI(currentUser);
    }
    // [END on_start_check_user]


    private void generarCodigoBarras(String texto) {
        try {
            BarcodeEncoder encoder = new BarcodeEncoder();
            Bitmap bitmap = encoder.encodeBitmap(texto, BarcodeFormat.CODE_128, 600, 200);
            ivCodigoBarras.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }

    public fun callSignInGoogle(view:ViewFragment){
        launchCredentialManager();
    }

    private fun launchCredentialManager() {
        // [START create_credential_manager_request]
        // Instantiate a Google sign-in request
        val googleIdOption = GetGoogleIdOption.Builder()
                // Your server's client ID, not your Android client ID.
                .setServerClientId(getString(R.string.default_web_client_id))
                // Only show accounts previously used to sign in.
                .setFilterByAuthorizedAccounts(false)
                .build()

        // Create the Credential Manager request
        val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()
        // [END create_credential_manager_request]

        lifecycleScope.launch {
            try {
                // Launch Credential Manager UI
                val result = credentialManager.getCredential(
                        context = requireActivity(),
                        request = request
                )

                // Extract credential from the result returned by Credential Manager
                handleSignIn(result.credential)
            } catch (e: GetCredentialException) {
                Log.e(TAG, "Couldn't retrieve user's credentials: ${e.localizedMessage}")
            }
        }
    }

    // [START handle_sign_in]
    private fun handleSignIn(credential: Credential) {
        // Check if credential is of type Google ID
        if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            // Create Google ID Token
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)

            // Sign in to Firebase with using the token
            firebaseAuthWithGoogle(googleIdTokenCredential.idToken)
        } else {
            Log.w(TAG, "Credential is not of type Google ID!")
        }
    }
    // [END handle_sign_in]

    // [START auth_with_google]
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                // Sign in success, update UI with the signed-in user's information
                Log.d(TAG, "signInWithCredential:success")
                val user = auth.currentUser
                updateUI(user)
            } else {
                // If sign in fails, display a message to the user
                Log.w(TAG, "signInWithCredential:failure", task.exception)
                updateUI(null)
            }
        }
    }
    // [END auth_with_google]

    public fun callSignOut(view:ViewFragment){
        signOut();
    }

    // [START sign_out]
    private fun signOut() {
        // Firebase sign out
        auth.signOut()

        // When a user signs out, clear the current user credential state from all credential providers.
        lifecycleScope.launch {
            try {
                val clearRequest = ClearCredentialStateRequest()
                credentialManager.clearCredentialState(clearRequest)
                updateUI(null)
            } catch (e:ClearCredentialException) {
                Log.e(TAG, "Couldn't clear user credentials: ${e.localizedMessage}")
            }
        }
    }
    // [END sign_out]

    private fun updateUI(user:FirebaseUser?) {
    }
    //cambiar este metodo por el que me envió el profe

    companion object {
        private const val TAG = "GoogleActivity"
    }

}
