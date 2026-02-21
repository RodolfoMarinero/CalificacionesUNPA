package mx.edu.unpa.calificacionesunpa.ui.calendarioescolar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import mx.edu.unpa.calificacionesunpa.data.repository.CalendarioEscolarRepository
import javax.inject.Inject

// ui/storage/StorageViewModel.kt
@HiltViewModel
class CalendarioEscolarViewModel @Inject constructor(
    private val repository: CalendarioEscolarRepository
) : ViewModel() {

    private val _file = MutableStateFlow<ByteArray?>(null)
    val file: StateFlow<ByteArray?> = _file

    fun getFile(id: String) {
        viewModelScope.launch {
            val bytes = repository.getFile(id)
            _file.value = bytes
        }
    }
}

