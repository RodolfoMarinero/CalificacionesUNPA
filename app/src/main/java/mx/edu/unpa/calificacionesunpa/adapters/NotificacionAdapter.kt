package mx.edu.unpa.calificacionesunpa.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import mx.edu.unpa.calificacionesunpa.R
import mx.edu.unpa.calificacionesunpa.models.Aviso
import mx.edu.unpa.calificacionesunpa.ui.view_holders.NotificationViewHolder
import java.text.SimpleDateFormat
import java.util.Locale


class NotificationAdapter(
    private val items: List<Aviso>,
    private val onClick: ((Aviso) -> Unit)? = null,
    private val onDelete: ((Aviso) -> Unit)? = null
) : RecyclerView.Adapter<NotificationViewHolder>() {

    private val dateFormatter = SimpleDateFormat("dd/MM/yy HH:mm", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.notification_item, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val item = items[position]
        /*if (item.iconResId != 0) {
            holder.icon.setImageResource(item.iconResId)
        } else {
            holder.icon.setImageResource(R.drawable.notification) // valor por defecto
        }*/
        holder.title.text = "aviso"
        holder.message.text = item.aviso
        //holder.date.text = dateFormatter.format(item.timestamp)
        holder.itemView.setOnClickListener {
            onClick?.invoke(item)
        }
        //holder.sender.text = "Enviado por: ${item.remitente}"
        holder.sender.text = "Enviado por: Servicios Escolares"

        holder.btnEliminar.setOnClickListener {
            onDelete?.invoke(item)
        }
    }

    override fun getItemCount(): Int = items.size
}