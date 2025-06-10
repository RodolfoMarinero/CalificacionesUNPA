package mx.edu.unpa.calificacionesunpa.service

import mx.edu.unpa.calificacionesunpa.models.Materia
import kotlin.math.round

object PromedioCalculatorService {

    var promedioGeneral: Double = 0.0
        private set

    fun calcularPromedioGeneral(materias: List<Materia>): Double {
        if (materias.isEmpty()) {
            promedioGeneral = 0.0
            return promedioGeneral
        }

        val materiasPorSemestre = materias.groupBy { it.semestre }

        val promediosPorSemestre = materiasPorSemestre.values.map { calcularPromedioSemestre(it) }
            .filter { it > 0 }

        promedioGeneral = if (promediosPorSemestre.isEmpty()) 0.0
        else redondear(promediosPorSemestre.average())

        return promedioGeneral
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
