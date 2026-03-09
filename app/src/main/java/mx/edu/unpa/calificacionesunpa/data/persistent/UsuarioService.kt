package mx.edu.unpa.calificacionesunpa.data.persistent

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import mx.edu.unpa.calificacionesunpa.models.Alumno

object UsuarioService {
    var alumnoActual: Alumno? = null
    var token: String? = null
    private val _semestreSeleccionado = MutableLiveData<Int>()
    val semestreSeleccionado: LiveData<Int> = _semestreSeleccionado
    fun seleccionarSemestre(semestre: Int) {
        _semestreSeleccionado.value = semestre;
    }
    fun getMatricula(context: Context? = null): String? {
        return alumnoActual?.matricula
    }

    fun guardarMatricula(context: Context, matricula: String) {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("matricula", matricula).apply()

        // También actualizar alumnoActual si existe
        alumnoActual?.matricula = matricula
    }

    fun recuperarMatricula(context: Context): String? {
        // Primero intenta del objeto en memoria
        alumnoActual?.matricula?.let { return it }

        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return prefs.getString("matricula", null)
    }
    fun getPeriodoActual(context: Context): String {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return prefs.getString("periodo_actual", "2025-2026-A") ?: "2025-2026-A"
    }

    fun guardarPeriodoActual(context: Context, periodo: String) {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("periodo_actual", periodo).apply()
    }
    fun guardarCorreo(context: Context, correo: String) {

        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("correo_actual", correo).apply()

        alumnoActual?.correo = correo
    }

    fun recuperarCorreo(context: Context): String? {

        alumnoActual?.correo?.let { return it }

        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return prefs.getString("correo_actual", null)
    }
}
