package mx.edu.unpa.calificacionesunpa.ui.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mx.edu.unpa.calificacionesunpa.data.persistent.UsuarioService
import mx.edu.unpa.calificacionesunpa.data.repository.AlumnoRepository
import mx.edu.unpa.calificacionesunpa.data.repository.LoginRepository
import mx.edu.unpa.calificacionesunpa.models.Alumno
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val alumnoRepository: AlumnoRepository,
    private val loginRepository: LoginRepository
) : ViewModel() {

    private val _alumnoState = MutableStateFlow<Result<Alumno>?>(null)
    val alumnoState: StateFlow<Result<Alumno>?> = _alumnoState.asStateFlow()

    private var esPrimerAcceso: Boolean = false

    fun iniciarSesion(usuario: String, contrasena: String) {
        viewModelScope.launch {
            try {
                val esValido = loginRepository.login(usuario, contrasena)

                if (esValido.isSuccess) {
                    val respuesta = esValido.getOrNull() // Extraemos el objeto para no llamarlo múltiples veces

                    // Guardamos ambos datos en el Singleton
                    UsuarioService.token = respuesta?.token
                    UsuarioService.campus = respuesta?.campus // <-- ¡LA MAGIA OCURRE AQUÍ!

                    if (usuario == contrasena) {
                        esPrimerAcceso = true
                    }

                    if (usuario != "100000") { // Es un alumno
                        // 2. Descargar información completa
                        val result: Result<Alumno> = alumnoRepository.getAlumnoConMaterias(usuario)

                        // --- ESTA ES LA MEJORA ---
                        // Actualizamos el UsuarioService AQUÍ MISMO apenas llegan los datos
                        result.onSuccess { alumno ->
                            Log.d("LoginViewModel", "Alumno descargado: ${alumno.nombre} ${alumno.apPaterno}")

                            // Actualizamos el Singleton
                            UsuarioService.alumnoActual = alumno
                        }

                        _alumnoState.value = result
                    }
                } else {
                    _alumnoState.value = Result.failure(Exception("Usuario o contraseña incorrectos"))
                }
            } catch (e: Exception) {
                _alumnoState.value = Result.failure(e)
            }
        }
    }

    fun esPrimerAcceso(): Boolean {
        // Aseguramos que el objeto usuario tenga el flag correcto
        UsuarioService.alumnoActual?.usuario?.esPrimerAcceso = esPrimerAcceso
        return esPrimerAcceso
    }
}