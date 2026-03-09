package mx.edu.unpa.calificacionesunpa.ui.tutor

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import mx.edu.unpa.calificacionesunpa.data.model.*
import mx.edu.unpa.calificacionesunpa.data.repository.TutorRepository

class TutorViewModel(private val repository: TutorRepository) : ViewModel() {

    private val _ventana = MutableLiveData<VentanaSolicitud>()
    val ventana: LiveData<VentanaSolicitud> = _ventana

    private val _tutorActual = MutableLiveData<TutorAsignado>()
    val tutorActual: LiveData<TutorAsignado> = _tutorActual

    private val _docentes = MutableLiveData<List<DocenteDisponible>>()
    val docentes: LiveData<List<DocenteDisponible>> = _docentes

    private val _solicitudResult = MutableLiveData<SolicitudResponse>()
    val solicitudResult: LiveData<SolicitudResponse> = _solicitudResult

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _historial = MutableLiveData<List<HistorialTutor>>()
    val historial: LiveData<List<HistorialTutor>> = _historial

    private val _correoActualizado = MutableLiveData<ActualizarCorreoResponse>()
    val correoActualizado: LiveData<ActualizarCorreoResponse> = _correoActualizado
    fun cargarHistorial(matricula: String) {
        viewModelScope.launch {
            _loading.value = true
            repository.obtenerHistorial(matricula)
                .onSuccess { _historial.value = it }
                .onFailure { _error.value = it.message }
            _loading.value = false
        }
    }
    fun verificarVentana(periodo: String) {
        viewModelScope.launch {
            _loading.value = true
            repository.verificarVentana(periodo)
                .onSuccess { _ventana.value = it }
                .onFailure { _error.value = it.message }
            _loading.value = false
        }
    }
    fun verificarVentanaTest(periodo: String) {
        viewModelScope.launch {
            _loading.value = true
            repository.verificarVentanaTest(periodo)
                .onSuccess { _ventana.value = it }
                .onFailure { _error.value = it.message }
            _loading.value = false
        }
    }
    fun cargarMiTutor(matricula: String, periodo: String) {
        viewModelScope.launch {
            _loading.value = true
            repository.obtenerMiTutor(matricula, periodo)
                .onSuccess { _tutorActual.value = it }
                .onFailure { _error.value = it.message }
            _loading.value = false
        }
    }

    fun cargarDocentesDisponibles(matricula: String, periodo: String) {
        viewModelScope.launch {
            _loading.value = true
            repository.obtenerDocentesDisponibles(matricula, periodo)
                .onSuccess { _docentes.value = it }
                .onFailure { _error.value = it.message }
            _loading.value = false
        }
    }

    fun solicitarTutor(request: SolicitudTutorRequest) {
        viewModelScope.launch {
            _loading.value = true
            repository.solicitarTutor(request)
                .onSuccess { _solicitudResult.value = it }
                .onFailure { _error.value = it.message }
            _loading.value = false
        }
    }
    fun actualizarCorreo(matricula: String, correo: String) {
        viewModelScope.launch {
            _loading.value = true
            val request = ActualizarCorreoRequest(matricula, correo)
            repository.actualizarCorreo(request)
                .onSuccess { _correoActualizado.value = it }
                .onFailure { _error.value = it.message }
            _loading.value = false
        }
    }
}