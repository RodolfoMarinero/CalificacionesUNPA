package mx.edu.unpa.calificacionesunpa.ui.tutoria

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.*
import com.google.android.material.tabs.TabLayout
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import mx.edu.unpa.calificacionesunpa.R
import mx.edu.unpa.calificacionesunpa.data.api.TutorAPI
import mx.edu.unpa.calificacionesunpa.data.model.*
import javax.inject.Inject

@AndroidEntryPoint
class MiTutoriaFragment : Fragment() {

    private val vm: MiTutoriaViewModel by viewModels()

    private lateinit var tabs: TabLayout

    // ── Mi Tutor ──
    private lateinit var secTutor: View
    private lateinit var emailAlert: View
    private lateinit var btnRegistrarCorreo: Button
    private lateinit var tvNombreTutor: TextView
    private lateinit var tvCarreraTutor: TextView
    private lateinit var tvCorreoTutor: TextView
    private lateinit var tvTipoAsignacion: TextView
    private lateinit var tvEstadoTutor: TextView        // ← FIX 1: faltaba este binding
    private lateinit var tvCorreoAlumno: TextView
    private lateinit var btnActualizarCorreo: TextView
    private lateinit var noTutorCard: View
    private lateinit var tutorCard: View
    private lateinit var btnIrCambiar: Button

    // ── Cambiar tutor ──
    private lateinit var secCambiar: View
    private lateinit var motivoCard: View
    private lateinit var chipGroupMotivo: ChipGroup
    private lateinit var etMotivoDetalle: TextInputEditText
    private lateinit var spinnerCarrera: Spinner
    private lateinit var listaTutores: LinearLayout
    private lateinit var btnConfirmar: Button

    // ── Historial ──
    private lateinit var secHistorial: View
    private lateinit var listaHistorial: LinearLayout

    // ── Correo inline ──
    private lateinit var secCorreo: View
    private lateinit var etCorreo: TextInputEditText
    private lateinit var btnGuardarCorreo: Button

    override fun onCreateView(inflater: LayoutInflater, c: ViewGroup?, s: Bundle?): View =
        inflater.inflate(R.layout.fragment_mi_tutoria, c, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        observeViewModel()
        setupTabs()
        setupCambiarTutor()
        setupCorreo()
        vm.cargarDatos()
    }

    private fun bindViews(v: View) {
        tabs               = v.findViewById(R.id.tabs)
        secTutor           = v.findViewById(R.id.sec_tutor)
        emailAlert         = v.findViewById(R.id.email_alert)
        btnRegistrarCorreo = v.findViewById(R.id.btn_registrar_correo)
        tvNombreTutor      = v.findViewById(R.id.tv_nombre_tutor)
        tvCarreraTutor     = v.findViewById(R.id.tv_carrera_tutor)
        tvCorreoTutor      = v.findViewById(R.id.tv_correo_tutor)
        tvTipoAsignacion   = v.findViewById(R.id.tv_tipo_asignacion)
        tvEstadoTutor      = v.findViewById(R.id.tv_estado_tutor)   // ← FIX 1
        tvCorreoAlumno     = v.findViewById(R.id.tv_correo_alumno)
        btnActualizarCorreo= v.findViewById(R.id.btn_actualizar_correo)
        noTutorCard        = v.findViewById(R.id.no_tutor_card)
        tutorCard          = v.findViewById(R.id.tutor_card)
        btnIrCambiar       = v.findViewById(R.id.btn_ir_cambiar)
        secCambiar         = v.findViewById(R.id.sec_cambiar)
        motivoCard         = v.findViewById(R.id.motivo_card)
        chipGroupMotivo    = v.findViewById(R.id.chip_group_motivo)
        etMotivoDetalle    = v.findViewById(R.id.et_motivo_detalle)
        spinnerCarrera     = v.findViewById(R.id.spinner_carrera)
        listaTutores       = v.findViewById(R.id.lista_tutores)
        btnConfirmar       = v.findViewById(R.id.btn_confirmar_cambio)
        secHistorial       = v.findViewById(R.id.sec_historial)
        listaHistorial     = v.findViewById(R.id.lista_historial)
        secCorreo          = v.findViewById(R.id.sec_correo)
        etCorreo           = v.findViewById(R.id.et_correo)
        btnGuardarCorreo   = v.findViewById(R.id.btn_guardar_correo)
    }

