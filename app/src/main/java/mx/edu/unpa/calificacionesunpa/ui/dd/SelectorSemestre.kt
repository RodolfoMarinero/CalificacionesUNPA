package mx.edu.unpa.calificacionesunpa.ui.dd

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.unpa.calificaciones.adapters.SemestreAdapter
import mx.edu.unpa.calificacionesunpa.R
import mx.edu.unpa.calificacionesunpa.data.persistent.UsuarioService

class SelectorSemestre : Fragment() {

    private var columnCount = 1
    private var semestresPromedios: Map<Int, String> = emptyMap()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { args ->
            columnCount = args.getInt(ARG_COLUMN_COUNT, 1)
            @Suppress("UNCHECKED_CAST")
            semestresPromedios = (args.getSerializable(ARG_SEMESTRES_PROMEDIOS) as? HashMap<Int, String>)
                ?: emptyMap()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.lista_semestres, container, false)

        val recyclerView = if (view is RecyclerView) {
            view
        } else {
            view.findViewById<RecyclerView>(R.id.list)
        }

        recyclerView.layoutManager = if (columnCount <= 1) {
            LinearLayoutManager(context)
        } else {
            GridLayoutManager(context, columnCount)
        }
        recyclerView.adapter = SemestreAdapter(semestresPromedios) { semestre ->
            onSemestreSeleccionado(semestre)
        }

        return view
    }

    private fun onSemestreSeleccionado(semestre: Int) {
        UsuarioService.seleccionarSemestre(semestre)
    }
    companion object {
        private const val ARG_COLUMN_COUNT = "column-count"
        private const val ARG_SEMESTRES_PROMEDIOS = "semestres-promedios"

        @JvmStatic
        fun newInstance(
            columnCount: Int,
            semestresPromedios: Map<Int, String>
        ): SelectorSemestre {
            val fragment = SelectorSemestre()
            fragment.arguments = Bundle().apply {
                putInt(ARG_COLUMN_COUNT, columnCount)
                putSerializable(ARG_SEMESTRES_PROMEDIOS, HashMap(semestresPromedios))
            }
            return fragment
        }
    }
}
