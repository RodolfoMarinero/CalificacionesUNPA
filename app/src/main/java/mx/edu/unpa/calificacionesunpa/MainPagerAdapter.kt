package mx.edu.unpa.calificacionesunpa // O ajusta a tu paquete correcto

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import mx.edu.unpa.calificacionesunpa.ui.acercade.AcercaDeFragment
import mx.edu.unpa.calificacionesunpa.ui.calificacionesanteriores.FragmentCalificacionesAnteriores // Importa tus fragmentos
import mx.edu.unpa.calificacionesunpa.ui.calendarioexamenes.CalendarioFragment
import mx.edu.unpa.calificacionesunpa.ui.notificaciones.NotificacionFragment
import mx.edu.unpa.calificacionesunpa.ui.perfil.FragmentPerfilN
// Importa aquí tus otros fragmentos cuando los crees (Calendario, Avisos, Perfil)

class MainPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    // Tenemos 4 pestañas en tu diseño
    override fun getItemCount(): Int = 5

    // Aquí le decimos qué fragmento abrir según la pestaña seleccionada
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> FragmentCalificacionesAnteriores() // Pestaña 1: Calificaciones
            1 -> CalendarioFragment()   // Pestaña 2: Calendario (Descomenta cuando exista)
            2 -> NotificacionFragment()       // Pestaña 3: Avisos (Descomenta cuando exista)
            3 -> FragmentPerfilN()      // Pestaña 4: Perfil (Descomenta cuando exista)
            4 -> AcercaDeFragment()

            // MIENTRAS TANTO: Usaremos CalificacionesFragment para todas para que no crashee
            else -> FragmentCalificacionesAnteriores()
        }
    }
}