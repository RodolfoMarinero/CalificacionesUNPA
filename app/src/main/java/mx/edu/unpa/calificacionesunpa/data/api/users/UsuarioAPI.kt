package mx.edu.unpa.calificacionesunpa.data.api.users

import mx.edu.unpa.calificacionesunpa.data.model.PasswordRequest
import mx.edu.unpa.calificacionesunpa.data.model.PasswordResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.PUT
import retrofit2.http.Path

interface UsuarioAPI {
    @PUT("usuarios/cambiarpassword/{nombre}/")
    suspend fun cambiarPassword(
        @Path("nombre") nombre: String,
        @Body request: PasswordRequest
    ): Response<PasswordResponse>
}
