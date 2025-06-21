package mx.edu.unpa.calificacionesunpa.ui.calificacionesanteriores;

import android.graphics.Color;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.lifecycle.Observer;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentReference;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.os.Build;
import android.provider.MediaStore;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;


import org.w3c.dom.Document;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import mx.edu.unpa.calificacionesunpa.R;
import mx.edu.unpa.calificacionesunpa.models.Alumno;
import mx.edu.unpa.calificacionesunpa.models.Materia;
import mx.edu.unpa.calificacionesunpa.providers.StorageProvider;
import mx.edu.unpa.calificacionesunpa.service.PromedioCalculatorService;
import mx.edu.unpa.calificacionesunpa.service.UsuarioService;
import mx.edu.unpa.calificacionesunpa.ui.dd.SelectorSemestre;
import mx.edu.unpa.calificacionesunpa.ui.perfil.FragmentPerfilN;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import com.google.firebase.auth.FirebaseAuth;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.File;
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
    private Map<Integer, String> semestresMapa;

    private MaterialButton btnAnterior;
    private MaterialButton btnSiguiente;
    private MaterialButton btnSemestreActual;
    private int idxCicloActual = 1; // Índice del ciclo actual, empieza en 1
    private PromedioCalculatorService promedioCalculatorService;

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

        ivPerfil = root.findViewById(R.id.ivPerfil);

        File file = new File(requireContext().getFilesDir(), "imagen_perfil.png");
        if (file.exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
            ivPerfil.setImageBitmap(bitmap);
        } else {
            // Si no está local, aún puedes hacer fallback a Firebase si deseas
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            StorageProvider provider = new StorageProvider();
            provider.getImageByUserId(userId, new OnResultCallback() {
                @Override
                public void onResult(@Nullable String base64) {
                    if (base64 != null) {
                        byte[] imageBytes = Base64.decode(base64, Base64.DEFAULT);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                        ivPerfil.setImageBitmap(bitmap);
                    }
                }
            });
        }
        ivPerfil.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("nombre", nombre);
            bundle.putString("matricula", txtMatricula.getText().toString());
            bundle.putString("carrera", carrera);
            bundle.putString("promedio", txtPromedioGeneral.getText().toString().replace("Promedio: ", ""));
            bundle.putString("codigo", txtMatricula.getText().toString());

            FragmentPerfilN fragment = new FragmentPerfilN();
            fragment.setArguments(bundle);

            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.nav_host_fragment_content_main, fragment) // Usa el contenedor correcto
                    .addToBackStack(null)
                    .commit();
        });





        txtPromedioGeneral.setVisibility(View.GONE);

        // 2) Inicializar providers
        usuarioService = UsuarioService.INSTANCE;
        promedioCalculatorService = PromedioCalculatorService.INSTANCE;
        // 4) Traer alumno básico
        alumnoActual = usuarioService.getAlumnoActual();
        todasMaterias = alumnoActual.getMaterias();
        promedioCalculatorService.calcularPromedioGeneral(todasMaterias);
        txtMatricula.setText(alumnoActual.getMatricula());
        nombre=alumnoActual.getNombre()+" "+alumnoActual.getApPaterno()+" "+alumnoActual.getApMaterno();
        carrera=alumnoActual.getNombreCarrera();
        tvNombre.setText(nombre);


        btnAnterior = root.findViewById(R.id.btnIzquierdo);
        btnSiguiente = root.findViewById(R.id.btnDerecho);
        btnSemestreActual = root.findViewById(R.id.btnSemestre);

        contenedorSpinner = root.findViewById(R.id.rvSemestres);
        contenedorSpinner.setVisibility(View.VISIBLE);
        containerSpinner =  root.findViewById(R.id.contenedorSpinner);
        sombra = root.findViewById(R.id.blurOverlaySpinner);
        sombra.setOnClickListener(v->{
            ocultarSpinnerSiVisible();
        });
        //Observa ciclo actual
        usuarioService.getSemestreSeleccionado().observe(getViewLifecycleOwner(), (Observer<Integer>) semestre -> {
            if (semestre != null) {
                idxCicloActual = semestre;
                loadGradesForCycle();
                ocultarSpinnerSiVisible();
            }
        });

        //crear mapa de semestres y seleccionar el semestre actual
        setupSemestreSelector();
        int ultimoCiclo = semestresMapa.keySet().stream().max(Comparator.comparingInt(a -> a)).orElse(0);
        usuarioService.seleccionarSemestre(ultimoCiclo);
        btnSemestreActual.setText(semestresMapa.get(ultimoCiclo));
        // 5) Listener para mostrar/ocultar el spinner
        btnSemestreActual.setOnClickListener(v -> {
            llamarFragmento();
        });
        //Listeners para los botones de navegación
        btnAnterior.setOnClickListener(v -> {
            if (tieneAnterior()) {
                usuarioService.seleccionarSemestre(idxCicloActual -1);
            }
        });
        btnSiguiente.setOnClickListener(v -> {
            if (tieneSiguiente()) {
                usuarioService.seleccionarSemestre(idxCicloActual + 1);
            }
        });
        ocultarSpinnerSiVisible();
        return root;
    }
    private void setupSemestreSelector(){
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
        semestresMapa = semestresMap;
    }
    private void llamarFragmento() {
        sombra.setVisibility(View.VISIBLE);
        FragmentManager fm = getParentFragmentManager();
        String tag = "SelectorSemestresTag";

        Fragment existing = fm.findFragmentByTag(tag);

        // Si ya existe uno, elimínalo antes de añadir uno nuevo
        if (existing != null) {
            fm.beginTransaction().remove(existing).commitNow();
        }

        Fragment fragmento = SelectorSemestre.newInstance(1, semestresMapa);
        fm.beginTransaction()
                .replace(R.id.contenedorSpinner, fragmento, tag)
                .commit();

        containerSpinner.setVisibility(View.VISIBLE);
    }

    private void ocultarSpinnerSiVisible(){
        boolean isVisible = containerSpinner.getVisibility() == View.VISIBLE;
        if (isVisible){
            sombra.setVisibility(View.GONE);
            containerSpinner.setVisibility(View.GONE);
        }
    }
    private void loadGradesForCycle() {
        String cicloEscolar = semestresMapa.get(idxCicloActual);
        Log.d(TAG, "loadGradesForCycle ciclo=" + cicloEscolar);
        btnSemestreActual.setText(cicloEscolar);
        cicloEscolar = "ciclosEscolares/" + cicloEscolar;
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
                addCell(row, format(mat.getCalificaciones().getPFinal() != null ? mat.getCalificaciones().getPFinal() : null));
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

    public void generarPdf(View view) {
        PdfDocument documento = new PdfDocument();
        Paint paint = new Paint();

        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page page = documento.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        int x = 10, y = 25;
        int rowSpacing = 20;

        int colMateriaWidth = 200;
        int colOthersWidth = 60;

        // 🧑 Datos del alumno
        paint.setTextSize(14);
        paint.setFakeBoldText(true);
        canvas.drawText("Reporte de Calificaciones", x, y, paint);
        y += rowSpacing;

        paint.setFakeBoldText(false);
        canvas.drawText("Nombre: " + nombre, x, y, paint); y += rowSpacing;
        canvas.drawText("Matrícula: " + alumnoActual.getMatricula(), x, y, paint); y += rowSpacing;
        canvas.drawText("Carrera: " + alumnoActual.getNombreCarrera(), x, y, paint); y += rowSpacing;

        if (txtPromedioGeneral.getVisibility() == View.VISIBLE && !txtPromedioGeneral.getText().toString().isEmpty()) {
            canvas.drawText(txtPromedioGeneral.getText().toString(), x, y, paint);
            y += rowSpacing;
        }

        y += 10;

        // 🧾 Encabezados tabla principal
        paint.setTextSize(12);
        paint.setFakeBoldText(true);
        String[] headers = {"Materia", "1er.", "2o.", "3er.", "P.P", "E.F", "CAL.DEF"};
        canvas.drawText(headers[0], x, y, paint);
        for (int j = 1; j < headers.length; j++) {
            canvas.drawText(headers[j], x + colMateriaWidth + (j - 1) * colOthersWidth, y, paint);
        }
        y += rowSpacing;

        paint.setFakeBoldText(false);

        // 📋 Filas calificaciones normales
        for (int i = 1; i < tablaCalificaciones.getChildCount(); i++) {
            TableRow fila = (TableRow) tablaCalificaciones.getChildAt(i);
            TextView celdaMateria = (TextView) fila.getChildAt(0);

            String materiaNombre = celdaMateria.getText().toString();
            List<String> lineasMateria = dividirTexto(materiaNombre, 30);
            for (int k = 0; k < lineasMateria.size(); k++) {
                canvas.drawText(lineasMateria.get(k), x, y + (k * 12), paint);
            }

            for (int j = 1; j < fila.getChildCount(); j++) {
                TextView celda = (TextView) fila.getChildAt(j);
                canvas.drawText(celda.getText().toString(), x + colMateriaWidth + (j - 1) * colOthersWidth, y, paint);
            }

            y += rowSpacing + (lineasMateria.size() - 1) * 12;
            if (y > 800) {
                documento.finishPage(page);
                page = documento.startPage(pageInfo);
                canvas = page.getCanvas();
                y = 25;
            }
        }

        // 📌 Tabla de extraordinarios
        if (tablaExtraordinarios.getVisibility() == View.VISIBLE && tablaExtraordinarios.getChildCount() > 1) {
            y += 30;
            paint.setFakeBoldText(true);
            canvas.drawText("Calificaciones Extraordinarias", x, y, paint);
            y += rowSpacing;

            paint.setFakeBoldText(false);
            String[] headersExtra = {"Materia", "1er", "2o", "E.E"};
            canvas.drawText(headersExtra[0], x, y, paint);
            for (int j = 1; j < headersExtra.length; j++) {
                canvas.drawText(headersExtra[j], x + colMateriaWidth + (j - 1) * colOthersWidth, y, paint);
            }
            y += rowSpacing;

            for (int i = 1; i < tablaExtraordinarios.getChildCount(); i++) {
                TableRow fila = (TableRow) tablaExtraordinarios.getChildAt(i);
                TextView celdaMateria = (TextView) fila.getChildAt(0);

                String materiaNombre = celdaMateria.getText().toString();
                List<String> lineasMateriaExtra = dividirTexto(materiaNombre, 30);
                for (int k = 0; k < lineasMateriaExtra.size(); k++) {
                    canvas.drawText(lineasMateriaExtra.get(k), x, y + (k * 12), paint);
                }

                for (int j = 1; j < fila.getChildCount(); j++) {
                    TextView celda = (TextView) fila.getChildAt(j);
                    canvas.drawText(celda.getText().toString(), x + colMateriaWidth + (j - 1) * colOthersWidth, y, paint);
                }

                y += rowSpacing + (lineasMateriaExtra.size() - 1) * 12;
                if (y > 800) {
                    documento.finishPage(page);
                    page = documento.startPage(pageInfo);
                    canvas = page.getCanvas();
                    y = 25;
                }
            }
        }

        documento.finishPage(page);

        // 📥 Guardado con nombre personalizado
        String nombreArchivo = "calificaciones_" + alumnoActual.getMatricula() + ".pdf";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Downloads.DISPLAY_NAME, nombreArchivo);
            values.put(MediaStore.Downloads.MIME_TYPE, "application/pdf");
            values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

            ContentResolver resolver = requireContext().getContentResolver();
            Uri uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);

            try (OutputStream output = resolver.openOutputStream(uri)) {
                documento.writeTo(output);
                Toast.makeText(requireContext(), "📥 PDF guardado en Descargas", Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(requireContext(), "❌ Error al guardar PDF", Toast.LENGTH_LONG).show();
            }

            documento.close();

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/pdf");
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            try {
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(requireContext(), "No hay visor de PDF instalado", Toast.LENGTH_LONG).show();
            }

        } else {
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), nombreArchivo);
            try (FileOutputStream fos = new FileOutputStream(file)) {
                documento.writeTo(fos);
                Toast.makeText(requireContext(), "📥 PDF guardado en Descargas", Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(requireContext(), "❌ Error al guardar PDF", Toast.LENGTH_LONG).show();
            }

            documento.close();

            Uri uri = FileProvider.getUriForFile(
                    requireContext(),
                    requireContext().getPackageName() + ".provider",
                    file
            );

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/pdf");
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            try {
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(requireContext(), "No hay visor de PDF instalado", Toast.LENGTH_LONG).show();
            }
        }
    }


    private List<String> dividirTexto(String texto, int maxLongitud) {
        List<String> lineas = new ArrayList<>();
        String[] palabras = texto.split(" ");
        StringBuilder lineaActual = new StringBuilder();

        for (String palabra : palabras) {
            if (lineaActual.length() + palabra.length() + 1 > maxLongitud) {
                lineas.add(lineaActual.toString().trim());
                lineaActual = new StringBuilder();
            }
            lineaActual.append(palabra).append(" ");
        }

        if (!lineaActual.toString().isEmpty()) {
            lineas.add(lineaActual.toString().trim());
        }

        // Limita a máximo 3 líneas
        while (lineas.size() > 3) {
            String nuevaLinea = lineas.get(2) + " " + lineas.remove(3);
            lineas.set(2, nuevaLinea);
        }

        return lineas;
    }





    private boolean tieneAnterior() {
        return idxCicloActual > 1;
    }

    private boolean tieneSiguiente() {
        int maxIdx = semestresMapa.keySet().stream().max(Integer::compareTo).orElse(1);
        return idxCicloActual < maxIdx;
    }

}