    private fun observeViewModel() {
        vm.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is TutoriaState.Loading -> { }
                is TutoriaState.Loaded  -> renderLoaded(state)
                is TutoriaState.Error   -> snack(state.msg)
            }
        }
        vm.tutoresDisponibles.observe(viewLifecycleOwner) { renderListaTutores(it) }
        vm.accion.observe(viewLifecycleOwner) { evento ->
            evento.getContentIfNotHandled()?.let { msg ->
                snack(msg)
                if (msg.startsWith("¡Excelente")) {
                    tabs.getTabAt(0)?.select()
                    vm.cargarDatos()
                }
            }
        }
    }

    private fun renderLoaded(state: TutoriaState.Loaded) {
        // ── Correo ──
        val tieneCorreo = !state.correoAlumno.isNullOrBlank()
        emailAlert.isVisible = !tieneCorreo
        tvCorreoAlumno.text  = state.correoAlumno ?: "Sin correo registrado"
        etCorreo.setText(state.correoAlumno ?: "")

        // ── Tutor ──
        if (state.tutor != null) {
            tutorCard.isVisible   = true
            noTutorCard.isVisible = false
            tvNombreTutor.text    = state.tutor.nombreTutor ?: ""
            tvCarreraTutor.text   = state.tutor.carreraTutor ?: ""
            tvCorreoTutor.text    = state.tutor.correoTutor ?: "Sin correo registrado"
            tvTipoAsignacion.text = formatTipo(state.tutor.tipoAsignacion)
            // FIX 1: poblar el campo Estado que antes quedaba vacío
            tvEstadoTutor.text    = formatEstado(state.tutor.estadoTutor)
            tvEstadoTutor.setTextColor(colorEstado(state.tutor.estadoTutor))
            motivoCard.isVisible  = state.tuvieraTutorAntes
        } else {
            tutorCard.isVisible   = false
            noTutorCard.isVisible = true
            motivoCard.isVisible  = false
        }

        // FIX 2: pestaña "Cambiar tutor" — visible solo si ventana está abierta
        actualizarTabCambiar(state.ventanaAbierta)

        renderHistorial(state.historial)

        if (state.carreras.isNotEmpty()) {
            val nombres = state.carreras.map {
                if (it.esPropia) "⭐ ${it.nombre}" else it.nombre
            }
            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                nombres
            )
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerCarrera.adapter = adapter
            val idx = state.carreras.indexOfFirst { it.esPropia }
            if (idx >= 0) spinnerCarrera.setSelection(idx)
        }
    }

    // FIX 2: agregar/quitar tab "Cambiar tutor" según ventana
    private fun actualizarTabCambiar(ventanaAbierta: Boolean) {
        val tabCambiar = tabs.getTabAt(1) // posición 1 = "Cambiar tutor"
        if (tabCambiar != null) {
            tabCambiar.view.isVisible = ventanaAbierta
        }
        // Si la ventana se cierra y estás en esa pestaña, ir a "Mi tutor"
        if (!ventanaAbierta && tabs.selectedTabPosition == 1) {
            tabs.getTabAt(0)?.select()
        }
    }

    private fun renderListaTutores(tutores: List<DocenteDisponible>) {
        listaTutores.removeAllViews()
        tutores.forEach { tutor ->
            val item = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_tutor_disponible, listaTutores, false)

            item.findViewById<TextView>(R.id.tv_nombre).text = tutor.nombre

            val slots = tutor.tutoradosActuales
            val max   = tutor.maxTutorados.toLong()
            val bar   = item.findViewById<ProgressBar>(R.id.bar_slots)
            bar.max      = max.toInt()
            bar.progress = slots.toInt()
            item.findViewById<TextView>(R.id.tv_slots).text =
                if (slots >= max) "$slots / $max · Sin espacio" else "$slots / $max tutorados"

            val lleno = slots >= max
            item.isEnabled = !lleno
            item.alpha     = if (lleno) 0.5f else 1f

            item.setOnClickListener {
                if (!lleno) {
                    for (i in 0 until listaTutores.childCount) {
                        listaTutores.getChildAt(i).isSelected = false
                    }
                    item.isSelected = true
                    vm.seleccionarDocente(tutor.id)
                }
            }
            listaTutores.addView(item)
        }
    }

    private fun renderHistorial(historial: List<HistorialTutor>) {
        listaHistorial.removeAllViews()
        historial.forEachIndexed { index, h ->
            val item = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_historial_tutor, listaHistorial, false)
            item.findViewById<TextView>(R.id.tv_periodo).text     = h.periodo
            item.findViewById<TextView>(R.id.tv_nombre_hist).text = h.nombreTutor
            item.findViewById<TextView>(R.id.tv_tipo_hist).text   = formatTipo(h.tipoAsignacion)
            item.findViewById<View>(R.id.timeline_line).isVisible = index < historial.lastIndex
            listaHistorial.addView(item)
        }
    }

    private fun setupTabs() {
        tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                secTutor.isVisible     = tab.position == 0
                secCambiar.isVisible   = tab.position == 1
                secHistorial.isVisible = tab.position == 2
                if (tab.position == 1) {
                    vm.cargarDocentesCarrera(spinnerCarrera.selectedItemPosition)
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        btnIrCambiar.setOnClickListener       { tabs.getTabAt(1)?.select() }
        btnRegistrarCorreo.setOnClickListener  { secCorreo.isVisible = true }
        btnActualizarCorreo.setOnClickListener { secCorreo.isVisible = !secCorreo.isVisible }

        spinnerCarrera.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                vm.cargarDocentesCarrera(pos)
            }
            override fun onNothingSelected(p: AdapterView<*>?) {}
        }
    }

    private fun setupCambiarTutor() {
        btnConfirmar.setOnClickListener {
            var motivo: String? = null
            if (motivoCard.isVisible) {
                val chipId = chipGroupMotivo.checkedChipId
                if (chipId == View.NO_ID) {
                    snack("Selecciona el motivo del cambio")
                    return@setOnClickListener
                }
                val chipText = chipGroupMotivo.findViewById<Chip>(chipId).text.toString()
                val detalle  = etMotivoDetalle.text?.toString()?.trim() ?: ""
                motivo = if (detalle.isBlank()) chipText else "$chipText: $detalle"
            }
            if (vm.docenteSeleccionadoId.value == null) {
                snack("Selecciona un tutor")
                return@setOnClickListener
            }
            vm.confirmarCambio(motivo)
        }
    }

    private fun setupCorreo() {
        btnGuardarCorreo.setOnClickListener {
            val correo = etCorreo.text?.toString()?.trim() ?: ""
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
                snack("Ingresa un correo válido")
                return@setOnClickListener
            }
            vm.actualizarCorreo(correo)
            secCorreo.isVisible = false
        }
    }

    // ── Helpers de formato ────────────────────────────────
    private fun formatTipo(tipo: String?) = when (tipo) {
        "ELECCION_ALUMNO"   -> "Elegido por ti"
        "CONTINUIDAD"       -> "Continuidad"
        "ALEATORIO_CARRERA" -> "Asignación automática"
        "ALEATORIO_AFIN"    -> "Carrera afín"
        "ALEATORIO_GLOBAL"  -> "Asignación general"
        else                -> tipo ?: ""
    }

    // FIX 1: texto legible para el estado del tutor
    private fun formatEstado(estado: String?) = when (estado?.uppercase()) {
        "ACTIVO"   -> "Activo"
        "SABATICO",
        "SABÁTICO" -> "En año sabático"
        "INHABIL",
        "INHÁBIL"  -> "Inhábil"
        "BAJA"     -> "Baja"
        null, ""   -> "Activo"          // la mayoría son activos, default seguro
        else       -> estado
    }

    // FIX 1: color acorde al estado
    private fun colorEstado(estado: String?): Int {
        val color = when (estado?.uppercase()) {
            "ACTIVO", null, "" -> "#065F46"  // verde
            "SABATICO",
            "SABÁTICO"         -> "#92400E"  // ámbar
            "INHABIL",
            "INHÁBIL"          -> "#9D174D"  // rosa
            "BAJA"             -> "#64748B"  // gris
            else               -> "#065F46"
        }
        return android.graphics.Color.parseColor(color)
    }

    private fun snack(msg: String) =
        Snackbar.make(requireView(), msg, Snackbar.LENGTH_LONG).show()
}

