package mx.edu.unpa.calificacionesunpa.data.model

import mx.edu.unpa.calificacionesunpa.R
import java.time.LocalDate

data class NotificationItem(
    val id: String = "",
    val iconResId: Int = R.drawable.notification,
    val titulo: String = "",
    val mensaje: String = "",
    val esGlobal: Boolean = false,
    val destinatarios: List<String> = listOf(),
    val expiraEn: String = "",
    //val timestamp: Long = java.util.Date(),

    val fueLeida: Boolean = true,
    val remitente: String = ""
) {
    constructor() : this(id = "",0, "", "", false, listOf(""), LocalDate.now().plusDays(3).toString(),  false) // Constructor sin argumentos requerido por Firebase
}