package mx.edu.unpa.calificacionesunpa.ui.view_holders
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import mx.edu.unpa.calificacionesunpa.R

class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    // Referencia al CardView para cambiar el color de fondo
    val card: androidx.cardview.widget.CardView = itemView.findViewById(R.id.cardNotificacion)

    // Icono (ivIconoNotificacion en el XML)
    val icon: ImageView = itemView.findViewById(R.id.ivIconoNotificacion)

    // Título (tvTituloNotificacion en el XML)
    val title: TextView = itemView.findViewById(R.id.tvTituloNotificacion)

    // Fecha/Tiempo (tvTiempoNotificacion en el XML)
    val time: TextView = itemView.findViewById(R.id.tvTiempoNotificacion)

}