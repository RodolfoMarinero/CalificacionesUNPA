package mx.edu.unpa.calificacionesunpa.ui.calificacionesanteriores;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListView;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import mx.edu.unpa.calificacionesunpa.R;
import mx.edu.unpa.calificacionesunpa.ui.*;

public class FragmentCalificacionesAnteriores extends Fragment {

    private Spinner spinnerSemestres;
    private Spinner spinnerFiltro;
    private ExpandableListView expandableListView;
    private TextView txtPromedioGeneral;

    private List<String> materias;
    private HashMap<String, List<String>> calificaciones;
    private CalificacionesExpandableListAdapter adapter;

    public FragmentCalificacionesAnteriores() {
        // Requiere un constructor vacío
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Infla el layout
        View rootView = inflater.inflate(R.layout.fragment_calificaciones_anteriores, container, false);

        // Inicializar los componentes
        spinnerSemestres = rootView.findViewById(R.id.spinnerSemestres);
        spinnerFiltro = rootView.findViewById(R.id.spinnerFiltro);
        expandableListView = rootView.findViewById(R.id.expandableListView);
        txtPromedioGeneral = rootView.findViewById(R.id.txtPromedioGeneral);

        // Configurar Spinner de Semestres
        String[] semestres = {"Semestre actual", "Semestre 1", "Semestre 2", "Semestre 3"};
        ArrayAdapter<String> semestreAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, semestres);
        semestreAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSemestres.setAdapter(semestreAdapter);

        // Configurar Spinner para filtrar por tipo de calificación o materia
        String[] filtros = {"Por Materia", "Por Tipo de Calificación"};
        ArrayAdapter<String> filtroAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, filtros);
        filtroAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFiltro.setAdapter(filtroAdapter);

        // Lógica para cuando se selecciona un semestre
        spinnerSemestres.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                actualizarListaPorMateria(position);  // Actualiza con el semestre seleccionado
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No hacer nada
            }
        });

        // Lógica para cuando se selecciona el filtro de tipo de calificación o materia
        spinnerFiltro.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int semestreSeleccionado = spinnerSemestres.getSelectedItemPosition();
                if (position == 0) { // Filtrar por Materia
                    actualizarListaPorMateria(semestreSeleccionado);
                } else { // Filtrar por Tipo de Calificación
                    actualizarListaPorTipoDeCalificacion(semestreSeleccionado);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No hacer nada
            }
        });

        return rootView;
    }

    private void actualizarListaPorMateria(int semestre) {
        materias = new ArrayList<>();
        calificaciones = new HashMap<>();
        double promedioGeneral = 0;

        switch (semestre) {
            case 1:
                materias.add("Matemáticas");
                materias.add("Historia");

                calificaciones.put("Matemáticas", List.of("Parcial 1: 9.0", "Parcial 2: 8.5", "Parcial 3: 9.2", "Promedio: 8.9"));
                calificaciones.put("Historia", List.of("Parcial 1: 8.0", "Parcial 2: 8.2", "Parcial 3: 8.8", "Promedio: 8.3"));

                promedioGeneral = 8.5;
                break;
            case 2:
                materias.add("Química");
                materias.add("Física");

                calificaciones.put("Química", List.of("Parcial 1: 9.5", "Parcial 2: 9.0", "Parcial 3: 8.8", "Promedio: 9.1"));
                calificaciones.put("Física", List.of("Parcial 1: 8.5", "Parcial 2: 8.0", "Parcial 3: 8.2", "Promedio: 8.2"));

                promedioGeneral = 8.6;
                break;
            default:
                materias.add("Química");
                materias.add("Física");

                calificaciones.put("Química", List.of("Parcial 1: 9.5", "Parcial 2: 9.0", "Parcial 3: 8.8", "Promedio: 9.1"));
                calificaciones.put("Física", List.of("Parcial 1: 8.5", "Parcial 2: 8.0", "Parcial 3: 8.2", "Promedio: 8.2"));

                promedioGeneral = 8.6;
                break;
        }

        txtPromedioGeneral.setText("Tu promedio general es: " + promedioGeneral);
        adapter = new CalificacionesExpandableListAdapter(requireContext(), materias, calificaciones);
        expandableListView.setAdapter(adapter);
    }

    private void actualizarListaPorTipoDeCalificacion(int semestre) {
        materias = new ArrayList<>();
        calificaciones = new HashMap<>();
        double promedioGeneral = 0;

        switch (semestre) {
            case 1:
                materias.add("Parcial 1");
                materias.add("Parcial 2");

                calificaciones.put("Parcial 1", List.of("Matemáticas: 9.0", "Historia: 8.0"));
                calificaciones.put("Parcial 2", List.of("Matemáticas: 8.5", "Historia: 8.2"));

                promedioGeneral = 8.5;
                break;
            case 2:
                materias.add("Parcial 1");
                materias.add("Parcial 2");

                calificaciones.put("Parcial 1", List.of("Química: 9.5", "Física: 8.5"));
                calificaciones.put("Parcial 2", List.of("Química: 9.0", "Física: 8.0"));

                promedioGeneral = 8.6;
                break;
            default:
                materias.add("Parcial 1");
                materias.add("Parcial 2");

                calificaciones.put("Parcial 1", List.of("Química: 9.5", "Física: 8.5"));
                calificaciones.put("Parcial 2", List.of("Química: 9.0", "Física: 8.0"));

                promedioGeneral = 8.6;
                break;
        }

        txtPromedioGeneral.setText("Tu promedio general es: " + promedioGeneral);
        adapter = new CalificacionesExpandableListAdapter(requireContext(), materias, calificaciones);
        expandableListView.setAdapter(adapter);
    }
}
