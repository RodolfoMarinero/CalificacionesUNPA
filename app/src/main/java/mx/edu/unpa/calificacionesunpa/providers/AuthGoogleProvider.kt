package mx.edu.unpa.calificacionesunpa.providers

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.view.View
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.exceptions.ClearCredentialException
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.LifecycleCoroutineScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import mx.edu.unpa.calificacionesunpa.models.Alumno
import mx.edu.unpa.calificacionesunpa.service.UsuarioService


class AuthGoogleProvider() {

    private val usuarioService = UsuarioService;
    private val db = FirebaseFirestore.getInstance()


    fun callSignInGoogle(
        view: View,
        activity: Activity,
        lifecycleScope: LifecycleCoroutineScope,
        credentialManager: CredentialManager,
        webClientId: String,
        auth: FirebaseAuth
    ) {
        launchCredentialManager(activity, lifecycleScope, credentialManager, webClientId, auth)
    }

    // Lanza el CredentialManager
    fun launchCredentialManager(
        activity: Activity,
        lifecycleScope: LifecycleCoroutineScope,
        credentialManager: CredentialManager,
        webClientId: String,
        auth: FirebaseAuth
    ) {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setServerClientId(webClientId)
            .setFilterByAuthorizedAccounts(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        lifecycleScope.launch {
            try {
                val result = credentialManager.getCredential(activity, request)
                handleSignIn(result.credential, activity, lifecycleScope, credentialManager, auth)
            } catch (e: GetCredentialException) {
                Log.e(TAG, "Error obteniendo credenciales: ${e.localizedMessage}")
            }
        }
    }

    // Procesa el resultado del login
    fun handleSignIn(
        credential: Credential,
        activity: Activity,
        lifecycleScope: LifecycleCoroutineScope,
        credentialManager: CredentialManager,
        auth: FirebaseAuth
    ) {
        if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            firebaseAuthWithGoogle(googleIdTokenCredential.idToken, activity, auth)
        } else {
            Log.w(TAG, "Credential no es de tipo Google ID")
        }
    }

    // Autenticación con Firebase
    fun firebaseAuthWithGoogle(idToken: String, activity: Activity, auth: FirebaseAuth) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(activity) { task ->
                if (task.isSuccessful) {
                    updateUI(auth.currentUser, activity)
                } else {
                    Log.w(TAG, "Fallo al autenticar", task.exception)
                    updateUI(null, activity)
                }
            }
    }

    // Función para cerrar sesión
    fun callSignOut(
        activity: Activity,
        lifecycleScope: LifecycleCoroutineScope,
        credentialManager: CredentialManager,
        auth: FirebaseAuth
    ) {
        signOut(activity, lifecycleScope, credentialManager, auth)
    }

    // Proceso para cerrar sesión y limpiar credenciales
    fun signOut(
        activity: Activity,
        lifecycleScope: LifecycleCoroutineScope,
        credentialManager: CredentialManager,
        auth: FirebaseAuth
    ) {
        auth.signOut()
        lifecycleScope.launch {
            try {
                val clearRequest = ClearCredentialStateRequest()
                credentialManager.clearCredentialState(clearRequest)
                updateUI(null, activity)
            } catch (e: ClearCredentialException) {
                Log.e(TAG, "No se pudieron limpiar las credenciales: ${e.localizedMessage}")
            }
        }
    }

    // Actualiza UI y guarda en SharedPreferences si es válido
    fun updateUI(user: FirebaseUser?, context: Context) {
        val sharedPref: SharedPreferences =
            context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        if (user != null) {
            val email = user.email
            val uid = user.uid
            Log.d("FirebaseUser", "Id: $uid")
            Log.d("FirebaseUser", "Email: $email")
            if (!email.isNullOrEmpty() && !email.endsWith("@unpaloma.com")) {
                sharedPref.edit().putBoolean("accesoConGoogle", true).apply()

                val alumnoActual = usuarioService.alumnoActual
                if (alumnoActual != null) {
                    val db = FirebaseFirestore.getInstance()
                    val data = mapOf(
                        "correoGoogle" to email,
                        "matricula" to alumnoActual.matricula,
                    )

                    db.collection("usuarios_vinculados")
                        .document(uid)
                        .get()
                        .addOnSuccessListener { document ->
                            if (!document.exists()) {
                                db.collection("usuarios_vinculados")
                                    .document(uid)
                                    .set(data)
                                    .addOnSuccessListener {
                                        Log.d("Firestore", "Usuario vinculado exitosamente")
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e("Firestore", "Error al vincular usuario", e)
                                    }
                            } else {
                                Log.d("Firestore", "Ya existe documento vinculado para este UID")
                            }
                        }

                } else {
                    Log.w("AuthGoogleProvider", "No se encontró alumno actual para vincular")
                }
            }
        }
    }
}