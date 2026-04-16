package mx.edu.unpa.calificacionesunpa.ui.calendarioexamenes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.applandeo.materialcalendarview.EventDay
import mx.edu.unpa.calificacionesunpa.R
import mx.edu.unpa.calificacionesunpa.data.persistent.UsuarioService
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import com.applandeo.materialcalendarview.CalendarView
import com.applandeo.materialcalendarview.listeners.OnDayClickListener

class CalendarioFragment : Fragment() {

    private lateinit var rvExamenes: RecyclerView
    private lateinit var adapter: ExamenesAdapter
    private lateinit var tvFechaActual: TextView
    private lateinit var tvSemestreCalendario: TextView
    private lateinit var calendarView: CalendarView

    // Lista maestra de todos los exámenes del semestre
    private var todosLosEventos = mutableListOf<EventoExamen>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.calendario_examenes, container, false)

        // 1. Enlazar Vistas
        rvExamenes = root.findViewById(R.id.rvExamenesDelMes)
        tvFechaActual = root.findViewById(R.id.tvFechaActual)
        tvSemestreCalendario = root.findViewById(R.id.tvSemestreCalendario)
        calendarView = root.findViewById(R.id.calendarView)

        // 2. Configurar la Fecha de Hoy
        val sdf = SimpleDateFormat("dd-MMMM-yyyy", Locale("es", "MX"))
        val fechaDeHoy = sdf.format(Calendar.getInstance().time)
        tvFechaActual.text = fechaDeHoy.replaceFirstChar { it.uppercase() }

        // 3. Configurar el RecyclerView
        adapter = ExamenesAdapter(emptyList())
        rvExamenes.adapter = adapter

        // 4. EXTRAER DATOS REALES DE LA API (UsuarioService)
        procesarFechasDelApi(root)

        return root
    }

    private val eventosCalendario = mutableListOf<EventDay>()

    private fun procesarFechasDelApi(root: View) {
        val alumno = UsuarioService.alumnoActual
        if (alumno?.materias == null || alumno.materias!!.isEmpty()) return

        val materias = alumno.materias!!
        val cicloActual = materias.mapNotNull { it.ciclo }.maxOrNull() ?: "Sin Ciclo"
        tvSemestreCalendario.text = cicloActual

        todosLosEventos.clear()
        eventosCalendario.clear()

        materias.filter { it.ciclo == cicloActual }.forEach { materia ->
            materia.calendarioExamenes?.let { cal ->
                addEvento(cal.p1, materia.materia, "1er Parcial")
                addEvento(cal.p2, materia.materia, "2do Parcial")
                addEvento(cal.p3, materia.materia, "3er Parcial")
                addEvento(cal.f, materia.materia, "Ordinario")
                addEvento(cal.e1, materia.materia, "Extra. 1")
                addEvento(cal.e2, materia.materia, "Extra. 2")
                addEvento(cal.esp, materia.materia, "Especial")
            }
        }

        todosLosEventos.sortBy { it.fechaRaw }
        adapter.actualizarDatos(todosLosEventos)

        // 🔥 AHORA SÍ: Buscamos el calendario usando "root"
        val calView = root.findViewById<com.applandeo.materialcalendarview.CalendarView>(R.id.calendarView)
        calView.setEvents(eventosCalendario)
        // 🔥 MAGIA DE INTERACCIÓN: Click en el calendario 🔥
        // 🔥 MAGIA DE INTERACCIÓN: Popup Institucional Personalizado 🔥
        calView.setOnDayClickListener(object : com.applandeo.materialcalendarview.listeners.OnDayClickListener {
            override fun onDayClick(eventDay: EventDay) {
                val clickedCalendar = eventDay.calendar

                // Convertimos la fecha tocada a nuestro formato "2026-04-06"
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val clickedDateRaw = sdf.format(clickedCalendar.time)

                // Buscamos TODOS los exámenes de ese día (por si hay más de uno)
                val examenesDelDia = todosLosEventos.filter { it.fechaRaw == clickedDateRaw }

                if (examenesDelDia.isNotEmpty()) {
                    // 1. Inflamos el layout personalizado
                    val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_examenes, null)
                    val tvDialogTitle = dialogView.findViewById<TextView>(R.id.tvDialogTitle)
                    val tvDialogContent = dialogView.findViewById<TextView>(R.id.tvDialogContent)
                    val btnDialogEntendido = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnDialogEntendido)
                    val btnDialogClose = dialogView.findViewById<ImageView>(R.id.btnDialogClose)

                    // 2. Llenamos los datos dinámicos
                    tvDialogTitle.text = "📅 Exámenes del ${examenesDelDia[0].dia} de ${examenesDelDia[0].mesStr}"

                    val mensaje = java.lang.StringBuilder()
                    for (examen in examenesDelDia) {
                        mensaje.append("📚 Materia: ${examen.materia}\n")
                        mensaje.append("📝 Tipo: ${examen.tipoExamen}\n\n")
                    }
                    tvDialogContent.text = mensaje.toString().trim()

                    // 3. Creamos y mostramos el Diálogo
                    val dialog = com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                        .setView(dialogView)
                        .create()

                    // Ocultamos el fondo del MaterialAlertDialog para que solo se vea nuestro diseño redondeado
                    dialog.window?.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))

                    dialog.show()

                    // 4. Listeners para cerrar
                    btnDialogEntendido.setOnClickListener { dialog.dismiss() }
                    btnDialogClose.setOnClickListener { dialog.dismiss() }
                } else {
                    Toast.makeText(requireContext(), "No hay exámenes en esta fecha", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun addEvento(fechaRaw: String?, nombreMateria: String, tipoExamen: String) {
        if (!fechaRaw.isNullOrEmpty()) {
            try {
                // Parseamos la fecha que viene de la API (Ej: "2026-04-06")
                val sdfIn = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val date = sdfIn.parse(fechaRaw)

                if (date != null) {
                    val sdfDia = SimpleDateFormat("dd", Locale.getDefault())
                    val sdfMes = SimpleDateFormat("MMM", Locale("es", "MX"))

                    val dia = sdfDia.format(date)
                    val mesStr = sdfMes.format(date).replaceFirstChar { it.uppercase() } // Ej: "Abr"

                    // Agregamos a la lista de abajo
                    todosLosEventos.add(EventoExamen(fechaRaw, dia, mesStr, nombreMateria, tipoExamen))

                    // Agregamos el punto morado al Calendario de arriba
                    val calendar = Calendar.getInstance()
                    calendar.time = date
                    eventosCalendario.add(EventDay(calendar, R.drawable.ic_dot_purple))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}