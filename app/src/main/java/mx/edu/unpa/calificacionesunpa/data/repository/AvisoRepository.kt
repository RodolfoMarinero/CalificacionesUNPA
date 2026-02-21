package mx.edu.unpa.calificacionesunpa.data.repository

import mx.edu.unpa.calificacionesunpa.data.api.avisos.AvisoAPI
import mx.edu.unpa.calificacionesunpa.models.Aviso
import javax.inject.Inject

class AvisoRepository @Inject constructor(
    private val api: AvisoAPI
) {

    suspend fun getAvisos(): Result<List<Aviso>> = try {
        Result.success(api.listarAvisos())
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getAviso(id: Int): Result<Aviso> = try {
        Result.success(api.obtenerAviso(id))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun addAviso(aviso: Aviso): Result<Aviso> = try {
        Result.success(api.crearAviso(aviso))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun updateAviso(id: Int, aviso: Aviso): Result<Aviso> = try {
        Result.success(api.actualizarAviso(id, aviso))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun deleteAviso(id: Int): Result<Unit> = try {
        api.eliminarAviso(id)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
