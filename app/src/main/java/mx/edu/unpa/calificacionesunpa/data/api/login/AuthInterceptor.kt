package mx.edu.unpa.calificacionesunpa.data.api.login

import android.content.SharedPreferences
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val prefs: SharedPreferences
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        // 1. Recuperamos ambos valores
        val token = prefs.getString("jwt_token", null)
        val campus = prefs.getString("campus_id", null) // <-- NUEVO: Recuperamos el campus

        val requestBuilder = chain.request().newBuilder()

        // 2. Inyectamos el Token
        if (!token.isNullOrEmpty()) {
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }

        // 3. INYECTAMOS LA BANDERA DEL CAMPUS
        if (!campus.isNullOrEmpty()) {
            requestBuilder.addHeader("X-Campus-ID", campus)
        }

        return chain.proceed(requestBuilder.build())
    }
}