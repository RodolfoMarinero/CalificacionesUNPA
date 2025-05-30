package mx.edu.unpa.calificacionesunpa.models


import Calificacion
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.IgnoreExtraProperties

/**
 * Representa una materia con calificaciones y referencia al ciclo escolar.
 * Se utiliza una clase regular con un constructor vacío para compatibilidad con Firestore.
 */
@IgnoreExtraProperties
class Materia() {
    var activo: Boolean = false
    var calificaciones: Calificacion = Calificacion()
    var ciclo: DocumentReference? = null
    var materia: String = ""
    var semestre: Int=-1;

    /** Constructor secundario para crear instancias manualmente */
    constructor(
        activo: Boolean,
        calificacion: Calificacion,
        ciclo: DocumentReference?,
        materia: String,
        semestre: Int
    ) : this() {
        this.activo = activo
        this.calificaciones = calificacion
        this.ciclo = ciclo
        this.materia = materia
        this.semestre = semestre
    }

    fun getPromedioParciales(): Double {
        // Verifica si hay calificaciones
        var parcial1 = calificaciones.parcial1;
        var parcial2 = calificaciones.parcial2;
        var parcial3 = calificaciones.parcial3;
        if (parcial1==null|| parcial2==null || parcial3==null) {
            return 0.0 // Si no hay calificaciones, retorna 0
        }
        return ((parcial1 + parcial2 + parcial3) / 3.0).toDouble()
    }


}

