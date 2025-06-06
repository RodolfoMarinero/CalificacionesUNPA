package mx.edu.unpa.calificacionesunpa.ui.calificacionesanteriores;

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
import com.google.firebase.auth.FirebaseAuth;
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
    private TextView tvNombre;
    private UsuarioService usuarioService;
    private List<Materia> todasMaterias = new ArrayList<>();
    private ImageView ivPerfil;
    private Alumno alumnoActual;

    private String nombre,carrera;

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
        tvNombre             = root.findViewById(R.id.tvNombre);
        Button btnPdf = root.findViewById(R.id.btnDescargarPdf);
        btnPdf.setOnClickListener(this::generarPdf);


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

        if (alumnoActual == null) {
            Log.e(TAG, "❌ alumnoActual es null");
            Toast.makeText(requireContext(), "No se encontró información del alumno.", Toast.LENGTH_LONG).show();
            return new FrameLayout(requireContext()); // evitar inflar vista rota
        }

        todasMaterias = alumnoActual.getMaterias();

        txtMatricula.setText(alumnoActual.getMatricula());
        nombre=alumnoActual.getNombre()+" "+alumnoActual.getApPaterno()+" "+alumnoActual.getApMaterno();
        carrera=alumnoActual.getNombreCarrera();
        tvNombre.setText(nombre);
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





}
