package mx.edu.unpa.calificacionesunpa.ui.actualizarpassword

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mx.edu.unpa.calificacionesunpa.data.repository.UsuarioRepository
import javax.inject.Inject

@HiltViewModel
class UsuarioViewModel @Inject constructor(
    private val repository: UsuarioRepository
) : ViewModel() {

    // Estado privado mutable
    private val _estado = MutableStateFlow<Result<String>?>(null)

    // Exposición pública solo lectura
    val estado: StateFlow<Result<String>?> = _estado.asStateFlow()

    fun cambiarPassword(nombre: String, actual: String, nueva: String) {
        viewModelScope.launch {
            _estado.value = repository.cambiarPassword(nombre, actual, nueva)
        }
    }
}
