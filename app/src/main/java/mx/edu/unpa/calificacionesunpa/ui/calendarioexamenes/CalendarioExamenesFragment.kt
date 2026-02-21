package mx.edu.unpa.calificacionesunpa.ui.calendarioexamenes

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import mx.edu.unpa.calificacionesunpa.R

//ARIEL LISTO, YA FUNCIONA CON UN MOCK, FALTA CARGAR DESDE UNA API
class CalendarioFragment : Fragment() {

    private val viewModel: CalendarioExamenesViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.calendario_examenes, container, false)
        agregarCalendariosATabla(view.findViewById(R.id.tablaCalendario))
        return view
    }


    fun agregarCalendariosATabla(tabla:TableLayout ) {
        for (calendario in viewModel.getCalendarios()){
            val fila = TableRow(context)
            addCell(fila, calendario.materia)
            addCell(fila,calendario.p1)
            addCell(fila,calendario.p2)
            addCell(fila,calendario.p3)
            addCell(fila,calendario.f)
            addCell(fila,calendario.e1)
            addCell(fila,calendario.e2)
            addCell(fila,calendario.esp)
            tabla.addView(fila)
        }
    }

    private fun addCell(row: TableRow, texto: String) {
        val tv = TextView(requireContext())
        tv.text = texto
        tv.setPadding(8, 8, 8, 8)

        // Centrado completo
        tv.gravity = Gravity.CENTER
        tv.textAlignment = View.TEXT_ALIGNMENT_CENTER

        tv.setTextColor(Color.BLACK)
        // Multi-línea
        tv.setSingleLine(false)
        tv.maxLines = 3

        // LayoutParams con peso
        val lp = TableRow.LayoutParams(
            0,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            1f
        )
        tv.layoutParams = lp

        row.addView(tv)
    }


}
