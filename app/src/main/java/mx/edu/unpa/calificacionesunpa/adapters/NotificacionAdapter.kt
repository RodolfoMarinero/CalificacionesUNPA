package mx.edu.unpa.calificacionesunpa.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import mx.edu.unpa.calificacionesunpa.R
import mx.edu.unpa.calificacionesunpa.models.Aviso
import mx.edu.unpa.calificacionesunpa.ui.view_holders.NotificationViewHolder

class NotificationAdapter(
    private val items: List<Aviso>,
    private val onClick: ((Aviso) -> Unit)? = null,
    private val onDelete: ((Aviso) -> Unit)? = null
) : RecyclerView.Adapter<NotificationViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.notification_item, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val item = items[position]

        // 1. Asignamos los textos (Aviso y Fecha)
        holder.title.text = item.aviso
        holder.time.text = item.fecha

        // 2. 🔥 Lógica de Estilo (Colores e Íconos) basada en tu diseño de Figma
        // 1 = Profesores, 2 = Alumnos, 3 = Todos
        val (colorHex, iconoRes) = when (item.dirigir) {
            1 -> "#4B0082" to R.drawable.ic_assignment   // Morado (Profesores)
            2 -> "#004080" to R.drawable.ic_book         // Azul (Alumnos)
            3 -> {
                // Si es para todos pero contiene palabras de alerta, lo ponemos en rojo
                if (item.aviso.contains("suspensi", ignoreCase = true) ||
                    item.aviso.contains("urgente", ignoreCase = true)) {
                    "#8B0000" to R.drawable.ic_warning   // Rojo (Urgente)
                } else {
                    "#B8860B" to R.drawable.ic_event     // Dorado (General)
                }
            }
            else -> "#2E7D32" to R.drawable.ic_notifications // Verde (Otros)
        }

        // 3. Aplicamos el color al CardView y el ícono al ImageView
        holder.card.setCardBackgroundColor(Color.parseColor(colorHex))
        holder.icon.setImageResource(iconoRes)

        // 4. Listeners (Click en la píldora y botón eliminar)
        holder.itemView.setOnClickListener {
            onClick?.invoke(item)
        }
    }

    override fun getItemCount(): Int = items.size
}