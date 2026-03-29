package mx.edu.unpa.calificacionesunpa.ui.calificacionesanteriores

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import mx.edu.unpa.calificacionesunpa.R
import mx.edu.unpa.calificacionesunpa.models.Materia
import java.util.Locale

class CalificacionesAdapter(private var materias: List<Materia>) :
    RecyclerView.Adapter<CalificacionesAdapter.ViewHolder>() {

    private val expandedPositions = mutableSetOf<Int>()

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val layoutCabecera: LinearLayout = view.findViewById(R.id.layoutCabecera)
        val layoutDetalles: LinearLayout = view.findViewById(R.id.layoutDetalles)
        val layoutExtras: LinearLayout = view.findViewById(R.id.layoutExtras)

        val tvNombreMateria: TextView = view.findViewById(R.id.tvNombreMateria)
        val tvCalificacionDefinitiva: TextView = view.findViewById(R.id.tvCalificacionDefinitiva)
        val ivExpandir: ImageView = view.findViewById(R.id.ivExpandir)

        // Celdas de calificaciones
        val tvP1: TextView = view.findViewById(R.id.tvP1)
        val tvP2: TextView = view.findViewById(R.id.tvP2)
        val tvP3: TextView = view.findViewById(R.id.tvP3)
        val tvPP: TextView = view.findViewById(R.id.tvPP)
        val tvEF: TextView = view.findViewById(R.id.tvEF)

        // Celdas de extraordinarios
        val tvE1: TextView = view.findViewById(R.id.tvE1)
        val tvE2: TextView = view.findViewById(R.id.tvE2)
        val tvESP: TextView = view.findViewById(R.id.tvESP)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_calificacion, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val materia = materias[position]
        val c = materia.calificaciones

        // 1. Cabecera principal
        holder.tvNombreMateria.text = materia.materia
        holder.tvCalificacionDefinitiva.text = format(c.pFinal)

        // 2. Llenar columnas de ordinarios
        holder.tvP1.text = format(c.parcial1)
        holder.tvP2.text = format(c.parcial2)
        holder.tvP3.text = format(c.parcial3)
        holder.tvPP.text = format(materia.getPromedioParciales())
        holder.tvEF.text = format(c.ordinario)

        // 3. Manejo de Extraordinarios
        val tieneExtra = c.extra1 != null || c.extra2 != null || c.especial != null
        if (tieneExtra) {
            holder.layoutExtras.visibility = View.VISIBLE
            holder.tvE1.text = format(c.extra1)
            holder.tvE2.text = format(c.extra2)
            holder.tvESP.text = format(c.especial)
        } else {
            holder.layoutExtras.visibility = View.GONE
        }

        // 4. Lógica de Expandir / Contraer Animado
        val isExpanded = expandedPositions.contains(position)
        holder.layoutDetalles.visibility = if (isExpanded) View.VISIBLE else View.GONE
        holder.ivExpandir.rotation = if (isExpanded) 180f else 0f

        holder.layoutCabecera.setOnClickListener {
            if (isExpanded) {
                expandedPositions.remove(position)
            } else {
                expandedPositions.add(position)
            }
            notifyItemChanged(position)
        }
    }

    override fun getItemCount() = materias.size

    fun actualizarDatos(nuevasMaterias: List<Materia>) {
        materias = nuevasMaterias
        expandedPositions.clear()
        notifyDataSetChanged()
    }

    private fun format(v: Double?): String {
        if (v == null || v == 0.0) return "-"
        return when (v) {
            11.0 -> "NP"
            12.0 -> "SD"
            else -> String.format(Locale.getDefault(), "%.1f", v)
        }
    }
}