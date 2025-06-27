package mx.edu.unpa.calificacionesunpa.adapters
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.type.Date
import mx.edu.unpa.calificacionesunpa.R
import mx.edu.unpa.calificacionesunpa.ui.view_holders.NotificationViewHolder

import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Locale

data class NotificationItem(
    val id: String = "",
    val iconResId: Int = R.drawable.notification,
    val titulo: String = "",
    val mensaje: String = "",
    val esGlobal: Boolean = false,
    val destinatarios: List<String> = listOf(),
    val expiraEn: String = "",
    val timestamp: java.util.Date = java.util.Date(),

    val fueLeida : Boolean = true,
    val remitente: String = ""
) {
    constructor() : this(id = "",0, "", "", false, listOf(""), LocalDate.now().plusDays(3).toString(), java.util.Date(), false) // Constructor sin argumentos requerido por Firebase
}




class NotificationAdapter(
    private val items: List<NotificationItem>,
    private val onClick: ((NotificationItem) -> Unit)? = null,
    private val onDelete: ((NotificationItem) -> Unit)? = null
) : RecyclerView.Adapter<NotificationViewHolder>() {

    private val dateFormatter = SimpleDateFormat("dd/MM/yy HH:mm", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.notification_item, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val item = items[position]
        if (item.iconResId != 0) {
            holder.icon.setImageResource(item.iconResId)
        } else {
            holder.icon.setImageResource(R.drawable.notification) // valor por defecto
        }
        holder.title.text = item.titulo
        holder.message.text = item.mensaje
        holder.date.text = dateFormatter.format(item.timestamp)
        holder.itemView.setOnClickListener {
            onClick?.invoke(item)
        }
        holder.sender.text = "Enviado por: ${item.remitente}"

        holder.btnEliminar.setOnClickListener {
            onDelete?.invoke(item)
        }
    }

    override fun getItemCount(): Int = items.size
}