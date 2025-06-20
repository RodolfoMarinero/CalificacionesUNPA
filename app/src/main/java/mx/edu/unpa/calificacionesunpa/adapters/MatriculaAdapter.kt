package mx.edu.unpa.calificacionesunpa.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import mx.edu.unpa.calificacionesunpa.R

class MatriculaAdapter(
    private val matriculas: List<String>,
    private val onEliminarClick: (String) -> Unit
) : RecyclerView.Adapter<MatriculaAdapter.MatriculaViewHolder>() {

    inner class MatriculaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtMatricula: TextView = itemView.findViewById(R.id.txtMatriculaItem)
        val btnEliminar: ImageView = itemView.findViewById(R.id.btnEliminarMatricula)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MatriculaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_matricula, parent, false)
        return MatriculaViewHolder(view)
    }

    override fun onBindViewHolder(holder: MatriculaViewHolder, position: Int) {
        val matricula = matriculas[position]
        holder.txtMatricula.text = matricula
        holder.btnEliminar.setOnClickListener {
            onEliminarClick(matricula)
        }
    }

    override fun getItemCount(): Int = matriculas.size
}
