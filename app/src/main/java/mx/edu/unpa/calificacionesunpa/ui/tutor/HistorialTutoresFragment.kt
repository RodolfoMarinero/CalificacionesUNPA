package mx.edu.unpa.calificacionesunpa.ui.tutor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import mx.edu.unpa.calificacionesunpa.databinding.FragmentHistorialTutoresBinding
import mx.edu.unpa.calificacionesunpa.data.persistent.UsuarioService
import mx.edu.unpa.calificacionesunpa.data.repository.TutorRepository
import javax.inject.Inject
@AndroidEntryPoint
class HistorialTutoresFragment : Fragment() {

    private var _binding: FragmentHistorialTutoresBinding? = null
    private val binding get() = _binding!!
    @Inject
    lateinit var tutorRepository: TutorRepository

    private val viewModel: TutorViewModel by viewModels {
        TutorViewModelFactory(tutorRepository)
    }

    private lateinit var adapter: HistorialAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistorialTutoresBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupObservers()
        cargarHistorial()
    }

    private fun setupRecyclerView() {
        adapter = HistorialAdapter()
        binding.recyclerHistorial.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@HistorialTutoresFragment.adapter
        }
    }

    private fun setupObservers() {
        viewModel.historial.observe(viewLifecycleOwner) { historial ->
            if (historial.isEmpty()) {
                mostrarSinHistorial()
            } else {
                adapter.submitList(historial)
                binding.recyclerHistorial.visibility = View.VISIBLE
                binding.cardSinHistorial.visibility = View.GONE
            }
        }

        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun cargarHistorial() {

        val matricula = UsuarioService.recuperarMatricula(requireContext()) ?: run {
            Snackbar.make(binding.root, "No se encontró tu matrícula", Snackbar.LENGTH_LONG).show()
            return
        }
        viewModel.cargarHistorial(matricula)
    }
    private fun obtenerPeriodoActual(): String {
        return UsuarioService.getPeriodoActual(requireContext())
    }
    private fun mostrarSinHistorial() {
        binding.recyclerHistorial.visibility = View.GONE
        binding.cardSinHistorial.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}