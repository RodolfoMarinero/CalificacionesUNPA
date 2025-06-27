package mx.edu.unpa.calificacionesunpa.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import mx.edu.unpa.calificacionesunpa.R
import mx.edu.unpa.calificacionesunpa.models.PdfData
import java.text.SimpleDateFormat
import java.util.Locale

class DocumentosAdapter(
    private val items: List<PdfData>,
    private val onClick: (PdfData) -> Unit
) : RecyclerView.Adapter<DocumentosAdapter.DocViewHolder>() {

    inner class DocViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvName: TextView = view.findViewById(R.id.tvFileName)
        private val tvFecha: TextView = view.findViewById(R.id.tvFecha)
        private val cbSeleccionado: CheckBox = view.findViewById(R.id.cbSeleccionado)

        fun bind(meta: PdfData) {
            tvName.text = meta.id

            val fmt = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            tvFecha.text = fmt.format(meta.timestamp)

            cbSeleccionado.isChecked = meta.seleccionado

            // Evitar que el checkbox se vea interactivo
            cbSeleccionado.isClickable = false
            cbSeleccionado.isFocusable = false

            itemView.setOnClickListener { onClick(meta) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DocViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_documento, parent, false)
        return DocViewHolder(v)
    }

    override fun onBindViewHolder(holder: DocViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}