// ══════════════════════════════════════════════════════
//  VIEW MODEL
// ══════════════════════════════════════════════════════
@HiltViewModel
class MiTutoriaViewModel @Inject constructor(
    private val api: TutorAPI,
    private val prefs: android.content.SharedPreferences
) : ViewModel() {

    private val matricula get() = prefs.getString("matricula", "") ?: ""
    private val periodo   get() = prefs.getString("periodo_actual", "2025-2026-A") ?: "2025-2026-A"
    private val correo    get() = prefs.getString("correo_actual", null)
    private val carrera   get() = prefs.getString("carrera_alumno", "") ?: ""

    val state                 = MutableLiveData<TutoriaState>()
    val tutoresDisponibles    = MutableLiveData<List<DocenteDisponible>>()
    val docenteSeleccionadoId = MutableLiveData<Long?>(null)
    val accion                = MutableLiveData<Event<String>>()

    private var carrerasCache: List<CarreraItem> = emptyList()

    fun cargarDatos() {
        viewModelScope.launch {
            state.value = TutoriaState.Loading
            try {
                // FIX 2: consultar el estado de la ventana junto con los demás datos
                val ventanaResp   = api.verificarVentana(periodo)
                val tutorResp     = api.obtenerMiTutor(matricula, periodo)
                val historialResp = api.obtenerHistorial(matricula)
                val docentesResp  = api.obtenerDocentesDisponibles(matricula, periodo)

                val ventanaAbierta = ventanaResp.isSuccessful &&
                        ventanaResp.body()?.abierta == true

                val tutor     = if (tutorResp.isSuccessful)     tutorResp.body()           else null
                val historial = if (historialResp.isSuccessful) historialResp.body() ?: emptyList() else emptyList()
                val docentes  = if (docentesResp.isSuccessful)  docentesResp.body() ?: emptyList() else emptyList()

                val tuvieraTutorAntes = historial.any { it.periodo != periodo }

                val carreraAlumno = carrera
                carrerasCache = docentes
                    .map { it.carrera }
                    .distinct()
                    .map { c -> CarreraItem(c, c == carreraAlumno) }
                    .sortedByDescending { it.esPropia }

                state.value = TutoriaState.Loaded(
                    tutor             = tutor?.takeIf { it.tieneTutor },
                    historial         = historial,
                    tuvieraTutorAntes = tuvieraTutorAntes,
                    correoAlumno      = correo,
                    carreras          = carrerasCache,
                    ventanaAbierta    = ventanaAbierta   // ← FIX 2
                )
            } catch (e: Exception) {
                state.value = TutoriaState.Error("Error al cargar datos: ${e.localizedMessage}")
            }
        }
    }

    fun cargarDocentesCarrera(posicion: Int) {
        viewModelScope.launch {
            try {
                val carreraFiltro = carrerasCache.getOrNull(posicion)?.nombre
                val resp  = api.obtenerDocentesDisponibles(matricula, periodo)
                val todos = if (resp.isSuccessful) resp.body() ?: emptyList() else emptyList()
                tutoresDisponibles.value = if (carreraFiltro != null)
                    todos.filter { it.carrera == carreraFiltro }
                else todos
            } catch (_: Exception) {}
        }
    }

    fun seleccionarDocente(id: Long) { docenteSeleccionadoId.value = id }

    fun confirmarCambio(motivo: String?) {
        val docenteId = docenteSeleccionadoId.value ?: return
        viewModelScope.launch {
            try {
                val resp = api.solicitarTutor(
                    SolicitudTutorRequest(matricula, docenteId, periodo, motivo)
                )
                val body = resp.body()
                when {
                    body?.exito == true          -> accion.value = Event("¡Excelente! ${body.mensaje}")
                    body?.requiereMotivo == true -> accion.value = Event("Debes indicar el motivo del cambio")
                    else                         -> accion.value = Event(body?.error ?: "Error al cambiar tutor")
                }
            } catch (e: Exception) {
                accion.value = Event("Error: ${e.localizedMessage}")
            }
        }
    }

    fun actualizarCorreo(correoNuevo: String) {
        viewModelScope.launch {
            try {
                val resp = api.actualizarCorreo(ActualizarCorreoRequest(matricula, correoNuevo))
                if (resp.isSuccessful && resp.body()?.exito == true) {
                    prefs.edit().putString("correo_actual", correoNuevo).apply()
                    accion.value = Event("Correo actualizado correctamente")
                    cargarDatos()
                } else {
                    accion.value = Event("Error al actualizar correo")
                }
            } catch (e: Exception) {
                accion.value = Event("Error: ${e.localizedMessage}")
            }
        }
    }
}

// ══════════════════════════════════════════════════════
//  ESTADOS
// ══════════════════════════════════════════════════════
sealed class TutoriaState {
    object Loading : TutoriaState()
    data class Loaded(
        val tutor:             TutorAsignado?,
        val historial:         List<HistorialTutor>,
        val tuvieraTutorAntes: Boolean,
        val correoAlumno:      String?,
        val carreras:          List<CarreraItem>,
        val ventanaAbierta:    Boolean              // ← FIX 2: nuevo campo
    ) : TutoriaState()
    data class Error(val msg: String) : TutoriaState()
}

data class CarreraItem(val nombre: String, val esPropia: Boolean)

class Event<T>(private val content: T) {
    private var handled = false
    fun getContentIfNotHandled(): T? =
        if (handled) null else { handled = true; content }
}