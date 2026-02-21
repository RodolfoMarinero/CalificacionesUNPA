package mx.edu.unpa.calificacionesunpa.ui.notificaciones

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import mx.edu.unpa.calificacionesunpa.data.repository.AvisoRepository
import mx.edu.unpa.calificacionesunpa.models.Aviso
import javax.inject.Inject

@HiltViewModel
class AvisoViewModel @Inject constructor(
    private val repository: AvisoRepository
) : ViewModel() {

    private val _avisos = MutableStateFlow<Result<List<Aviso>>?>(null)
    val avisos: StateFlow<Result<List<Aviso>>?> = _avisos

    fun cargarAvisos() {
        viewModelScope.launch {
            _avisos.value = repository.getAvisos()
        }
    }

    fun crearAviso(aviso: Aviso) {
        viewModelScope.launch {
            repository.addAviso(aviso)
            cargarAvisos() // refresca lista
        }
    }

    fun eliminarAviso(id: Int) {
        viewModelScope.launch {
            repository.deleteAviso(id)
            cargarAvisos()
        }
    }
}
