package mx.edu.unpa.calificacionesunpa.ui.tutor

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import mx.edu.unpa.calificacionesunpa.databinding.ItemHistorialTutorBinding
import mx.edu.unpa.calificacionesunpa.data.model.HistorialTutor
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class HistorialAdapter : ListAdapter<HistorialTutor, HistorialAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemHistorialTutorBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        private val binding: ItemHistorialTutorBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: HistorialTutor) {
            binding.apply {
                tvPeriodo.text = item.periodo
                tvNombreTutor.text = item.nombreTutor
                tvCarreraTutor.text = item.carreraTutor
                tvTipoAsignacion.text = item.tipoAsignacion

                // Formatear fecha
                try {
                    val fecha = LocalDateTime.parse(item.fechaAsignacion)
                    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                    tvFechaAsignacion.text = fecha.format(formatter)
                } catch (e: Exception) {
                    tvFechaAsignacion.text = item.fechaAsignacion.substring(0, 10)
                }
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<HistorialTutor>() {
        override fun areItemsTheSame(oldItem: HistorialTutor, newItem: HistorialTutor): Boolean {
            return oldItem.periodo == newItem.periodo
        }

        override fun areContentsTheSame(oldItem: HistorialTutor, newItem: HistorialTutor): Boolean {
            return oldItem == newItem
        }
    }
}