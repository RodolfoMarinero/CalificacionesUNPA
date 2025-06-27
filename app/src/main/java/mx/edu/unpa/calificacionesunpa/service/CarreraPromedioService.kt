package mx.edu.unpa.calificacionesunpa.service

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import mx.edu.unpa.calificacionesunpa.models.Alumno
import mx.edu.unpa.calificacionesunpa.models.Materia
import mx.edu.unpa.calificacionesunpa.providers.AlumnoProvider

object CarreraPromedioService {

    private val db = FirebaseFirestore.getInstance()

    suspend fun obtenerPromedioCarrera(): Double {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return 0.0

        return try {
            val alumno = getAlumnoSuspend(userId) ?: return 0.0

            val materias = alumno.materias?.filter { it.calificaciones.pFinal != null } ?: emptyList()

            PromedioCalculatorService.calcularPromedioGeneral(materias)
        } catch (e: Exception) {
            Log.e("CarreraPromedioService", "Error al obtener promedio: ${e.message}")
            0.0
        }
    }
    suspend fun getAlumnoSuspend(usuarioId: String): Alumno? = suspendCancellableCoroutine { cont ->
        val alumnoProvider = AlumnoProvider()

        alumnoProvider.obtenerAlumnoConMateriasDeUsuario(usuarioId) { alumno ->
            cont.resume(alumno, null)
        }
    }

}
