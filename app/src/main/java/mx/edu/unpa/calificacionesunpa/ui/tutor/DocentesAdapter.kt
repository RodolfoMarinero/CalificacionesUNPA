package mx.edu.unpa.calificacionesunpa.ui.tutor

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import mx.edu.unpa.calificacionesunpa.databinding.ItemDocenteDisponibleBinding
import mx.edu.unpa.calificacionesunpa.data.model.DocenteDisponible

class DocentesAdapter(
    private val onDocenteClick: (DocenteDisponible) -> Unit
) : ListAdapter<DocenteDisponible, DocentesAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDocenteDisponibleBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding, onDocenteClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        private val binding: ItemDocenteDisponibleBinding,
        private val onDocenteClick: (DocenteDisponible) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(docente: DocenteDisponible) {
            binding.apply {
                tvNombreDocente.text = docente.nombre
                tvCarreraDocente.text = docente.carrera
                tvDisponibilidad.text = docente.disponibilidad

                // Configurar ProgressBar
                progressCarga.max = 100
                progressCarga.progress = docente.porcentajeCarga

                // Cambiar color según carga
                val color = when {
                    docente.porcentajeCarga < 50 -> android.graphics.Color.parseColor("#10b981")  // Verde
                    docente.porcentajeCarga < 80 -> android.graphics.Color.parseColor("#f59e0b")  // Naranja
                    else -> android.graphics.Color.parseColor("#ef4444")  // Rojo
                }
                progressCarga.progressTintList = android.content.res.ColorStateList.valueOf(color)

                // Click en la card
                root.setOnClickListener {
                    onDocenteClick(docente)
                }
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<DocenteDisponible>() {
        override fun areItemsTheSame(oldItem: DocenteDisponible, newItem: DocenteDisponible): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: DocenteDisponible, newItem: DocenteDisponible): Boolean {
            return oldItem == newItem
        }
    }
}