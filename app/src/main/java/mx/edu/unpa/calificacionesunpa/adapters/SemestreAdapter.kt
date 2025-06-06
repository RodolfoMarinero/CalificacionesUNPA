package com.unpa.calificaciones.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import mx.edu.unpa.calificacionesunpa.R

class SemestreAdapter(
    private val semestresMap: Map<Int, String>,
    private val onItemClick: (Int) -> Unit
) : RecyclerView.Adapter<SemestreAdapter.SemestreViewHolder>() {

    private val semestreList = semestresMap.keys.sorted().reversed()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SemestreViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.semestre_item, parent, false)
        return SemestreViewHolder(view)
    }

    override fun getItemCount(): Int = semestreList.size

    override fun onBindViewHolder(holder: SemestreViewHolder, position: Int) {
        val semestre = semestreList[position]
        val cicloId = semestresMap[semestre] ?: "no sé"
        holder.bind(cicloId, "10", onItemClick, semestre)
    }

    inner class SemestreViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtSemestre: TextView = itemView.findViewById(R.id.txtSemestreLabel)
        private val txtPromedio: TextView = itemView.findViewById(R.id.txtPromedioSemestre)

        fun bind(cicloId: String, promedio: String, onItemClick: (Int) -> Unit, semestreNum: Int) {
            txtSemestre.text = cicloId
            txtPromedio.text = promedio
            itemView.setOnClickListener {
                onItemClick(semestreNum)
            }
        }
    }
}
