package mx.edu.unpa.calificacionesunpa.models

data class Aviso(
    val id: Int,
    val cicloId: String,
    val periodoId: String,
    val aviso: String,
    val fecha: String,   // puedes parsear a LocalDate si quieres
    val dirigir: Int     // 1=profesores, 2=alumnos, 3=todos
)
