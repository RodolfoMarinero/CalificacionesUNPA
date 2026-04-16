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

    // Variables para el Huevo de Pascua (Easter Egg)
    private var contadorToques = 0
    private var ultimoToque: Long = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflamos el XML con la nueva estructura de LinearLayout
        return inflater.inflate(R.layout.fragment_acerca_de, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Referencias a la UI
        val ivLogoUnpa = view.findViewById<ImageView>(R.id.ivLogoUnpa)
        val cardLogo = view.findViewById<View>(R.id.cardLogo)
        val cardFotoEquipo = view.findViewById<View>(R.id.cardFotoEquipo)
        val btnPrivacidad = view.findViewById<Button>(R.id.btnPrivacidad)

        // 2. Lógica del Huevo de Pascua (Easter Egg)
        ivLogoUnpa?.setOnClickListener {
            val tiempoActual = SystemClock.elapsedRealtime()

            // Si pasa más de 1 segundo entre toques, el combo se reinicia
            if (tiempoActual - ultimoToque > 1000) {
                contadorToques = 0
            }

            ultimoToque = tiempoActual
            contadorToques++

            if (contadorToques == 5) {
                // --- TRANSICIÓN ELEGANTE ---

                // Desvanecemos el logo
                cardLogo?.animate()?.alpha(0f)?.setDuration(300)?.withEndAction {
                    cardLogo.visibility = View.GONE

                    // Hacemos aparecer la foto del equipo en grande
                    cardFotoEquipo?.visibility = View.VISIBLE
                    cardFotoEquipo?.alpha = 0f
                    cardFotoEquipo?.animate()
                        ?.alpha(1f)
                        ?.setDuration(600)
                        ?.start()
                }?.start()

                Toast.makeText(requireContext(), "🚀 ¡Generación 2021-2026 presente!", Toast.LENGTH_LONG).show()
                contadorToques = 0
            }
        }

        // 3. Lógica del Botón de Privacidad
        btnPrivacidad?.setOnClickListener {
            val urlPrivacidad = "https://sites.google.com/view/unpa-grades-privacidad/inicio"
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(urlPrivacidad))
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "No se pudo abrir el navegador", Toast.LENGTH_SHORT).show()
            }
        }
    }
}