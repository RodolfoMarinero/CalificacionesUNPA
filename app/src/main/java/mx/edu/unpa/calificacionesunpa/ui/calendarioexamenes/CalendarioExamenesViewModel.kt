package mx.edu.unpa.calificacionesunpa.ui.calendarioexamenes

import androidx.lifecycle.ViewModel
import mx.edu.unpa.calificacionesunpa.models.Calendario
import mx.edu.unpa.calificacionesunpa.data.persistent.CalendariosExamenService


class CalendarioExamenesViewModel constructor() : ViewModel() {



    fun getCalendarios(): MutableList<Calendario> {
        return CalendariosExamenService.calendariosExamen
    }
}
