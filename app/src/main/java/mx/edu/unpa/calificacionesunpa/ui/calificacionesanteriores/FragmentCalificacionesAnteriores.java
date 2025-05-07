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
import java.util.*;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import mx.edu.unpa.calificacionesunpa.R;
import mx.edu.unpa.calificacionesunpa.models.Calificaciones;
import mx.edu.unpa.calificacionesunpa.models.Materia;
import mx.edu.unpa.calificacionesunpa.models.StudentBasic;
import mx.edu.unpa.calificacionesunpa.providers.*;

public class FragmentCalificacionesAnteriores extends Fragment {
    private static final String TAG = "CalifFrag";

    private TextView txtMatricula;
    private Spinner spinnerSemestres;
    private TableLayout tablaCalificaciones;
    private TableLayout tablaExtraordinarios;
    private TextView txtPromedioGeneral;
    private TextView tvTipoCalificacion;
    private TextView tvExtraordinariosLabel;


    private StudentProviderJ studentProviderJ;
    private MateriaProvider materiaProvider;
    private CalificacionesProvider califProvider;

    private final List<Materia> todasMaterias = new ArrayList<>();
    private int currentSemester = 1;

    private boolean materiasYaCargadas = false;

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


        txtPromedioGeneral.setVisibility(View.GONE);
        tvTipoCalificacion.setText("");

        // 2) Inicializar providers
        studentProviderJ = new StudentProviderJ();
        materiaProvider  = new MateriaProvider();
        califProvider    = new CalificacionesProvider();

        // 3) Obtener email
        String email = null;
        if (getArguments() != null) {
            email = getArguments().getString("email");
        }
        if (email == null && FirebaseAuth.getInstance().getCurrentUser() != null) {
            email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        }
        Log.d(TAG, "Email para consulta: " + email);

        // 4) Traer alumno básico
        if (email != null) {
            studentProviderJ.fetchBasicByEmail(email, new StudentProviderJ.StudentBasicCallback() {
                @Override
                public void onSuccess(@NonNull StudentBasic student) {
                    Log.d(TAG, "Alumno obtenido: " + student.getNombre() +
                            ", matrícula: " + student.getMatricula() +
                            ", materias paths: " + student.getMaterias());
                    txtMatricula.setText(student.getMatricula());
                    if (!materiasYaCargadas) {
                        materiasYaCargadas = true;
                        todasMaterias.clear();
                        loadAllMaterias(student.getMaterias(), 0);
                    } else {
                        Log.d(TAG, "Materias ya cargadas, omitiendo recarga");
                        setupSpinner();
                    }
                }
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, "Error fetchBasicByEmail", e);
                    Toast.makeText(requireContext(),
                            "Error al obtener alumno: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                }
            });
        } else {
            Log.w(TAG, "Email nulo, no se puede consultar");
            Toast.makeText(requireContext(),
                    "Correo no disponible para consulta",
                    Toast.LENGTH_LONG).show();
        }

