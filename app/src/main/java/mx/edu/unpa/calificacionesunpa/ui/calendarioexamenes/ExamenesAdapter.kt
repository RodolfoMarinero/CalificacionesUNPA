package mx.edu.unpa.calificacionesunpa.ui.calendarioexamenes

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import mx.edu.unpa.calificacionesunpa.R

data class EventoExamen(
    val fechaRaw: String,
    val dia: String,
    val mesStr: String,
    val materia: String,
    val tipoExamen: String
)

class ExamenesAdapter(private var examenes: List<EventoExamen>) :
    RecyclerView.Adapter<ExamenesAdapter.ExamenViewHolder>() {

    // Variable para saber qué tarjeta está tocada
    private var posicionResaltada = -1

    class ExamenViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val rootTarjeta: LinearLayout = view.findViewById(R.id.rootTarjeta)
        val tvDiaExamen: TextView = view.findViewById(R.id.tvDiaExamen)
        val tvMesExamen: TextView = view.findViewById(R.id.tvMesExamen) // El nuevo TextView
        val tvDescripcionExamen: TextView = view.findViewById(R.id.tvDescripcionExamen)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExamenViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_examen, parent, false)
        return ExamenViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExamenViewHolder, position: Int) {
        val examen = examenes[position]

        holder.tvDiaExamen.text = examen.dia
        holder.tvMesExamen.text = examen.mesStr // Ahora sí pasamos el mes
        holder.tvDescripcionExamen.text = "${examen.tipoExamen} - ${examen.materia}"

        // 🔥 EFECTO DE RESALTADO 🔥
        if (position == posicionResaltada) {
            // Se vuelve un morado más claro y se hace un 5% más grande
            holder.rootTarjeta.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#BA55D3"))
            holder.rootTarjeta.scaleX = 1.05f
            holder.rootTarjeta.scaleY = 1.05f
            holder.rootTarjeta.elevation = 8f
        } else {
            // Morado normal
            holder.rootTarjeta.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#800080"))
            holder.rootTarjeta.scaleX = 1.0f
            holder.rootTarjeta.scaleY = 1.0f
            holder.rootTarjeta.elevation = 2f
        }
    }

    override fun getItemCount() = examenes.size

    fun actualizarDatos(nuevosExamenes: List<EventoExamen>) {
        examenes = nuevosExamenes
        posicionResaltada = -1 // Limpiamos el resaltado al cambiar de semestre
        notifyDataSetChanged()
    }

    // Función para que el fragmento nos diga cuál resaltar
    fun resaltarItem(position: Int) {
        posicionResaltada = position
        notifyDataSetChanged() // Refresca la lista para aplicar colores
    }
}