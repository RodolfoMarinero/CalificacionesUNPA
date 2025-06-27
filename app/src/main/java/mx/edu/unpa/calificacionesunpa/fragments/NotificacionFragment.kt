package mx.edu.unpa.calificacionesunpa.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import mx.edu.unpa.calificacionesunpa.R
import mx.edu.unpa.calificacionesunpa.adapters.NotificationAdapter
import mx.edu.unpa.calificacionesunpa.adapters.NotificationItem
import mx.edu.unpa.calificacionesunpa.providers.NotificacionProvider
import mx.edu.unpa.calificacionesunpa.service.UsuarioService

import kotlin.jvm.java

class NotificacionFragment : Fragment(R.layout.fragment_notificaciones) {
    private lateinit var recyclerView: RecyclerView
    private val notificationsList = mutableListOf<NotificationItem>()
    private lateinit var adapter: NotificationAdapter
    private lateinit var emptyView: TextView
    private lateinit var provider: NotificacionProvider
    private val alumnoActual: String by lazy {
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
        adapter = NotificationAdapter(notificationsList,
            onClick = { notificacion -> /* manejar clic */ },
            onDelete = { notificacion -> eliminarNotificacion(notificacion) }
        )
        recyclerView.adapter = adapter
        recyclerView.itemAnimator = DefaultItemAnimator()

        provider = NotificacionProvider()
        cargarNotificaciones()

    }
    private fun cargarNotificaciones() {
        provider.cargarNotificacionesDesdeFirestore { lista ->
            val eliminadas = obtenerIdsEliminadas(alumnoActual)
            val visibles = lista.filter { it.id !in eliminadas }

            notificationsList.clear()
            notificationsList.addAll(visibles)
            adapter.notifyDataSetChanged()
            toggleEmptyState()
        }
    }
    private fun eliminarNotificacion(notificacion: NotificationItem) {
        guardarNotificacionEliminada(alumnoActual, notificacion.id)
        notificationsList.remove(notificacion)
        adapter.notifyDataSetChanged()
        toggleEmptyState()
    }
    private fun getSharedPrefs(): SharedPreferences {
        return requireContext().getSharedPreferences("notificaciones_prefs", Context.MODE_PRIVATE)
    }

    private fun guardarNotificacionEliminada(usuarioId: String, idNotificacion: String) {
        val prefs = getSharedPrefs()
        val key = "eliminadas_$usuarioId"
        val eliminadas = prefs.getStringSet(key, mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        eliminadas.add(idNotificacion)
        prefs.edit().putStringSet(key, eliminadas).apply()
    }

    private fun obtenerIdsEliminadas(usuarioId: String): Set<String> {
        val prefs = getSharedPrefs()
        val key = "eliminadas_$usuarioId"
        return prefs.getStringSet(key, emptySet()) ?: emptySet()
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
