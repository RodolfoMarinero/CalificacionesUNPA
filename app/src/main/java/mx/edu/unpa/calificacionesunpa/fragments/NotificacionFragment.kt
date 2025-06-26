package mx.edu.unpa.calificacionesunpa.fragments

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

import kotlin.jvm.java

class NotificacionFragment : Fragment(R.layout.fragment_notificaciones) {
    private lateinit var recyclerView: RecyclerView
    private val notificationsList = mutableListOf<NotificationItem>()
    private lateinit var adapter: NotificationAdapter
    private lateinit var emptyView: TextView
    private lateinit var provider: NotificacionProvider

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
        provider.cargarNotificacionesDesdeFirestore { lista ->
            notificationsList.clear()
            notificationsList.addAll(lista)
            adapter.notifyDataSetChanged()
            toggleEmptyState()
        }
    }
    private fun eliminarNotificacion(notificacion: NotificationItem) {
        // Eliminar de Firestore y actualizar lista
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
