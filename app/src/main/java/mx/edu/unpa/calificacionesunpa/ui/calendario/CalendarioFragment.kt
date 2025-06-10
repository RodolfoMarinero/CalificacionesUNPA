package mx.edu.unpa.calificacionesunpa.ui.calendario

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.fragment.app.Fragment
import mx.edu.unpa.calificacionesunpa.R

class CalendarioFragment : Fragment() {

    private lateinit var tablaCalendario: TableLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflar el layout del fragmento
        val view = inflater.inflate(R.layout.calendario_escolar, container, false)

        // Referencia al TableLayout
        tablaCalendario = view.findViewById(R.id.tablaCalendario)

        // Ejemplo: Agregar una fila al iniciar
        agregarFila(
            materia = "Matemáticas",
            califs = listOf("8", "9", "7", "-", "-", "-", "-")
        )

        return view
    }

    // Función para agregar una fila de calificaciones
    fun agregarFila(materia: String, califs: List<String>) {
        val fila = TableRow(context)

        // Agregar la materia como primera celda
        val tvMateria = TextView(context)
        tvMateria.text = materia
        tvMateria.setPadding(8, 4, 8, 4)
        fila.addView(tvMateria)

        // Agregar las calificaciones restantes
        for (calif in califs) {
            val tv = TextView(context)
            tv.text = calif
            tv.setPadding(8, 4, 8, 4)
            fila.addView(tv)
        }

        tablaCalendario.addView(fila)
    }
}
