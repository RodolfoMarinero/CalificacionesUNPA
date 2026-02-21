package mx.edu.unpa.calificacionesunpa.utils

import mx.edu.unpa.calificacionesunpa.models.Materia
import kotlin.math.round

object PromedioCalculator {

    var promedioGeneral: Double = 0.0
        private set

    fun calcularPromedioGeneral(materias: List<Materia>): Double {
        if (materias.isEmpty()) return 0.0

        val suma = materias.sumOf { it.calificaciones.pFinal ?: 0.0 }
        val promedio = suma / materias.size
        return redondear(promedio)
    }

    fun calcularPromedioSemestre(materias: List<Materia>): Double {
        if (materias.any { it.calificaciones.pFinal == null }) return 0.0

        val suma = materias.sumOf { it.calificaciones.pFinal ?: 0.0 }
        val promedio = suma / materias.size
        return redondear(promedio)
    }

    private fun redondear(valor: Double): Double {
        return round(valor * 10) / 10.0
    }
}