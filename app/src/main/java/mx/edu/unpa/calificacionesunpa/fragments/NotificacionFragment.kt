package mx.edu.unpa.calificacionesunpa.fragments
/*
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import mx.edu.unpa.calificacionesunpa.R

import kotlin.jvm.java

class NotificacionFragment : Fragment(R.layout.activity_notificaciones) {
    private lateinit var recyclerView: RecyclerView
    private val notificationsList = mutableListOf<NotificationItem>()
    private lateinit var adapter: NotificationAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Ajuste de inset de sistema
        ViewCompat.setOnApplyWindowInsetsListener(
            view.findViewById(R.id.main)
        ) { v, insets ->
            val sys = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(sys.left, sys.top, sys.right, sys.bottom)
            insets
        }

        recyclerView = view.findViewById(R.id.recyclerNotificaciones)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = NotificationAdapter(notificationsList)
        recyclerView.adapter = adapter

        cargarNotificacionesDesdeFirestore()
    }

    private fun cargarNotificacionesDesdeFirestore() {
        val db = FirebaseFirestore.getInstance()
        db.collection("notificaciones")
            .orderBy("timestamp")
            .get()
            .addOnSuccessListener { documents ->
                notificationsList.clear()
                for (doc in documents) {
                    val notificacion = doc.toObject(NotificationItem::class.java)
                    notificationsList.add(notificacion)
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error al cargar notificaciones", e)
            }
    }
}
*/