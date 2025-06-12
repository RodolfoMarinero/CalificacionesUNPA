package mx.edu.unpa.calificacionesunpa.ui.calendario

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.Fragment
import mx.edu.unpa.calificacionesunpa.R
import mx.edu.unpa.calificacionesunpa.models.Calendario
import mx.edu.unpa.calificacionesunpa.service.ExamenesService

class CalendarioFragment : Fragment() {

    private lateinit var tablaCalendario: TableLayout
    private lateinit var fechas: List<Calendario>
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflar el layout del fragmento
        val view = inflater.inflate(R.layout.calendario_examenes, container, false)

        // Referencia al TableLayout
        tablaCalendario = view.findViewById(R.id.tablaCalendario)
        fechas= ExamenesService.examenes
        agregarFilas()

        return view
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
    fun agregarFilas() {
        for (examen in fechas){
            val fila = TableRow(context)

            // Celda de materia
            addCell(fila, examen.materia)
            addCell(fila,examen.p1)
            addCell(fila,examen.p2)
            addCell(fila,examen.p3)
            addCell(fila,examen.f)
            addCell(fila,examen.e1)
            addCell(fila,examen.e2)
            addCell(fila,examen.esp)
            tablaCalendario.addView(fila)
        }
    }

}
