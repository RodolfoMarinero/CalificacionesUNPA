package mx.edu.unpa.calificacionesunpa.data.model

import com.google.gson.annotations.SerializedName

/**
 * Respuesta de verificación de ventana
 */
data class VentanaSolicitud(
    @SerializedName("abierta") val abierta: Boolean,
    @SerializedName("mensaje") val mensaje: String,
    @SerializedName("proximaApertura") val proximaApertura: String
)

/**
 * Información del tutor actual
 */
data class TutorAsignado(
    @SerializedName("tieneTutor") val tieneTutor: Boolean,
    @SerializedName("nombreTutor") val nombreTutor: String? = null,
    @SerializedName("correoTutor") val correoTutor: String? = null,
    @SerializedName("carreraTutor") val carreraTutor: String? = null,
    @SerializedName("tipoAsignacion") val tipoAsignacion: String? = null,
    @SerializedName("fechaAsignacion") val fechaAsignacion: String? = null,
    @SerializedName("mensaje") val mensaje: String? = null,
    @SerializedName("estadoTutor") val estadoTutor: String? = null
)

/**
 * Docente disponible para selección
 */
data class DocenteDisponible(
    @SerializedName("id") val id: Long,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("correo") val correo: String,
    @SerializedName("carrera") val carrera: String,
    @SerializedName("tutoradosActuales") val tutoradosActuales: Long,
    @SerializedName("maxTutorados") val maxTutorados: Int,
    @SerializedName("nivelAfinidad") val nivelAfinidad: Int = 999
) {
    val disponibilidad: String
        get() = "$tutoradosActuales/$maxTutorados"

    val porcentajeCarga: Int
        get() = ((tutoradosActuales.toFloat() / maxTutorados) * 100).toInt()
}

/**
 * Request para solicitar tutor
 */
data class SolicitudTutorRequest(
    @SerializedName("matricula") val matricula: String,
    @SerializedName("docenteId") val docenteId: Long,
    @SerializedName("periodo") val periodo: String,
    @SerializedName("motivo") val motivo: String?
)

/**
 * Respuesta de solicitud
 */
data class SolicitudResponse(
    @SerializedName("exito") val exito: Boolean,
    @SerializedName("mensaje") val mensaje: String,
    @SerializedName("tutorSolicitado") val tutorSolicitado: String? = null,
    @SerializedName("periodo") val periodo: String? = null,
    @SerializedName("error") val error: String? = null,
    @SerializedName("requiereMotivo")  val requiereMotivo: Boolean?,
)
/**
 * Item del historial de tutores
 */
data class HistorialTutor(
    @SerializedName("periodo") val periodo: String,
    @SerializedName("nombreTutor") val nombreTutor: String,
    @SerializedName("correoTutor") val correoTutor: String,
    @SerializedName("carreraTutor") val carreraTutor: String,
    @SerializedName("tipoAsignacion") val tipoAsignacion: String,
    @SerializedName("fechaAsignacion") val fechaAsignacion: String
)
data class ActualizarCorreoRequest(
    @SerializedName("matricula") val matricula: String,
    @SerializedName("correo") val correo: String
)


data class ActualizarCorreoResponse(
    @SerializedName("exito") val exito: Boolean,
    @SerializedName("mensaje") val mensaje: String,
    @SerializedName("correo") val correo: String? = null,
    @SerializedName("error") val error: String? = null
)