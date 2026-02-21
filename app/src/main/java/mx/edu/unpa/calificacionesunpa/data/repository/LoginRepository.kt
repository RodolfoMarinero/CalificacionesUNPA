package mx.edu.unpa.calificacionesunpa.data.repository


import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mx.edu.unpa.calificacionesunpa.data.api.login.AuthAPI
import mx.edu.unpa.calificacionesunpa.data.api.login.AuthResponse
import mx.edu.unpa.calificacionesunpa.data.api.login.LoginRequest
import mx.edu.unpa.calificacionesunpa.data.api.login.RegisterRequest
import mx.edu.unpa.calificacionesunpa.data.persistent.UsuarioService
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

// -------------------------
// Modelos de datos
// -------------------------






@Singleton
class LoginRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val api: AuthAPI,
    private val prefs: SharedPreferences
){

    companion object {
        private const val TAG = "AuthProvider"
        private const val PREFS_NAME = "auth_prefs"
        private const val KEY_TOKEN = "auth_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_EMAIL = "email"
        private const val KEY_EXPIRES_AT = "expires_at"
    }


    private val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()

    // -------------------------
    // Login
    // -------------------------
    /*suspend fun login(matricula: String, password: String): Result<AuthResponse> {
        return api.login(matricula, password).onSuccess { saveSession(it) }
    }*/

    suspend fun login(matricula: String, password: String): Result<AuthResponse> {
        return try {
            val response = api.login(LoginRequest(matricula, password))
            prefs.edit().putString("jwt_token", response.token).apply()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }



    suspend fun register(email: String, password: String): Result<AuthResponse> {
        return try {
            val response = api.register(RegisterRequest(email, password))
            saveSession(response) // si quieres guardar sesión
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // -------------------------
    // Método genérico para POST
    // -------------------------
    private suspend fun <T> postRequest(url: String, body: Any, responseType: Class<T>): Result<T> = withContext(Dispatchers.IO) {
        try {
            val json = gson.toJson(body)
            val request = Request.Builder()
                .url(url)
                .post(json.toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()


            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                val parsed = gson.fromJson(responseBody, responseType)
                Result.success(parsed)
            } else {
                Result.failure(Exception("HTTP ${response.code}: ${response.message}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error en postRequest", e)
            Result.failure(e)
        }
    }

    // -------------------------
    // Sesión y SharedPreferences
    // -------------------------
    private fun saveSession(authResponse: AuthResponse) {
        /*
        with(sharedPrefs.edit()) {
            putString(KEY_TOKEN, authResponse.token)
            putString(KEY_USER_ID, authResponse.userId)
            putString(KEY_EMAIL, authResponse.email)
            authResponse.expiresIn?.let {
                putLong(KEY_EXPIRES_AT, System.currentTimeMillis() + (it * 1000))
            }
            apply()
        }

         */
    }

    fun getId(): String = sharedPrefs.getString(KEY_USER_ID, "") ?: ""
    //fun getToken(): String = sharedPrefs.getString(KEY_TOKEN, "") ?: ""

    fun getToken(): String {
        return prefs.getString("jwt_token", "") ?: ""
    }

    fun exitSession() = sharedPrefs.edit().clear().apply()

    fun existsSession(): Boolean {
        val token = getToken()
        val userId = getId()
        return token.isNotEmpty()  && userId.isNotEmpty() && !isTokenExpired()
    }

    private fun isTokenExpired(): Boolean {
        val expiresAt = sharedPrefs.getLong(KEY_EXPIRES_AT, -1)
        return expiresAt < 0 || System.currentTimeMillis() > expiresAt
    }
}