package mx.edu.unpa.calificacionesunpa.ui.calificacionesanteriores

import android.content.ContentValues
import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.hilt.android.AndroidEntryPoint
import mx.edu.unpa.calificacionesunpa.R
import mx.edu.unpa.calificacionesunpa.data.persistent.UsuarioService
import mx.edu.unpa.calificacionesunpa.models.Alumno
import mx.edu.unpa.calificacionesunpa.models.Materia
import mx.edu.unpa.calificacionesunpa.ui.dd.SelectorSemestre
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

@AndroidEntryPoint
class FragmentCalificacionesAnteriores : Fragment() {

    private lateinit var rvCalificaciones: RecyclerView
    private lateinit var btnAnterior: View
    private lateinit var btnSiguiente: View
    private lateinit var tvSemestreActual: TextView
    private lateinit var btnDescargarPdf: FloatingActionButton

    // Variables del Spinner
    private var contenedorSpinner: RecyclerView? = null
    private var containerSpinner: FrameLayout? = null
    private var sombra: View? = null

    private lateinit var adaptador: CalificacionesAdapter
    private var todasMaterias = mutableListOf<Materia>()
    private var materiasActuales = listOf<Materia>() // Para el PDF
    private var semestresMapa = mutableMapOf<Int, String>()
    private var idxCicloActual = 1

