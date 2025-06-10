package mx.edu.unpa.calificacionesunpa.models


data class Calendario(
    val materia: String,
    val primerParcial: String,
    val segundoParcial: String,
    val tercerParcial: String,
    val ordinario: String,
    val extra1: String,
    val extra2: String,
    val especial: String
)
