package mx.edu.unpa.calificacionesunpa.models


import Calificacion
class Materia() {
    var clave: String=""
    var materia: String = ""
    var semestre: Int=-1;
    var activo: Boolean = false
    var ciclo: String? = null
    var calendarioExamenes : Calendario? = null;
    var calificaciones: Calificacion = Calificacion()

    /** Constructor secundario para crear instancias manualmente */
    constructor(
        clave: String,
        activo: Boolean,
        calificacion: Calificacion,
        ciclo: String?,
        materia: String,
        semestre: Int,
        calendario : Calendario?
    ) : this() {
        this.clave=clave
        this.activo = activo
        this.calificaciones = calificacion
        this.ciclo = ciclo
        this.materia = materia
        this.semestre = semestre
        this.calendarioExamenes = calendario
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

