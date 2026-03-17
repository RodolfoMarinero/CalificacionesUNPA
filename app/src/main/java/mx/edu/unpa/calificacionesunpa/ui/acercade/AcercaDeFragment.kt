package mx.edu.unpa.calificacionesunpa.ui.acercade

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import mx.edu.unpa.calificacionesunpa.R

class AcercaDeFragment : Fragment() {

    // Variables para el Huevo de Pascua
    private var contadorToques = 0
    private var ultimoToque: Long = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflamos el XML que creamos en el paso anterior
        val view = inflater.inflate(R.layout.fragment_acerca_de, container, false)

        val ivLogoUnpa = view.findViewById<ImageView>(R.id.ivLogoUnpa)
        val ivFotoEquipo = view.findViewById<ImageView>(R.id.ivFotoEquipo)
        val btnPrivacidad = view.findViewById<Button>(R.id.btnPrivacidad)

        // ----------------------------------------------------
        // LÓGICA DEL HUEVO DE PASCUA (EASTER EGG)
        // ----------------------------------------------------
        ivLogoUnpa.setOnClickListener {
            val tiempoActual = SystemClock.elapsedRealtime()

            // Si pasa más de 1 segundo (1000 ms) entre toques, el combo se reinicia
            if (tiempoActual - ultimoToque > 1000) {
                contadorToques = 0
            }

            ultimoToque = tiempoActual
            contadorToques++

            // Si llegamos a los 5 toques seguidos
            if (contadorToques == 5) {
                // Magia: Ocultamos el logo y mostramos la foto del equipo
                ivLogoUnpa.visibility = View.GONE
                ivFotoEquipo.visibility = View.VISIBLE

                Toast.makeText(requireContext(), "¡Conoce al equipo de Gen 2021-2026!", Toast.LENGTH_LONG).show()
                contadorToques = 0 // Reiniciamos por si acaso
            }
        }

        // ----------------------------------------------------
        // LÓGICA DEL BOTÓN DE PRIVACIDAD
        // ----------------------------------------------------
        btnPrivacidad.setOnClickListener {
            // Aquí pegas el link real de tu Google Site
            val urlPrivacidad = "https://sites.google.com/view/unpa-grades-privacidad/inicio"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(urlPrivacidad))
            startActivity(intent)
        }


        return view
    }
}