    private val usuarioService = UsuarioService
    private var alumnoActual: Alumno? = null
    private var nombreCompleto: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_calificaciones_anteriores, container, false)

        // 1. Enlaces UI Base
        rvCalificaciones = root.findViewById(R.id.rvCalificaciones)
        btnAnterior = root.findViewById(R.id.btnIzquierdo)
        btnSiguiente = root.findViewById(R.id.btnDerecho)
        tvSemestreActual = root.findViewById(R.id.btnSemestre)
        btnDescargarPdf = root.findViewById(R.id.btnDescargarPdf)

        // 2. Enlaces UI del Spinner
        contenedorSpinner = root.findViewById(R.id.rvSemestres)
        containerSpinner = root.findViewById(R.id.contenedorSpinner)
        sombra = root.findViewById(R.id.blurOverlaySpinner)

        adaptador = CalificacionesAdapter(emptyList())
        rvCalificaciones.adapter = adaptador

        // 3. Datos del alumno
        alumnoActual = usuarioService.alumnoActual
        if (alumnoActual == null) {
            Toast.makeText(requireContext(), "Error: Datos de alumno no cargados", Toast.LENGTH_SHORT).show()
            return root
        }
        todasMaterias = alumnoActual?.materias?.toMutableList() ?: mutableListOf()
        nombreCompleto = "${alumnoActual!!.nombre} ${alumnoActual!!.apPaterno} ${alumnoActual!!.apMaterno}"

        // 4. Configurar semestres
        setupSemestreSelector()
        val ultimoCiclo = semestresMapa.keys.maxOrNull() ?: 0
        usuarioService.seleccionarSemestre(ultimoCiclo)

        // 5. Observar cambios de semestre
        usuarioService.semestreSeleccionado.observe(viewLifecycleOwner) { semestre ->
            if (semestre != null) {
                idxCicloActual = semestre
                loadGradesForCycle()
                ocultarSpinnerSiVisible()
            }
        }

        // 6. Listeners de navegación y Spinner
        btnAnterior.setOnClickListener { if (idxCicloActual > 1) usuarioService.seleccionarSemestre(idxCicloActual - 1) }
        btnSiguiente.setOnClickListener { if (idxCicloActual < semestresMapa.size) usuarioService.seleccionarSemestre(idxCicloActual + 1) }

        tvSemestreActual.setOnClickListener { llamarFragmento() }
        sombra?.setOnClickListener { ocultarSpinnerSiVisible() }

        // 6. Listener PDF
        btnDescargarPdf.setOnClickListener { generarPdf() }

        ocultarSpinnerSiVisible()
        return root
    }

    private fun setupSemestreSelector() {
        val ciclosOrdenados = todasMaterias.mapNotNull { it.ciclo }.distinct().sorted()
        semestresMapa.clear()
        var contador = 1
        for (ciclo in ciclosOrdenados) semestresMapa[contador++] = ciclo
    }

    private fun loadGradesForCycle() {
        val cicloEscolar = semestresMapa[idxCicloActual] ?: return
        materiasActuales = todasMaterias.filter { it.ciclo == cicloEscolar } // Guardamos para el PDF

        if (materiasActuales.isEmpty()) {
            Toast.makeText(requireContext(), "No hay materias para este ciclo", Toast.LENGTH_SHORT).show()
        }
        adaptador.actualizarDatos(materiasActuales)
    }

    // --- MÉTODOS DEL SPINNER RESTAURADOS ---
    private fun llamarFragmento() {
        sombra?.visibility = View.VISIBLE
        containerSpinner?.visibility = View.VISIBLE
        val fm = parentFragmentManager
        val existing = fm.findFragmentByTag("SelectorSemestresTag")
        if (existing != null) fm.beginTransaction().remove(existing).commitNow()

        val fragmento = SelectorSemestre.newInstance(1, semestresMapa)
        fm.beginTransaction().replace(R.id.contenedorSpinner, fragmento, "SelectorSemestresTag").commit()
    }

    private fun ocultarSpinnerSiVisible() {
        if (containerSpinner?.visibility == View.VISIBLE) {
            sombra?.visibility = View.GONE
            containerSpinner?.visibility = View.GONE
        }
    }

    // --- MÉTODO PDF RESTAURADO (Leyendo de la lista en lugar de la vista) ---
    private fun generarPdf() {
        if (alumnoActual == null || materiasActuales.isEmpty()) {
            Toast.makeText(requireContext(), "No hay datos para generar el PDF", Toast.LENGTH_SHORT).show()
            return
        }

        val documento = PdfDocument()
        val paint = Paint()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        var page = documento.startPage(pageInfo)
        var canvas = page.canvas

        val x = 10f
        var y = 25f
        val rowSpacing = 20f
        val colMateriaWidth = 200f
        val colOthersWidth = 60f

        // Cabecera
        paint.textSize = 14f
        paint.isFakeBoldText = true
        canvas.drawText("Reporte de Calificaciones - ${semestresMapa[idxCicloActual]}", x, y, paint)
        y += rowSpacing

        paint.isFakeBoldText = false
        canvas.drawText("Nombre: $nombreCompleto", x, y, paint)
        y += rowSpacing
        canvas.drawText("Matrícula: ${alumnoActual!!.matricula}", x, y, paint)
        y += rowSpacing
        canvas.drawText("Carrera: ${alumnoActual!!.nombreCarrera}", x, y, paint)
        y += rowSpacing + 10f

        // Encabezados Tabla
        paint.textSize = 12f
        paint.isFakeBoldText = true
        val headers = arrayOf("Materia", "1er.", "2o.", "3er.", "P.P", "E.F", "CAL.DEF")
        canvas.drawText(headers[0], x, y, paint)
        for (j in 1 until headers.size) {
            canvas.drawText(headers[j], x + colMateriaWidth + (j - 1) * colOthersWidth, y, paint)
        }
        y += rowSpacing
        paint.isFakeBoldText = false

        // Filas Ordinarias
        for (materia in materiasActuales) {
            val c = materia.calificaciones
            val lineasMateria = dividirTexto(materia.materia, 30)

            for (k in lineasMateria.indices) {
                canvas.drawText(lineasMateria[k], x, y + (k * 12f), paint)
            }

            canvas.drawText(format(c.parcial1), x + colMateriaWidth, y, paint)
            canvas.drawText(format(c.parcial2), x + colMateriaWidth + colOthersWidth, y, paint)
            canvas.drawText(format(c.parcial3), x + colMateriaWidth + colOthersWidth * 2, y, paint)
            canvas.drawText(format(materia.getPromedioParciales()), x + colMateriaWidth + colOthersWidth * 3, y, paint)
            canvas.drawText(format(c.ordinario), x + colMateriaWidth + colOthersWidth * 4, y, paint)
            canvas.drawText(format(c.pFinal), x + colMateriaWidth + colOthersWidth * 5, y, paint)

            y += rowSpacing + (lineasMateria.size - 1) * 12f
            if (y > 800) {
                documento.finishPage(page)
                page = documento.startPage(pageInfo)
                canvas = page.canvas
                y = 25f
            }
        }

        // Tabla Extraordinarios
        val extras = materiasActuales.filter { it.calificaciones.extra1 != null || it.calificaciones.extra2 != null || it.calificaciones.especial != null }
        if (extras.isNotEmpty()) {
            y += 30f
            paint.isFakeBoldText = true
            canvas.drawText("Calificaciones Extraordinarias", x, y, paint)
            y += rowSpacing

            paint.isFakeBoldText = false
            val headersExtra = arrayOf("Materia", "E1", "E2", "ESP")
            canvas.drawText(headersExtra[0], x, y, paint)
            for (j in 1 until headersExtra.size) {
                canvas.drawText(headersExtra[j], x + colMateriaWidth + (j - 1) * colOthersWidth, y, paint)
            }
            y += rowSpacing

            for (materia in extras) {
                val c = materia.calificaciones
                val lineasMateria = dividirTexto(materia.materia, 30)

                for (k in lineasMateria.indices) canvas.drawText(lineasMateria[k], x, y + (k * 12f), paint)

                canvas.drawText(format(c.extra1), x + colMateriaWidth, y, paint)
                canvas.drawText(format(c.extra2), x + colMateriaWidth + colOthersWidth, y, paint)
                canvas.drawText(format(c.especial), x + colMateriaWidth + colOthersWidth * 2, y, paint)

                y += rowSpacing + (lineasMateria.size - 1) * 12f
                if (y > 800) {
                    documento.finishPage(page)
                    page = documento.startPage(pageInfo)
                    canvas = page.canvas
                    y = 25f
                }
            }
        }

        documento.finishPage(page)
        guardarYMostrarPdf(documento) // Tu lógica de guardado
    }

    private fun guardarYMostrarPdf(documento: PdfDocument) {
        val nombreArchivo = "calificaciones_${alumnoActual!!.matricula}.pdf"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, nombreArchivo)
                put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
                put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            val uri = requireContext().contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
            try {
                requireContext().contentResolver.openOutputStream(uri!!)?.use { output -> documento.writeTo(output) }
                Toast.makeText(requireContext(), "PDF guardado en Descargas", Toast.LENGTH_LONG).show()
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/pdf")
                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                }
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error al guardar PDF", Toast.LENGTH_LONG).show()
            }
        } else {
            val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), nombreArchivo)
            try {
                FileOutputStream(file).use { fos -> documento.writeTo(fos) }
                Toast.makeText(requireContext(), "PDF guardado en Descargas", Toast.LENGTH_LONG).show()
                val uri = FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.provider", file)
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/pdf")
                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                }
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error al guardar PDF", Toast.LENGTH_LONG).show()
            }
        }
        documento.close()
    }

    private fun format(v: Double?): String {
        if (v == null || v == 0.0) return "-"
        return when (v) {
            11.0 -> "NP"
            12.0 -> "SD"
            else -> String.format(Locale.getDefault(), "%.1f", v)
        }
    }

    private fun dividirTexto(texto: String, maxLongitud: Int): List<String> {
        val lineas = mutableListOf<String>()
        var lineaActual = StringBuilder()
        for (palabra in texto.split(" ")) {
            if (lineaActual.length + palabra.length + 1 > maxLongitud) {
                lineas.add(lineaActual.toString().trim())
                lineaActual = StringBuilder()
            }
            lineaActual.append(palabra).append(" ")
        }
        if (lineaActual.isNotEmpty()) lineas.add(lineaActual.toString().trim())
        return lineas.take(3) // Máximo 3 líneas
    }
}