package mx.edu.unpa.calificacionesunpa.ui.view_holders


import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import mx.edu.unpa.calificacionesunpa.R

class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val icon: ImageView = itemView.findViewById(R.id.ivNotificationIcon)
    val title: TextView = itemView.findViewById(R.id.tvNotificationTitle)
    val message: TextView = itemView.findViewById(R.id.tvNotificationMessage)
    val date: TextView = itemView.findViewById(R.id.tvNotificationDate)
    val sender: TextView = itemView.findViewById(R.id.tvNotificationSender)
    val btnEliminar: ImageButton   = itemView.findViewById(R.id.btnEliminar)
}