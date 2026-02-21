package mx.edu.unpa.calificacionesunpa.ui.notificaciones

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import mx.edu.unpa.calificacionesunpa.R
import mx.edu.unpa.calificacionesunpa.adapters.NotificationAdapter
import mx.edu.unpa.calificacionesunpa.data.persistent.UsuarioService
import mx.edu.unpa.calificacionesunpa.models.Aviso

@AndroidEntryPoint
class NotificacionFragment : Fragment(R.layout.fragment_notificaciones) {
    private lateinit var recyclerView: RecyclerView

    private val viewModel: AvisoViewModel by viewModels()

    private val notificationsList = mutableListOf<Aviso>()
    private lateinit var adapter: NotificationAdapter
    private lateinit var emptyView: TextView

    private val matricula: String by lazy {
        UsuarioService.alumnoActual?.matricula ?: "desconocido"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        emptyView = view.findViewById(R.id.tvEmptyState)

        // Ajuste de inset de sistema
        ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.main)) { v, insets ->
            val sys = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(sys.left, sys.top, sys.right, sys.bottom)
            insets
        }

        recyclerView = view.findViewById(R.id.recyclerNotificaciones)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = NotificationAdapter(
            notificationsList,
            onClick = { notificacion -> /* manejar clic */ },
            onDelete = { notificacion ->  }
        )
        recyclerView.adapter = adapter
        recyclerView.itemAnimator = DefaultItemAnimator()

        cargarNotificaciones()
    }

    private fun cargarNotificaciones() {
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.avisos.collect { result ->
                    result?.onSuccess { lista ->
                        notificationsList.clear()
                        notificationsList.addAll(lista)
                        adapter.notifyDataSetChanged()
                        toggleEmptyState()
                    }?.onFailure {
                        Toast.makeText(requireContext(), "Error: al cargar notificaciones", Toast.LENGTH_SHORT).show()
                        Log.e("Notificaciones", "Error al cargar notificaciones: ")
                    }
                }
            }
        }
        viewModel.cargarAvisos()
    }


    private fun getSharedPrefs(): SharedPreferences {
        return requireContext().getSharedPreferences("notificaciones_prefs", Context.MODE_PRIVATE)
    }



    private fun toggleEmptyState() {
        if (notificationsList.isEmpty()) {
            emptyView.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            emptyView.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }


}