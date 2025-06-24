package mx.edu.unpa.calificacionesunpa.service

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import mx.edu.unpa.calificacionesunpa.models.Alumno

object UsuarioService {
    var alumnoActual: Alumno? = null
    private val _semestreSeleccionado = MutableLiveData<Int>()
    val semestreSeleccionado: LiveData<Int> = _semestreSeleccionado
    fun seleccionarSemestre(semestre: Int) {
        _semestreSeleccionado.value = semestre;
    }

}
