package mx.edu.unpa.calificacionesunpa.utils

import mx.edu.unpa.calificacionesunpa.models.Materia
import kotlin.math.round

object PromedioCalculator {

    /**
     * Calcula el promedio general ignorando materias que aún no tienen calificación final.
     * Esto evita que el promedio baje artificialmente al inicio del semestre.
     */
    fun calcularPromedioGeneral(materias: List<Materia>): Double {
        // 1. Filtramos solo las materias que NO son nulas y son mayores a 0
        // (Asumiendo que 0.0 es una calificación válida, pero 'null' es "no calificada aún")
        val materiasCalificadas = materias.filter { it.calificaciones.pFinal != null }

        if (materiasCalificadas.isEmpty()) return 0.0

        // 2. Sumamos solo lo que ya está calificado
        val suma = materiasCalificadas.sumOf { it.calificaciones.pFinal!! }

        // 3. Dividimos entre el número de materias calificadas, NO entre el total
        val promedio = suma / materiasCalificadas.size

        return promedio
    }

    /**
     * Calcula el promedio de un semestre específico.
     * Si una materia del semestre falta, podrías querer que regrese 0.0 o el promedio parcial.
     * Aquí lo ajustamos para que sea consistente con el general.
     */
    fun calcularPromedioSemestre(materias: List<Materia>): Double {
        return calcularPromedioGeneral(materias)
    }

    /**
     * Redondea a un decimal (Ej: 8.56 -> 8.6)
     */
    private fun redondear(valor: Double): Double {
        return round(valor * 10) / 10.0
    }
}