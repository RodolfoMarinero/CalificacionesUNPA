package mx.edu.unpa.calificacionesunpa.ui.login

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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
    private val loginRepository: LoginRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _alumnoState = MutableStateFlow<Result<Alumno>?>(null)
    val alumnoState: StateFlow<Result<Alumno>?> = _alumnoState.asStateFlow()

    private var esPrimerAcceso: Boolean = false

    fun iniciarSesion(usuario: String, contrasena: String) {
        viewModelScope.launch {
            try {
                // 1. Intentar Login
                val esValido = loginRepository.login(usuario, contrasena)

                if (esValido.isSuccess) {
                    val respuesta = esValido.getOrNull() // Extraemos el objeto para no llamarlo múltiples veces

                    val tokenRecibido = respuesta?.token
                    UsuarioService.token = tokenRecibido
                    UsuarioService.campus = respuesta?.campus

                    val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                    prefs.edit().putString("jwt_token", tokenRecibido).apply()

                    if (usuario == contrasena) {
                        esPrimerAcceso = true
                    }

                    if (usuario != "100000") { // Es un alumno
                        val result: Result<Alumno> = alumnoRepository.getAlumnoConMaterias(usuario)

                        result.onSuccess { alumno ->
                            Log.d("LoginViewModel", "Alumno descargado: ${alumno.nombre} ${alumno.apPaterno}")

                            // Actualizamos el Singleton
                            UsuarioService.alumnoActual = alumno

                            // Guardamos matrícula y periodo (Lo tuyo)
                            UsuarioService.guardarMatricula(context, alumno.matricula)
                            val periodoActual = calcularPeriodoActual(alumno)
                            guardarPeriodoActual(periodoActual)

                            Log.d("LoginViewModel", "Periodo actual calculado: $periodoActual")
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
    private fun calcularPeriodoActual(alumno: Alumno): String {
        val materias = alumno.materias ?: emptyList()

        // Extraer ciclos únicos de las materias
        val ciclos = materias
            .mapNotNull { it.ciclo }
            .distinct()
            .sorted()  // Orden cronológico ascendente

        // El último ciclo es el más reciente
        return ciclos.lastOrNull() ?: "25-26A"  // Default si no hay materias
    }


    private fun guardarPeriodoActual(periodo: String) {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("periodo_actual", periodo).apply()
        Log.d("LoginViewModel", "Periodo guardado en SharedPreferences: $periodo")
    }
    fun esPrimerAcceso(): Boolean {
        // Aseguramos que el objeto usuario tenga el flag correcto
        UsuarioService.alumnoActual?.usuario?.esPrimerAcceso = esPrimerAcceso
        return esPrimerAcceso
    }
}