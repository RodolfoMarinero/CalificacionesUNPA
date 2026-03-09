package mx.edu.unpa.calificacionesunpa.ui.tutor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import mx.edu.unpa.calificacionesunpa.databinding.FragmentActualizarCorreoBinding
import mx.edu.unpa.calificacionesunpa.data.persistent.UsuarioService
import mx.edu.unpa.calificacionesunpa.data.repository.TutorRepository
import javax.inject.Inject

@AndroidEntryPoint
class ActualizarCorreoFragment : Fragment() {

    private var _binding: FragmentActualizarCorreoBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var tutorRepository: TutorRepository

    private val viewModel: TutorViewModel by viewModels {
        TutorViewModelFactory(tutorRepository)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentActualizarCorreoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cargarCorreoActual()
        setupListeners()
        setupObservers()
    }

    private fun cargarCorreoActual() {

        val correoActual = UsuarioService.recuperarCorreo(requireContext())

        if (!correoActual.isNullOrEmpty()) {
            binding.etCorreo.setText(correoActual)
        }
    }

    private fun setupListeners() {
        binding.btnGuardar.setOnClickListener {
            val correo = binding.etCorreo.text.toString().trim()

            if (correo.isEmpty()) {
                Snackbar.make(binding.root, "Ingresa tu correo", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
                Snackbar.make(binding.root, "Formato de correo inválido", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            confirmarActualizacion(correo)
        }
    }

    private fun setupObservers() {
        viewModel.correoActualizado.observe(viewLifecycleOwner) { response ->
            if (response.exito) {
                // Actualizar en memoria
                response.correo?.let { correo ->
                    UsuarioService.guardarCorreo(requireContext(), correo)
                }
                AlertDialog.Builder(requireContext())
                    .setTitle("✅ Correo actualizado")
                    .setMessage(response.mensaje)
                    .setPositiveButton("Aceptar") { _, _ ->
                        requireActivity().onBackPressed()
                    }
                    .setCancelable(false)
                    .show()
            } else {
                Snackbar.make(
                    binding.root,
                    response.error ?: "Error desconocido",
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }

        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnGuardar.isEnabled = !isLoading
            binding.etCorreo.isEnabled = !isLoading
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun confirmarActualizacion(correo: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Confirmar")
            .setMessage("¿Deseas actualizar tu correo a:\n$correo?")
            .setPositiveButton("Confirmar") { _, _ ->
                actualizarCorreo(correo)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun actualizarCorreo(correo: String) {
        val matricula = UsuarioService.recuperarMatricula(requireContext()) ?: run {
            Snackbar.make(binding.root, "No se encontró tu matrícula", Snackbar.LENGTH_LONG).show()
            return
        }

        viewModel.actualizarCorreo(matricula, correo)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}