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
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.unpa.calificaciones.adapters.SemestreAdapter;

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
import mx.edu.unpa.calificacionesunpa.ui.dd.SelectorSemestre;
import mx.edu.unpa.calificacionesunpa.ui.perfil.FragmentPerfil;

public class FragmentCalificacionesAnteriores extends Fragment {
    private static final String TAG = "CalifFrag";

    private TextView txtMatricula;
    private TableLayout tablaCalificaciones;
    private TableLayout tablaExtraordinarios;
    private TextView txtPromedioGeneral;
    private TextView tvTipoCalificacion;
    private TextView tvExtraordinariosLabel;
    private TextView tvNombre;
    private UsuarioService usuarioService;
    private List<Materia> todasMaterias = new ArrayList<>();
    private ImageView ivPerfil;
    private Alumno alumnoActual;

    private String nombre,carrera;

    private RecyclerView contenedorSpinner ;
    private FrameLayout containerSpinner ;
    private View sombra ;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_calificaciones_anteriores, container, false);

        // 1) Referencias UI
        txtMatricula         = root.findViewById(R.id.txtMatricula);
        tablaCalificaciones  = root.findViewById(R.id.tablaCalificaciones);
        tablaExtraordinarios = root.findViewById(R.id.tablaExtraordinarios);
        txtPromedioGeneral   = root.findViewById(R.id.txtPromedioGeneral);
        tvExtraordinariosLabel   = root.findViewById(R.id.tvExtraordinariosLabel);
        tvNombre             = root.findViewById(R.id.tvNombre);

        ivPerfil = root.findViewById(R.id.ivPerfil); // asegúrate que tenga este ID en tu layout
        ivPerfil.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("nombre", nombre);
            bundle.putString("matricula", txtMatricula.getText().toString());
            bundle.putString("carrera", carrera);
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
        txtMatricula.setText(alumnoActual.getMatricula());
        nombre=alumnoActual.getNombre()+" "+alumnoActual.getApPaterno()+" "+alumnoActual.getApMaterno();
        carrera=alumnoActual.getNombreCarrera();
        tvNombre.setText(nombre);


        MaterialButton btnAnterior = root.findViewById(R.id.btnIzquierdo);
        MaterialButton btnSiguiente = root.findViewById(R.id.btnDerecho) ;
        MaterialButton btnSemestreActual = root.findViewById(R.id.btnSemestre);
        contenedorSpinner = root.findViewById(R.id.rvSemestres);
        containerSpinner =  root.findViewById(R.id.contenedorSpinner);
        sombra = root.findViewById(R.id.blurOverlaySpinner);
        // 5) Listener para mostrar/ocultar el spinner
        btnSemestreActual.setOnClickListener(v -> {
            llamarFragmento();
        });
        return root;
    }
    private Map<Integer, String> setupSemestreSelector(){
        Set<DocumentReference> ciclosUnicos = new LinkedHashSet<>();
        for (Materia m : todasMaterias) {
            if (m.getCiclo() != null) {
                ciclosUnicos.add(m.getCiclo());
            }
        }

        // Extraer solo los IDs de los ciclos (último segmento del path)
        Map<Integer, String> semestresMap = new LinkedHashMap<>();
        int contador = 1;
        for (DocumentReference ref : ciclosUnicos) {
            String path = ref.getPath();
            String[] partes = path.split("/");
            if (partes.length > 0) {
                String cicloId = partes[partes.length - 1];
                semestresMap.put(contador++, cicloId); // usar número como clave, cicloId como valor
            }
        }
        return semestresMap;
    }
    private void llamarFragmento() {
        sombra.setVisibility(View.VISIBLE);
        FragmentManager fm = getParentFragmentManager();
        String tag = "SelectorSemestresTag";
        Fragment existing = fm.findFragmentByTag(tag);

        if (existing != null && existing.isVisible()) {
            ocultarSpinnerSiVisible();
        } else {
            Fragment fragmento = SelectorSemestre.newInstance(1,setupSemestreSelector());
            fm.beginTransaction()
                    .replace(R.id.contenedorSpinner, fragmento, tag)
                    .addToBackStack(null)
                    .commit();
            containerSpinner.setVisibility(View.VISIBLE);
        }
    }

    private void ocultarSpinnerSiVisible(){
        boolean isVisible = containerSpinner.getVisibility() == View.VISIBLE;
        sombra.setVisibility(isVisible ? View.GONE : View.VISIBLE);
        containerSpinner.setVisibility(isVisible ? View.GONE : View.VISIBLE);
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
