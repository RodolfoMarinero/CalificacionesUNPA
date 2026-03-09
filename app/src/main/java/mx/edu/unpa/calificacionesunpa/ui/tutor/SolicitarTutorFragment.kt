package mx.edu.unpa.calificacionesunpa.ui.tutor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject
import mx.edu.unpa.calificacionesunpa.R
import mx.edu.unpa.calificacionesunpa.databinding.FragmentSolicitarTutorBinding
import mx.edu.unpa.calificacionesunpa.data.model.DocenteDisponible
import mx.edu.unpa.calificacionesunpa.data.model.SolicitudTutorRequest
import mx.edu.unpa.calificacionesunpa.data.persistent.UsuarioService
import mx.edu.unpa.calificacionesunpa.data.repository.TutorRepository

@AndroidEntryPoint
class SolicitarTutorFragment : Fragment() {

    private var _binding: FragmentSolicitarTutorBinding? = null
    private val binding get() = _binding!!
    @Inject  // ← INYECCIÓN DE DEPENDENCIAS
    lateinit var tutorRepository: TutorRepository
    private val viewModel: TutorViewModel by viewModels {
        // Aquí inyecta tu ViewModel Factory con el Repository
        TutorViewModelFactory(tutorRepository)
    }

    private lateinit var adapter: DocentesAdapter
    private var matricula: String = ""
    private var periodo: String = ""
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSolicitarTutorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        matricula = UsuarioService.recuperarMatricula(requireContext()) ?: run {
            Snackbar.make(binding.root, "No se encontró tu matrícula", Snackbar.LENGTH_LONG).show()
            return
        }

        periodo = obtenerPeriodoActual()

        setupRecyclerView()
        setupObservers()
        verificarVentanaYCargar()
    }

    private fun obtenerPeriodoActual(): String {
        return UsuarioService.getPeriodoActual(requireContext())
    }


    private fun setupRecyclerView() {
        adapter = DocentesAdapter { docente ->
            confirmarSeleccion(docente)
        }

        binding.recyclerDocentes.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@SolicitarTutorFragment.adapter
        }
    }

    private fun setupObservers() {
        // Observar ventana de solicitud
        viewModel.ventana.observe(viewLifecycleOwner) { ventana ->
            if (ventana.abierta) {
                mostrarVentanaAbierta()
                cargarDocentes()
            } else {
                mostrarVentanaCerrada(ventana.mensaje)
            }
        }

        // Observar docentes disponibles
        viewModel.docentes.observe(viewLifecycleOwner) { docentes ->
            if (docentes.isEmpty()) {
                mostrarSinDocentes()
            } else {
                adapter.submitList(docentes)
                binding.recyclerDocentes.visibility = View.VISIBLE
                binding.cardSinDocentes.visibility = View.GONE
            }
        }

        // Observar resultado de solicitud
        viewModel.solicitudResult.observe(viewLifecycleOwner) { resultado ->
            if (resultado.exito) {
                mostrarExito(resultado.mensaje)
            } else {
                mostrarError(resultado.error ?: "Error desconocido")
            }
        }

        // Loading
        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.contentLayout.visibility = if (isLoading) View.GONE else View.VISIBLE
        }

        // Errores
        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun verificarVentanaYCargar() {
        viewModel.verificarVentana(periodo)
    }

    private fun cargarDocentes() {
        viewModel.cargarDocentesDisponibles(matricula, periodo)
    }

    private fun mostrarVentanaAbierta() {
        binding.apply {
            cardVentanaCerrada.visibility = View.GONE
            contentLayout.visibility = View.VISIBLE
        }
    }

    private fun mostrarVentanaCerrada(mensaje: String) {
        binding.apply {
            cardVentanaCerrada.visibility = View.VISIBLE
            tvMensajeVentana.text = mensaje
            contentLayout.visibility = View.GONE
        }
    }

    private fun mostrarSinDocentes() {
        binding.apply {
            recyclerDocentes.visibility = View.GONE
            cardSinDocentes.visibility = View.VISIBLE
        }
    }

    private fun confirmarSeleccion(docente: DocenteDisponible) {
        AlertDialog.Builder(requireContext())
            .setTitle("Confirmar selección")
            .setMessage("¿Deseas solicitar a ${docente.nombre} como tu tutor(a)?")
            .setPositiveButton("Confirmar") { _, _ ->
                enviarSolicitud(docente)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun enviarSolicitud(docente: DocenteDisponible) {
        val request = SolicitudTutorRequest(
            matricula = matricula,
            docenteId = docente.id,
            periodo = periodo,
            motivo = null
        )
        viewModel.solicitarTutor(request)
    }

    private fun mostrarExito(mensaje: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("✅ Solicitud exitosa")
            .setMessage(mensaje)
            .setPositiveButton("Aceptar") { _, _ ->
                // Navegar de vuelta
                findNavController().navigateUp()
            }
            .setCancelable(false)
            .show()
    }

    private fun mostrarError(mensaje: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("❌ Error")
            .setMessage(mensaje)
            .setPositiveButton("Aceptar", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}