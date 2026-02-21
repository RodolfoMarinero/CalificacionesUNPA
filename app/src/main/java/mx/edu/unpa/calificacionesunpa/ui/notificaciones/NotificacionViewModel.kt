package mx.edu.unpa.calificacionesunpa.ui.notificaciones

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import mx.edu.unpa.calificacionesunpa.data.model.NotificationItem
import mx.edu.unpa.calificacionesunpa.data.persistent.UsuarioService
import mx.edu.unpa.calificacionesunpa.data.repository.NotificacionRepository
import javax.inject.Inject

@HiltViewModel
class NotificacionViewModel@Inject constructor(
    private val repository: NotificacionRepository
) : ViewModel() {

    // Estado de la lista de notificaciones
    private val _notificacionesState = MutableStateFlow<Result<List<NotificationItem>>?>(null)
    val notificacionesState: StateFlow<Result<List<NotificationItem>>?> = _notificacionesState

    // Estado del contador de notificaciones no leídas
    private val _contadorNoLeidasState = MutableStateFlow<Result<Int>?>(null)
    val contadorNoLeidasState: StateFlow<Result<Int>?> = _contadorNoLeidasState

    // Cargar todas las notificaciones de un alumno
    fun cargarNotificaciones(alumnoId: String) {
        viewModelScope.launch {
            val result = repository.obtenerNotificaciones(alumnoId)
            _notificacionesState.value = result
        }
    }

    // Enviar una nueva notificación
    fun enviarNotificacion(notificacion: NotificationItem, onComplete: (Result<Boolean>) -> Unit) {
        viewModelScope.launch {
            val result = repository.enviarNotificacion(notificacion)
            onComplete(result)
        }
    }

    // Eliminar notificaciones expiradas hasta cierta fecha
    fun eliminarNotificacionesExpiradas(hastaFecha: String, onComplete: (Result<Int>) -> Unit) {
        viewModelScope.launch {
           // val result = repository.eliminarNotificacionesExpiradas(hastaFecha)
            //onComplete(result)
        }
    }

    // Marcar una notificación como leída
    fun marcarComoLeida(notificacionId: String) {
        viewModelScope.launch {
            val result = repository.marcarNotificacionComoLeida(notificacionId)
            // opcional: recargar notificaciones o contador si quieres reflejar cambios
            if (result.isSuccess) {
                // actualizar contador
                cargarContadorNoLeidas(UsuarioService.alumnoActual?.matricula ?: "")
            }
        }
    }

    // Obtener contador de notificaciones no leídas
    fun cargarContadorNoLeidas(alumnoId: String) {
        viewModelScope.launch {
            val result = repository.obtenerContadorNoLeidas(alumnoId)
            _contadorNoLeidasState.value = result
        }
    }

    // Eliminar una notificación específica
    fun eliminarNotificacion(notificacionId: String, onComplete: (Result<Boolean>) -> Unit) {
        viewModelScope.launch {
            val result = repository.eliminarNotificacion(notificacionId)
            onComplete(result)
        }
    }
}
