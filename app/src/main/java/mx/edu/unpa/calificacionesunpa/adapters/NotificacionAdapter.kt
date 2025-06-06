package mx.edu.unpa.calificacionesunpa.adapters
/*import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import mx.edu.unpa.calificacionesunpa.R
import mx.edu.unpa.calificacionesunpa.ui.view_holders.NotificationViewHolder

import java.text.SimpleDateFormat
import java.util.Locale

data class NotificationItem(
    val iconResId: Int = 0,
    val titulo: String = "",
    val mensaje: String = "",
    val esGlobal: Boolean = false,
    val timestamp: Long = 0L
) {
    constructor() : this(0, "", "", false, 0L) // Constructor sin argumentos requerido por Firebase
}




class NotificationAdapter(
    private val items: List<NotificationItem>,
    private val onClick: ((NotificationItem) -> Unit)? = null
) : RecyclerView.Adapter<NotificationViewHolder>() {

    private val dateFormatter = SimpleDateFormat("dd/MM/yy HH:mm", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.notificacion_item, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val item = items[position]
        holder.icon.setImageResource(item.iconResId)
        holder.title.text = item.titulo
        holder.message.text = item.mensaje
        holder.date.text = dateFormatter.format(item.timestamp)
        holder.itemView.setOnClickListener {
            onClick?.invoke(item)
        }
    }

    override fun getItemCount(): Int = items.size
}*/