package mx.edu.unpa.calificacionesunpa.ui.calificacionesanteriores;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Button;

import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;

import mx.edu.unpa.calificacionesunpa.R;

public class FragmentCalificacionesAnteriores extends Fragment {

    private Spinner spinnerSemestres;
    private TableLayout tablaCalificaciones;
    private TextView txtPromedioGeneral;
    private Button btnVerPromedio;

    public FragmentCalificacionesAnteriores() {
        // Constructor vacío requerido
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_calificaciones_anteriores, container, false);

        spinnerSemestres = rootView.findViewById(R.id.spinnerSemestres);
        tablaCalificaciones = rootView.findViewById(R.id.tablaCalificaciones);
        btnVerPromedio = rootView.findViewById(R.id.btnVerPromedio);

        // Semestres disponibles
        String[] semestres = {"Semestre actual", "Semestre 1", "Semestre 2", "Semestre 3"};
        ArrayAdapter<String> semestreAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, semestres);
        semestreAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSemestres.setAdapter(semestreAdapter);

        // Acción al cambiar de semestre
        spinnerSemestres.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                mostrarCalificaciones(position);
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                // Nada
            }
        });

        // Acción al presionar "Ver Promedio"
        btnVerPromedio.setOnClickListener(v -> {
            txtPromedioGeneral.setVisibility(View.VISIBLE);
        });

        return rootView;
    }

    private void mostrarCalificaciones(int semestreSeleccionado) {
        tablaCalificaciones.removeViews(1, Math.max(0, tablaCalificaciones.getChildCount() - 1)); // Limpia menos la cabecera

        List<String[]> datos = new ArrayList<>();
        double suma = 0;
        int cantidad = 0;

        switch (semestreSeleccionado) {
            case 1:
                datos.add(new String[]{"Matemáticas", "9", "8.5", "9.2", "9", "8", "8.9"});
                datos.add(new String[]{"Historia", "8", "8.2", "8.8", "9", "8", "8.4"});
                break;
            case 2:
                datos.add(new String[]{"Física", "7", "8", "8.5", "8", "9", "8.3"});
                datos.add(new String[]{"Química", "9.5", "9", "8.8", "9", "9", "9.0"});
                break;
            default:
                datos.add(new String[]{"Programación", "10", "9", "9.5", "10", "9", "9.5"});
                datos.add(new String[]{"Bases de Datos", "9", "9", "9", "9", "9", "9.0"});
                break;
        }

        for (String[] fila : datos) {
            TableRow tableRow = new TableRow(getContext());

            for (String celda : fila) {
                TextView textView = new TextView(getContext());
                textView.setText(celda);
                textView.setPadding(8, 8, 8, 8);
                textView.setGravity(android.view.Gravity.CENTER);
                tableRow.addView(textView);
            }

            tablaCalificaciones.addView(tableRow);

            try {
                suma += Double.parseDouble(fila[6]);
                cantidad++;
            } catch (NumberFormatException ignored) {}
        }

        double promedio = (cantidad > 0) ? suma / cantidad : 0;    }
}
