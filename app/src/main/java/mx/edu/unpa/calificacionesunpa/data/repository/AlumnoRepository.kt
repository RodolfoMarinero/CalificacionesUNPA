package mx.edu.unpa.calificacionesunpa.data.repository

import mx.edu.unpa.calificacionesunpa.data.api.alumno.AlumnoAPIretrofit
import mx.edu.unpa.calificacionesunpa.models.Alumno
import mx.edu.unpa.calificacionesunpa.models.Calendario
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlumnoRepository  @Inject constructor(
    private val api: AlumnoAPIretrofit
)  {

    suspend fun getAlumnoConMaterias(usuarioId: String): Result<Alumno> {
        return try {
            val alumno = api.getAlumnoConMaterias(usuarioId).toDomainModel()
            Result.success(alumno)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun obtenerBanderaPrimerAcceso(usuarioId: String): Result<Boolean>{
        return try {
            val response = api.getPrimerAcceso(usuarioId)
            if (response.success && response.data != null) {
                Result.success(response.data.primerAcceso)
            } else {
                Result.failure(Exception(response.message ?: "Error desconocido"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }

    }

    suspend fun obtenerFechasExamenPorMateria(materiaId: String): Result<Calendario?>{
        return try {
            val response = api.getExamenPorMateria(materiaId)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Error desconocido"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}