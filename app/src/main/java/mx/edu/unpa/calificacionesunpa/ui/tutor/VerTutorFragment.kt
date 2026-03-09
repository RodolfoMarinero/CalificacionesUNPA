package mx.edu.unpa.calificacionesunpa.ui.tutor

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject
import mx.edu.unpa.calificacionesunpa.data.model.TutorAsignado
import mx.edu.unpa.calificacionesunpa.databinding.FragmentVerTutorBinding
import mx.edu.unpa.calificacionesunpa.data.persistent.UsuarioService
import mx.edu.unpa.calificacionesunpa.data.repository.TutorRepository

@AndroidEntryPoint
class VerTutorFragment : Fragment() {

    private var _binding: FragmentVerTutorBinding? = null
    private val binding get() = _binding!!
    @Inject  // ← INYECCIÓN DE DEPENDENCIAS
    lateinit var tutorRepository: TutorRepository
    private val viewModel: TutorViewModel by viewModels {
        // Aquí inyecta tu ViewModel Factory con el Repository
        TutorViewModelFactory(tutorRepository)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVerTutorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObservers()
        cargarDatos()
    }

    private fun setupObservers() {
        viewModel.tutorActual.observe(viewLifecycleOwner) { tutor ->
            if (tutor.tieneTutor) {
                mostrarTutor(tutor)
            } else {
                mostrarSinTutor(tutor.mensaje ?: "Aún no tienes tutor asignado")
            }
        }

        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.contentLayout.visibility = if (isLoading) View.GONE else View.VISIBLE
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun cargarDatos() {
        val matricula = UsuarioService.recuperarMatricula(requireContext()) ?: run {
            Snackbar.make(binding.root, "No se encontró tu matrícula", Snackbar.LENGTH_LONG).show()
            return
        }

        val periodo = obtenerPeriodoActual()
        viewModel.cargarMiTutor(matricula, periodo)
    }
    private fun obtenerPeriodoActual(): String {
        return UsuarioService.getPeriodoActual(requireContext())
    }
    private fun mostrarTutor(tutor: TutorAsignado) {
        binding.apply {
            cardSinTutor.visibility = View.GONE
            cardConTutor.visibility = View.VISIBLE

            tvNombreTutor.text = tutor.nombreTutor
            tvCorreoTutor.text = tutor.correoTutor
            tvCarreraTutor.text = tutor.carreraTutor

            // Botón para enviar email
            btnContactarTutor.setOnClickListener {
                enviarEmail(tutor.correoTutor ?: "")
            }
        }
    }

    private fun mostrarSinTutor(mensaje: String) {
        binding.apply {
            cardConTutor.visibility = View.GONE
            cardSinTutor.visibility = View.VISIBLE
            tvMensajeSinTutor.text = mensaje
        }
    }

    private fun enviarEmail(correo: String) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$correo")
            putExtra(Intent.EXTRA_SUBJECT, "Consulta de tutoría")
        }
        startActivity(Intent.createChooser(intent, "Enviar email"))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}