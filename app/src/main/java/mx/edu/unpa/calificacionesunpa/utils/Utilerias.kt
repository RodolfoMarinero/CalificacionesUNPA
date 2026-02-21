package mx.edu.unpa.calificacionesunpa.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import mx.edu.unpa.calificacionesunpa.models.Materia

object Utilerias {
    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    fun calcularPromedioMaterias(materias: List<Materia>): Double {
        val materiasConCalificacion = materias.filter { it.calificaciones.pFinal != null }
        return if (materiasConCalificacion.isNotEmpty()) {
            PromedioCalculator.calcularPromedioGeneral(materiasConCalificacion)
        } else 0.0
    }
}
