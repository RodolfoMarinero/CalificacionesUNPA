package mx.edu.unpa.calificacionesunpa.data.repository

import mx.edu.unpa.calificacionesunpa.data.api.users.UsuarioAPI
import mx.edu.unpa.calificacionesunpa.data.model.PasswordRequest
import javax.inject.Inject

class UsuarioRepository @Inject constructor(
    private val api: UsuarioAPI
) {

    suspend fun cambiarPassword(nombre: String, actual: String, nueva: String): Result<String> {
        return try {
            val request = PasswordRequest(actual, nueva)
            val response = api.cambiarPassword(nombre, request)

            if (response.isSuccessful) {
                val body = response.body()
                Result.success(body?.message ?: "Operación exitosa")
            } else {
                val error = response.errorBody()?.string()
                Result.failure(Exception(error ?: "Error desconocido"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
