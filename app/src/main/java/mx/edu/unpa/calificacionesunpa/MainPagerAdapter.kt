package mx.edu.unpa.calificacionesunpa // O ajusta a tu paquete correcto

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import mx.edu.unpa.calificacionesunpa.ui.acercade.AcercaDeFragment
import mx.edu.unpa.calificacionesunpa.ui.calificacionesanteriores.FragmentCalificacionesAnteriores
import mx.edu.unpa.calificacionesunpa.ui.calendarioexamenes.CalendarioFragment
import mx.edu.unpa.calificacionesunpa.ui.notificaciones.NotificacionFragment
import mx.edu.unpa.calificacionesunpa.ui.perfil.FragmentPerfilN
import mx.edu.unpa.calificacionesunpa.ui.tutoria.MiTutoriaFragment

class MainPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 6

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> FragmentCalificacionesAnteriores()
            1 -> CalendarioFragment()
            2 -> NotificacionFragment()
            3 -> FragmentPerfilN()
            4 -> AcercaDeFragment()
            5 -> MiTutoriaFragment()
            else -> FragmentCalificacionesAnteriores()
        }
    }
}