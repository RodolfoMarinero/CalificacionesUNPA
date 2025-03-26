package mx.edu.unpa.calificacionesunpa.ui.calificacionesactuales;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import mx.edu.unpa.calificacionesunpa.R;

public class FragmentCalificacionesActuales extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Infla el layout del fragmento
        View view = inflater.inflate(R.layout.fragment_calificaciones_actuales, container, false);

        // Referencia al TableLayout
        TableLayout tableLayout = view.findViewById(R.id.tableLayoutCalificacionesActuales);

        // Datos de prueba
        String[][] datosCalificaciones = {
                {"Matemáticas", "9.0", "8.5", "9.2", "8.9", "9.0", "9.0", "7.0", "8.0", "8.5"},
                {"Historia", "8.0", "8.2", "8.8", "8.3", "9.0", "8.5", "7.5", "8.0", "8.2"},
                {"Química", "9.5", "9.0", "8.8", "9.1", "9.2", "9.1", "8.0", "8.5", "9.0"},
                {"Física", "8.5", "8.0", "8.2", "8.2", "8.5", "8.3", "7.8", "7.5", "8.0"},
                {"Inglés", "9.2", "9.0", "8.9", "9.0", "9.5", "9.2", "8.0", "8.5", "9.0"}
        };

        // Agregar filas a la tabla
        for (String[] fila : datosCalificaciones) {
            TableRow tableRow = new TableRow(getContext());

            for (String dato : fila) {
                TextView textView = new TextView(getContext());
                textView.setText(dato);
                textView.setPadding(8, 8, 8, 8);
                //textView.setBackgroundResource(R.drawable.cell_border);  // Agregar borde a cada celda
                tableRow.addView(textView);
            }

            tableLayout.addView(tableRow);
        }

        return view;
    }
}
