package mx.edu.unpa.calificacionesunpa.ui.notificaciones

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
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

    private val viewModel: AvisoViewModel by viewModels()

    // Usamos una lista mutable para el adaptador
    private val notificationsList = mutableListOf<Aviso>()
    private lateinit var adapter: NotificationAdapter

    // Vistas
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyView: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Inicializar Vistas
        recyclerView = view.findViewById(R.id.recyclerNotificaciones)
        emptyView = view.findViewById(R.id.tvEmptyState)
        val mainView = view.findViewById<View>(R.id.main)

        // 2. Configurar Insets (Márgenes de sistema)
        mainView?.let {
            ViewCompat.setOnApplyWindowInsetsListener(it) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        // 3. Configurar el Adaptador y RecyclerView
        adapter = NotificationAdapter(
            items = notificationsList,
            onClick = { aviso ->
                // Aquí podrías mostrar el Popup que hicimos antes
                mostrarPopupAviso(aviso)
            },
            onDelete = { aviso ->
                // Lógica para eliminar (opcional)
                eliminarAvisoLocal(aviso)
            }
        )

        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@NotificacionFragment.adapter
            itemAnimator = DefaultItemAnimator()
        }

        // 4. Iniciar la carga de datos y observar el ViewModel
        cargarNotificaciones()
    }

    private fun cargarNotificaciones() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.avisos.collect { result ->
                    result?.onSuccess { lista ->
                        notificationsList.clear()
                        notificationsList.addAll(lista)
                        adapter.notifyDataSetChanged()
                        toggleEmptyState()
                    }?.onFailure { error ->
                        Toast.makeText(requireContext(), "Error al cargar notificaciones", Toast.LENGTH_SHORT).show()
                        Log.e("Notificaciones", "Error: ${error.message}")
                        toggleEmptyState()
                    }
                }
            }
        }
        // Llamamos al ViewModel para que pida los datos a la API
        viewModel.cargarAvisos()
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

    private fun mostrarPopupAviso(aviso: Aviso) {
        // 1. Inflamos el layout personalizado
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_detalle_aviso, null)

        // Referencias a las vistas del diálogo
        val dialogHeader = dialogView.findViewById<LinearLayout>(R.id.dialogHeader)
        val dialogIcon = dialogView.findViewById<ImageView>(R.id.dialogIcon)
        val tvDialogTitle = dialogView.findViewById<TextView>(R.id.tvDialogTitle)
        val tvDialogContent = dialogView.findViewById<TextView>(R.id.tvDialogContent)
        val tvDialogDate = dialogView.findViewById<TextView>(R.id.tvDialogDate)
        val btnDialogEntendido = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnDialogEntendido)
        val btnDialogClose = dialogView.findViewById<ImageView>(R.id.btnDialogClose)

        // 2. Llenamos los datos dinámicos (Contenido)
        tvDialogContent.text = aviso.aviso
        tvDialogDate.text = aviso.fecha

        // --- 🔥 LÓGICA DE COLORES E ICONOS PARA EL POPUP 🔥 ---
        // Reutilizamos la misma lógica que usaste en el Adaptador
        val (colorHex, iconoRes, tituloText) = when (aviso.dirigir) {
            1 -> Triple("#4B0082", R.drawable.ic_assignment, "Aviso a Profesores")   // Morado
            2 -> Triple("#004080", R.drawable.ic_book, "Aviso a Alumnos")         // Azul
            3 -> {
                if (aviso.aviso.contains("suspensi", ignoreCase = true) ||
                    aviso.aviso.contains("urgente", ignoreCase = true)) {
                    Triple("#8B0000", R.drawable.ic_warning, "Aviso URGENTE")   // Rojo
                } else {
                    Triple("#B8860B", R.drawable.ic_event, "Aviso General")     // Dorado
                }
            }
            else -> Triple("#2E7D32", R.drawable.ic_notifications, "Aviso Institucional") // Verde
        }

        // 3. Aplicamos el color y el ícono al header del diálogo
        dialogHeader.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor(colorHex))
        dialogIcon.setImageResource(iconoRes)
        tvDialogTitle.text = tituloText

        // 4. Creamos y mostramos el Diálogo
        val dialog = com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .create()

        // Ocultamos el fondo del MaterialAlertDialog para que solo se vea nuestro diseño redondeado
        dialog.window?.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))

        dialog.show()

        // 5. Listeners para cerrar
        btnDialogEntendido.setOnClickListener { dialog.dismiss() }
        btnDialogClose.setOnClickListener { dialog.dismiss() }
    }

    private fun eliminarAvisoLocal(aviso: Aviso) {
        val posicion = notificationsList.indexOf(aviso)
        if (posicion != -1) {
            notificationsList.removeAt(posicion)
            adapter.notifyItemRemoved(posicion)
            toggleEmptyState()
            // Aquí podrías llamar al viewModel para borrarlo en la base de datos/API si fuera necesario
        }
    }
}