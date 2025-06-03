package mx.edu.unpa.calificacionesunpa.ui.calificacionesanteriores;

import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;

import org.w3c.dom.Document;

import java.util.*;
import java.util.stream.Collectors;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import mx.edu.unpa.calificacionesunpa.R;
import mx.edu.unpa.calificacionesunpa.models.Alumno;
import mx.edu.unpa.calificacionesunpa.models.Calificaciones;
import mx.edu.unpa.calificacionesunpa.models.Materia;
import mx.edu.unpa.calificacionesunpa.models.StudentBasic;
import mx.edu.unpa.calificacionesunpa.providers.*;
import mx.edu.unpa.calificacionesunpa.service.UsuarioService;
import mx.edu.unpa.calificacionesunpa.ui.perfil.FragmentPerfil;

public class FragmentCalificacionesAnteriores extends Fragment {
    private static final String TAG = "CalifFrag";

    private TextView txtMatricula;
    private Spinner spinnerSemestres;
    private TableLayout tablaCalificaciones;
    private TableLayout tablaExtraordinarios;
    private TextView txtPromedioGeneral;
    private TextView tvTipoCalificacion;
    private TextView tvExtraordinariosLabel;
    private UsuarioService usuarioService;
    private List<Materia> todasMaterias = new ArrayList<>();
    private ImageView ivPerfil;
    private Alumno alumnoActual;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_calificaciones_anteriores, container, false);

        // 1) Referencias UI
        txtMatricula         = root.findViewById(R.id.txtMatricula);
        spinnerSemestres     = root.findViewById(R.id.spinnerSemestres);
        tablaCalificaciones  = root.findViewById(R.id.tablaCalificaciones);
        tablaExtraordinarios = root.findViewById(R.id.tablaExtraordinarios);
        txtPromedioGeneral   = root.findViewById(R.id.txtPromedioGeneral);
        tvTipoCalificacion   = root.findViewById(R.id.tvTipoCalificacion);
        tvExtraordinariosLabel   = root.findViewById(R.id.tvExtraordinariosLabel);

        ivPerfil = root.findViewById(R.id.ivPerfil); // asegúrate que tenga este ID en tu layout
        ivPerfil.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("nombre", "Rodolfo Marinero Cruz"); // Reemplaza con datos reales
            bundle.putString("matricula", txtMatricula.getText().toString());
            bundle.putString("carrera", "Ingeniería en Computación");
            bundle.putString("promedio", txtPromedioGeneral.getText().toString().replace("Promedio: ", ""));
            bundle.putString("codigo", txtMatricula.getText().toString());

            FragmentPerfil fragment = new FragmentPerfil();
            fragment.setArguments(bundle);

            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.nav_host_fragment_content_main, fragment) // Usa el contenedor correcto
                    .addToBackStack(null)
                    .commit();
        });



        txtPromedioGeneral.setVisibility(View.GONE);
        tvTipoCalificacion.setText("");

        // 2) Inicializar providers
        usuarioService = UsuarioService.INSTANCE;
        // 4) Traer alumno básico
        alumnoActual = usuarioService.getAlumnoActual();
        todasMaterias = alumnoActual.getMaterias();

        setupSpinner();
        return root;
    }
    private int semestreToInt(@Nullable String sem) {
        if (sem == null) return 1;
        try {
            return Integer.parseInt(sem);
        } catch (NumberFormatException e) {
            String s = sem.trim().toUpperCase(Locale.ROOT);
            switch (s) {
                case "PRIMERO":   return 1;
                case "SEGUNDO":   return 2;
                case "TERCERO":   return 3;
                case "CUARTO":    return 4;
                case "QUINTO":    return 5;
                case "SEXTO":     return 6;
                case "SÉPTIMO":
                case "SEPTIMO":   return 7;
                case "OCTAVO":    return 8;
                case "NOVENO":    return 9;
                case "DÉCIMO":
                case "DECIMO":    return 10;
                default:
                    Log.w(TAG, "semestreToInt: formato desconocido '" + sem + "', asumiendo 1");
                    return 1;
            }
        }
    }

    private void setupSpinner() {
        Set<DocumentReference> ciclosUnicos = new LinkedHashSet<>();
        for (Materia m : todasMaterias) {
            if (m.getCiclo() != null) {
                ciclosUnicos.add(m.getCiclo());
            }
        }

        // Extraer solo los IDs de los ciclos (último segmento del path)
        List<String> items = new ArrayList<>();
        for (DocumentReference ref : ciclosUnicos) {
            String path = ref.getPath();
            // Dividir el path y obtener el último segmento
            String[] partes = path.split("/");
            if (partes.length > 0) {
                String cicloId = partes[partes.length - 1];
                items.add(cicloId);
            }
        }

        // Ordenar alfabéticamente
        Collections.sort(items);

        Log.d(TAG, "Ciclos en el Spinner: " + items);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                items
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSemestres.setAdapter(adapter);

        spinnerSemestres.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String cicloId = items.get(position);
                Log.d(TAG, "Ciclo seleccionado: " + cicloId);

                // Reconstruir la referencia completa
                String fullPath = "ciclosEscolares/" + cicloId;
                loadGradesForCycle(fullPath);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        if (!items.isEmpty()) {
            spinnerSemestres.setSelection(items.size() - 1);
        }
        spinnerSemestres.setVisibility(View.VISIBLE);
    }
    private void loadGradesForCycle(String cicloEscolar) {
        Log.d(TAG, "loadGradesForCycle ciclo=" + cicloEscolar);

        // Limpia las tablas
        if (tablaCalificaciones.getChildCount() > 1)
            tablaCalificaciones.removeViews(1, tablaCalificaciones.getChildCount() - 1);
        if (tablaExtraordinarios.getChildCount() > 1)
            tablaExtraordinarios.removeViews(1, tablaExtraordinarios.getChildCount() - 1);

        tablaExtraordinarios.setVisibility(View.GONE);
        tvExtraordinariosLabel.setVisibility(View.GONE);

        List<Materia> filtradas = new ArrayList<>();
        for (Materia m : todasMaterias) {
            if (cicloEscolar.equals(Objects.requireNonNull(m.getCiclo()).getPath())) {
                filtradas.add(m);
            }
        }
        
        Log.d(TAG, "Materias filtradas por ciclo: " + filtradas.size());
        if (filtradas.isEmpty()) {
            Toast.makeText(requireContext(),
                    "No hay materias para este ciclo escolar",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        boolean[] hasExtra = { false };
        for (Materia mat : filtradas) {
            Log.d(TAG, "Materia: " + mat.getMateria() + ", calificaciones: " + mat.getCalificaciones());
            if (alumnoActual.getEsRegular()) {
                TableRow row = new TableRow(requireContext());
                row.setGravity(Gravity.CENTER);
                addCell(row, mat.getMateria());
                addCell(row, format(mat.getCalificaciones().getParcial1() != null ? mat.getCalificaciones().getParcial1() : null));
                addCell(row, format(mat.getCalificaciones().getParcial2() != null ? mat.getCalificaciones().getParcial2() : null));
                addCell(row, format(mat.getCalificaciones().getParcial3() != null ? mat.getCalificaciones().getParcial3() : null));
                addCell(row, format(mat.getPromedioParciales() != 0.0 ? mat.getPromedioParciales() : null));
                addCell(row, format(mat.getCalificaciones().getOrdinario() != null ? mat.getCalificaciones().getOrdinario() : null));
                addCell(row, String.format(Locale.getDefault(), "%.1f", mat.getCalificaciones().getPFinal()));
                tablaCalificaciones.addView(row);
            }

            // Extraordinarios
            boolean tieneExtra = (
                    mat.getCalificaciones().getExtraOrdinario1() != null||
                            mat.getCalificaciones().getExtraOrdinario2() != null ||
                            mat.getCalificaciones().getEspecial() != null
            );
            if (tieneExtra) {
                tablaExtraordinarios.setVisibility(View.VISIBLE);
                tvExtraordinariosLabel.setVisibility(View.VISIBLE);
                hasExtra[0] = true;
                TableRow rowEx = new TableRow(requireContext());
                rowEx.setGravity(Gravity.CENTER);
                addCell(rowEx, mat.getMateria());
                addCell(rowEx, format(mat.getCalificaciones().getExtraOrdinario1()));
                addCell(rowEx, format(mat.getCalificaciones().getExtraOrdinario2()));
                addCell(rowEx, format(mat.getCalificaciones().getEspecial()));
                tablaExtraordinarios.addView(rowEx);
            }
        }
    }
    private void addCell(TableRow row, String texto) {
        TextView tv = new TextView(requireContext());
        tv.setText(texto);
        tv.setPadding(8, 8, 8, 8);

        // 1) Centrado completo
        tv.setGravity(Gravity.CENTER);
        tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

        // 2) Multi‐línea
        tv.setSingleLine(false);
        tv.setMaxLines(3);

        // 3) LayoutParams con “peso” para ancho fijo
        TableRow.LayoutParams lp = new TableRow.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
        );
        tv.setLayoutParams(lp);

        row.addView(tv);
    }


    private String format(Double v) {
        return v != null
                ? String.format(Locale.getDefault(), "%.1f", v)
                : "-";
    }
}
