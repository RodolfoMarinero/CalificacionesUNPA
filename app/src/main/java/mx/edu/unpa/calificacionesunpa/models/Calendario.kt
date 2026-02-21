package mx.edu.unpa.calificacionesunpa.models

//Esta clase permite registrar las fechas de examenes
//@IgnoreExtraProperties
data class Calendario(
    val materia: String = "",
    val e1: String = "", // fecha extra1
    val e2: String = "", // fecha extra2
    val esp: String = "", // fecha especial
    val f: String = "", //fecha examen final
    val p1: String = "", //fecha primerParcial
    val p2: String = "", // fecha segundo parcial
    val p3: String = "", //fecha tercer parcial
    val ciclo: String = "",
)