        return root;
    }

    private void loadAllMaterias(List<String> paths, int index) {
        Log.d(TAG, "loadAllMaterias index=" + index + " de " + paths.size());
        if (index >= paths.size()) {
            Log.d(TAG, "Todas materias cargadas: " + todasMaterias.size());
            setupSpinner();
            return;
        }
        String fullPath = paths.get(index);
        String materiaId = fullPath.contains("/") ?
                fullPath.substring(fullPath.lastIndexOf('/') + 1) : fullPath;
        Log.d(TAG, "→ cargando materiaId: " + materiaId);

        materiaProvider.getMateriaById(materiaId, new Function1<Materia, Unit>() {
            @Override
            public Unit invoke(Materia materia) {
                if (materia != null) {
                    Log.d(TAG, "Materia recuperada: id=" + materia.getId() +
                            ", nombre=" + materia.getNombre() +
                            ", semestre=" + materia.getSemestre());
                    todasMaterias.add(materia);
                } else {
                    Log.w(TAG, "Materia NULL para id=" + materiaId);
                }
                loadAllMaterias(paths, index + 1);
                return Unit.INSTANCE;
            }
        });
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
        currentSemester = 1;
        for (Materia m : todasMaterias) {
            int sem = semestreToInt(m.getSemestre());
            if (sem > currentSemester) currentSemester = sem;
        }
        Log.d(TAG, "setupSpinner → currentSemester=" + currentSemester +
                ", total materias=" + todasMaterias.size());

        // Construye la lista de ítems
        List<String> items = new ArrayList<>();
        for (int i = 1; i < currentSemester; i++) {
            items.add("Semestre " + i);
        }
        items.add("Semestre actual");  // Siempre el último

        Log.d(TAG, "Spinner items: " + items);

        // Crea el adapter y lo asocia
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                items
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSemestres.setAdapter(adapter);

        // Listener
        spinnerSemestres.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int semElegido = position < currentSemester - 1
                        ? position + 1
                        : currentSemester;
                Log.d(TAG, "Spinner posición=" + position + ", semestre=" + semElegido);
                loadGradesForSemester(semElegido);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) { }
        });

        // ➡️ Selecciona por defecto el último elemento (Semestre actual)
        int lastIndex = items.size() - 1;
        Log.d(TAG, "Seleccionando semestre actual en posición " + lastIndex);
        spinnerSemestres.setSelection(lastIndex);
    }


    private void loadGradesForSemester(int semestre) {
        Log.d(TAG, "loadGradesForSemester semestre=" + semestre);
        // Limpia las tablas
        if (tablaCalificaciones.getChildCount() > 1)
            tablaCalificaciones.removeViews(1, tablaCalificaciones.getChildCount() - 1);
        if (tablaExtraordinarios.getChildCount() > 1)
            tablaExtraordinarios.removeViews(1, tablaExtraordinarios.getChildCount() - 1);

        tablaExtraordinarios.setVisibility(View.GONE);
        tvExtraordinariosLabel.setVisibility(View.GONE);
        List<Materia> filtradas = new ArrayList<>();
        for (Materia m : todasMaterias) {
            if (semestreToInt(m.getSemestre()) == semestre) {
                filtradas.add(m);
            }
        }
        Log.d(TAG, "Materias filtradas: " + filtradas.size());
        if (filtradas.isEmpty()) {
            Toast.makeText(requireContext(),
                    "No hay materias para este semestre",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        final boolean[] hasExtra = { false };

        List<Double> definitivas = new ArrayList<>();
        for (Materia mat : filtradas) {
            califProvider.getCalificacionesByPath(mat.getCalificaciones(),
                    new CalificacionesProvider.CalificacionesCallback() {
                        @Override
                        public void onCallback(Calificaciones cal) {
                            Log.d(TAG, "Calificaciones de " + mat.getNombre() + ": " + cal);
                            // calcular definitiva
                            double def = 0;
                            if (cal != null) {
                                if (cal.getCalificacionDefinitiva() != null) {
                                    def = cal.getCalificacionDefinitiva();
                                } else if (cal.getEspecial() != null) {
                                    def = cal.getEspecial();
                                } else if (cal.getPromedioParciales() != null) {
                                    def = cal.getPromedioParciales();
                                }
                            }
                            definitivas.add(def);

                            // ¿tiene datos regulares?
                            boolean tieneRegular = cal != null && (
                                    cal.getParcial_1() != null ||
                                            cal.getParcial_2() != null ||
                                            cal.getParcial_3() != null ||
                                            cal.getPromedioParciales() != null ||
                                            cal.getExamenFinal() != null
                            );
                            if (tieneRegular) {
                                TableRow row = new TableRow(requireContext());
                                row.setGravity(Gravity.CENTER);
                                addCell(row, mat.getNombre());
                                addCell(row, format(cal != null ? cal.getParcial_1() : null));
                                addCell(row, format(cal != null ? cal.getParcial_2() : null));
                                addCell(row, format(cal != null ? cal.getParcial_3() : null));
                                addCell(row, format(cal != null ? cal.getPromedioParciales() : null));
                                addCell(row, format(cal != null ? cal.getExamenFinal() : null));
                                addCell(row, String.format(Locale.getDefault(), "%.1f", def));
                                tablaCalificaciones.addView(row);
                            }

                            //  🚩 EXTRAORDINARIOS 🚩
                            boolean tieneExtra = cal != null && (
                                    (cal.getExtra_1() != null && cal.getExtra_1() > 0) ||
                                            (cal.getExtra_2() != null && cal.getExtra_2() > 0) ||
                                            (cal.getEspecial() != null && cal.getEspecial() > 0)
                            );
                            if (tieneExtra) {
                                hasExtra[0] = true;
                                TableRow rowEx = new TableRow(requireContext());
                                rowEx.setGravity(Gravity.CENTER);
                                // reutiliza addCell, lo mejoraremos abajo
                                addCell(rowEx, mat.getNombre());
                                addCell(rowEx, format(cal.getExtra_1()));
                                addCell(rowEx, format(cal.getExtra_2()));
                                addCell(rowEx, format(cal.getEspecial()));
                                tablaExtraordinarios.addView(rowEx);
                            }

                            // cuando todas llegaron, calculamos promedio
                            if (definitivas.size() == filtradas.size()) {
                                double sum = 0;
                                for (Double d : definitivas) sum += d;
                                double avg = sum / definitivas.size();
                                Log.d(TAG, "Promedio general = " + avg);
                                txtPromedioGeneral.setText(
                                        String.format(Locale.getDefault(), "Promedio: %.2f", avg)
                                );
                                txtPromedioGeneral.setVisibility(View.VISIBLE);
                                tvTipoCalificacion.setText(
                                        avg >= 9.0 ? "Regular" : "Irregular"
                                );
                                if (hasExtra[0]) {
                                    tvExtraordinariosLabel.setVisibility(View.VISIBLE);
                                    tablaExtraordinarios.setVisibility(View.VISIBLE);
                                } else {
                                    tvExtraordinariosLabel.setVisibility(View.GONE);
                                    tablaExtraordinarios.setVisibility(View.GONE);
                                }
                            }
                        }
                    });
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
