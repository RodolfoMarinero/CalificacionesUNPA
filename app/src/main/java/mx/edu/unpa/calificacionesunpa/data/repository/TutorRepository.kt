package mx.edu.unpa.calificacionesunpa.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mx.edu.unpa.calificacionesunpa.data.api.TutorAPI
import mx.edu.unpa.calificacionesunpa.data.model.*

class TutorRepository(private val api: TutorAPI) {

    suspend fun verificarVentana(periodo: String): Result<VentanaSolicitud> = withContext(Dispatchers.IO) {
        try {
            val response = api.verificarVentana(periodo)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    suspend fun verificarVentanaTest(periodo: String): Result<VentanaSolicitud> = withContext(Dispatchers.IO) {
        try {
            val response = api.verificarVentanaTest(periodo)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    suspend fun obtenerMiTutor(matricula: String, periodo: String): Result<TutorAsignado> = withContext(Dispatchers.IO) {
        try {
            val response = api.obtenerMiTutor(matricula, periodo)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun obtenerDocentesDisponibles(matricula: String, periodo: String): Result<List<DocenteDisponible>> = withContext(Dispatchers.IO) {
        try {
            val response = api.obtenerDocentesDisponibles(matricula, periodo)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun solicitarTutor(request: SolicitudTutorRequest): Result<SolicitudResponse> = withContext(Dispatchers.IO) {
        try {
            val response = api.solicitarTutor(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    suspend fun obtenerHistorial(matricula: String): Result<List<HistorialTutor>> = withContext(Dispatchers.IO) {
        try {
            val response = api.obtenerHistorial(matricula)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun actualizarCorreo(request: ActualizarCorreoRequest): Result<ActualizarCorreoResponse> = withContext(Dispatchers.IO) {
        try {
            val response = api.actualizarCorreo(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}