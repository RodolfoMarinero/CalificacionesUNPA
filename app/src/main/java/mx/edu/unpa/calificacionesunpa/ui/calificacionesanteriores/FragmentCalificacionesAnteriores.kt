package mx.edu.unpa.calificacionesunpa.ui.calificacionesanteriores

//import mx.edu.unpa.calificacionesunpa.service.ProfilePictureService.Companion.getInstance
//import com.google.firebase.auth.FirebaseAuth
import android.content.ActivityNotFoundException
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfDocument.PageInfo
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import dagger.hilt.android.AndroidEntryPoint
import mx.edu.unpa.calificacionesunpa.R
import mx.edu.unpa.calificacionesunpa.data.persistent.CalendariosExamenService
import mx.edu.unpa.calificacionesunpa.data.persistent.UsuarioService
import mx.edu.unpa.calificacionesunpa.models.Alumno
import mx.edu.unpa.calificacionesunpa.models.Materia
import mx.edu.unpa.calificacionesunpa.service.OnBitmapResultCallback
import mx.edu.unpa.calificacionesunpa.service.ProfilePictureService
import mx.edu.unpa.calificacionesunpa.ui.dd.SelectorSemestre
import mx.edu.unpa.calificacionesunpa.ui.perfil.FragmentPerfilN
import mx.edu.unpa.calificacionesunpa.utils.PromedioCalculator
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.Locale
import java.util.function.ToIntFunction

@AndroidEntryPoint
class FragmentCalificacionesAnteriores : Fragment() {
    private var txtMatricula: TextView? = null
    private var tablaCalificaciones: TableLayout? = null
    private var tablaExtraordinarios: TableLayout? = null
    private var txtPromedioGeneral: TextView? = null
    private val tvTipoCalificacion: TextView? = null
    private var tvExtraordinariosLabel: TextView? = null
    private var tvNombre: TextView? = null
    private var usuarioService: UsuarioService? = null
    private var todasMaterias: MutableList<Materia>? = ArrayList<Materia>()
    private var ivPerfil: ImageView? = null
    private var alumnoActual: Alumno? = null

    private var nombre: String? = null
    private var carrera: String? = null

    private var contenedorSpinner: RecyclerView? = null
    private var containerSpinner: FrameLayout? = null
    private var sombra: View? = null
    private var semestresMapa: MutableMap<Int?, String?>? = null

    private var btnAnterior: MaterialButton? = null
    private var btnSiguiente: MaterialButton? = null
    private var btnSemestreActual: MaterialButton? = null
    private var idxCicloActual = 1 // Índice del ciclo actual, empieza en 1
    private var promedioCalculatorService: PromedioCalculator? = null
    private var profilePictureService: ProfilePictureService? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_calificaciones_anteriores, container, false)

        // 1) Referencias UI
        txtMatricula = root.findViewById<TextView?>(R.id.txtMatricula)
        tablaCalificaciones = root.findViewById<TableLayout?>(R.id.tablaCalificaciones)
        tablaExtraordinarios = root.findViewById<TableLayout?>(R.id.tablaExtraordinarios)
        txtPromedioGeneral = root.findViewById<TextView?>(R.id.txtPromedioGeneral)
        tvExtraordinariosLabel = root.findViewById<TextView?>(R.id.tvExtraordinariosLabel)
        tvNombre = root.findViewById<TextView?>(R.id.tvNombre)
        val btnPdf = root.findViewById<Button?>(R.id.btnDescargarPdf)
        btnPdf.setOnClickListener(View.OnClickListener { view: View? -> this.generarPdf(view) })




        ivPerfil = root.findViewById<ImageView?>(R.id.ivPerfil)

        ivPerfil!!.setOnClickListener(View.OnClickListener { v: View? ->
            val fragment = FragmentPerfilN()
            requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(
                    R.id.nav_host_fragment_content_main,
                    fragment
                ) // Usa el contenedor correcto
                .addToBackStack(null)
                .commit()
        })



        //profilePictureService = getInstance(requireContext(),StorageRepository())
        loadProfileImage()

        txtPromedioGeneral!!.setVisibility(View.GONE)

        // 2) Inicializar providers
        usuarioService = UsuarioService
        promedioCalculatorService = PromedioCalculator

        // 4) Traer alumno básico

        alumnoActual = usuarioService?.alumnoActual

        // Protegemos la app si el alumno es nulo
        if (alumnoActual == null) {
            Toast.makeText(requireContext(), "Error: Datos de alumno no cargados", Toast.LENGTH_SHORT).show()
            return root
        }

        // Protegemos la app si las materias son nulas
        todasMaterias = alumnoActual?.materias?.toMutableList() ?: mutableListOf()

        if (todasMaterias.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "No hay materias registradas", Toast.LENGTH_SHORT).show()
        } else {
            Log.e("CalificacionesAnterioresAriel", "Lista Materias " + todasMaterias)
            for (m in todasMaterias!!) {
                Log.d("CalificacionesAnterioresAriel", "Materia " + m.materia)
            }
            promedioCalculatorService?.calcularPromedioGeneral(todasMaterias!!)
        }

        txtMatricula?.setText(alumnoActual?.matricula ?: "Sin matrícula")
        alumnoActual = usuarioService!!.alumnoActual

// --- AGREGA ESTOS LOGS ---
        Log.e("DEBUG_PERFIL", "Alumno completo: $alumnoActual")
        Log.e("DEBUG_PERFIL", "Nombre: ${alumnoActual?.nombre}")
        Log.e("DEBUG_PERFIL", "Paterno: ${alumnoActual?.apPaterno}")
// -------------------------

        nombre = alumnoActual!!.nombre + " " + alumnoActual!!.apPaterno + " " + alumnoActual!!.apMaterno
        tvNombre!!.setText(nombre)

// --- AGREGA ESTO PARA PROBAR ---
// Si el nombre viene vacío o nulo, pon un texto de prueba para descartar error de diseño
        if (nombre?.trim()?.isEmpty() == true || nombre?.contains("null") == true) {
            tvNombre!!.text = "TEXTO DE PRUEBA (ROJO)"
            tvNombre!!.setTextColor(android.graphics.Color.RED)}


        btnAnterior = root.findViewById<MaterialButton?>(R.id.btnIzquierdo)
        btnSiguiente = root.findViewById<MaterialButton?>(R.id.btnDerecho)
        btnSemestreActual = root.findViewById<MaterialButton?>(R.id.btnSemestre)

        contenedorSpinner = root.findViewById<RecyclerView?>(R.id.rvSemestres)
        contenedorSpinner!!.setVisibility(View.VISIBLE)
        containerSpinner = root.findViewById<FrameLayout?>(R.id.contenedorSpinner)
        sombra = root.findViewById<View?>(R.id.blurOverlaySpinner)
        sombra!!.setOnClickListener(View.OnClickListener { v: View? ->
            ocultarSpinnerSiVisible()
        })
        //Observa ciclo actual
        usuarioService!!.semestreSeleccionado.observe(
            getViewLifecycleOwner(),
            Observer { semestre: Int? ->
                if (semestre != null) {
                    Log.e("CalificacionesAnterioresAriel", "Observado cambio de semestre ")
                    idxCicloActual = semestre
                    loadGradesForCycle()
                    ocultarSpinnerSiVisible()
                }
            })

        //crear mapa de semestres y seleccionar el semestre actual
        setupSemestreSelector()
        val ultimoCiclo = semestresMapa!!.keys.stream().max(
            Comparator.comparingInt<Int?>(
                ToIntFunction { a: Int? -> a!! })
        ).orElse(0)!!
        usuarioService!!.seleccionarSemestre(ultimoCiclo)
        btnSemestreActual!!.setText(semestresMapa!!.get(ultimoCiclo))
        // 5) Listener para mostrar/ocultar el spinner
        btnSemestreActual!!.setOnClickListener(View.OnClickListener { v: View? ->
            llamarFragmento()
        })
        //Listeners para los botones de navegación
        btnAnterior!!.setOnClickListener(View.OnClickListener { v: View? ->
            if (tieneAnterior()) {
                usuarioService!!.seleccionarSemestre(idxCicloActual - 1)
            }
        })
        btnSiguiente!!.setOnClickListener(View.OnClickListener { v: View? ->
            if (tieneSiguiente()) {
                usuarioService!!.seleccionarSemestre(idxCicloActual + 1)
            }
        })
        ocultarSpinnerSiVisible()
        return root
    }


    override fun onResume() {
        super.onResume()
        /*
        if (profilePictureService!!.shouldRefreshProfile()) {
            loadProfileImage()
        }*/
    }


    private fun loadProfileImage() {
       // val currentUser = FirebaseAuth.getInstance().getCurrentUser()
        val currentUser = null
        if (currentUser == null) {
            ivPerfil!!.setImageResource(R.drawable.ic_perfil)
            return
        }

       // val userId = currentUser.getUid()
        val userId = "1"

        profilePictureService!!.getProfilePicture(userId, object : OnBitmapResultCallback {
            override fun onResult(bitmap: Bitmap?) {
                requireActivity().runOnUiThread(Runnable {
                    if (bitmap != null) {
                        ivPerfil!!.setImageBitmap(bitmap)
                    } else {
                        ivPerfil!!.setImageResource(R.drawable.ic_perfil)
                    }
                })
            }
        })
    }


//    private fun setupSemestreSelector() {
//        val ciclosUnicos: MutableSet<String> = LinkedHashSet<String>()
//        Log.e("CalificacionesAnterioresAriel", "cargando ciclos de las materias ")
//        for (m in todasMaterias!!) {
//            if (m.ciclo != null) {
//                Log.e("CalificacionesAnterioresAriel", "cargando ciclos de las materias "+ m.ciclo)
//                ciclosUnicos.add(m.ciclo!!)
//            }
//        }
//
//        // Extraer solo los IDs de los ciclos (último segmento del path)
//        val semestresMap: MutableMap<Int?, String?> = LinkedHashMap<Int?, String?>()
//        var contador = 1
//        for (ciclo in ciclosUnicos) {
//            semestresMap.put(contador++, ciclo) // usar número como clave, cicloId como valor
//           // val path = ref.getPath()
//            //val partes = path.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
//            /*if (partes.size > 0) {
//                val cicloId: String? = partes[partes.size - 1]
//                semestresMap.put(contador++, cicloId) // usar número como clave, cicloId como valor
//            }*/
//        }
//        semestresMapa = semestresMap
//    }

    private fun setupSemestreSelector() {
        // 1. Extraemos todos los nombres de ciclos que no sean nulos
        val ciclosRaw = todasMaterias!!
            .mapNotNull { it.ciclo } // Filtra nulos automáticamente
            .distinct() // Quita repetidos

        // 2. IMPORTANTE: Los ordenamos alfabéticamente.
        // Al ser strings tipo "2023-2024-A", el orden natural es cronológico (Ascendente)
        // Quedarán así: [2021-2022-A, 2021-2022-B, ... , 2024-2025-A]
        val ciclosOrdenados = ciclosRaw.sorted()

        // 3. Llenar el mapa
        val semestresMap: MutableMap<Int?, String?> = LinkedHashMap()
        var contador = 1

        for (ciclo in ciclosOrdenados) {
            // Asignamos ID 1 al más viejo, ID N al más nuevo
            semestresMap[contador++] = ciclo
        }
        semestresMapa = semestresMap

        // DEBUG: Ver en consola cómo quedaron ordenados
        Log.d("ORDEN_CICLOS", "Ciclos ordenados: $semestresMap")
    }

    private fun llamarFragmento() {
        sombra!!.setVisibility(View.VISIBLE)
        val fm = getParentFragmentManager()
        val tag = "SelectorSemestresTag"

        val existing = fm.findFragmentByTag(tag)

        // Si ya existe uno, elimínalo antes de añadir uno nuevo
        if (existing != null) {
            fm.beginTransaction().remove(existing).commitNow()
        }

        val fragmento: Fragment = SelectorSemestre.newInstance(1, semestresMapa as Map<Int, String>)
        fm.beginTransaction()
            .replace(R.id.contenedorSpinner, fragmento, tag)
            .commit()

        containerSpinner!!.setVisibility(View.VISIBLE)
    }

    private fun ocultarSpinnerSiVisible() {
        val isVisible = containerSpinner!!.getVisibility() == View.VISIBLE
        if (isVisible) {
            sombra!!.setVisibility(View.GONE)
            containerSpinner!!.setVisibility(View.GONE)
        }
    }

    private fun loadGradesForCycle() {
        tablaCalificaciones!!.removeAllViews()
        agregarEncabezado(
            tablaCalificaciones!!,
            arrayOf<String>("Materia", "1er.", "2o.", "3er.", "P.P", "E.F", "CAL.DEF") as Array<String?>,
            true
        )
        agregarEncabezado(
            tablaExtraordinarios!!,
            arrayOf<String>("Materia", "E1", "E2", "ESP") as Array<String?>,
            true
        )
        var cicloEscolar = semestresMapa!!.get(idxCicloActual)
        Log.d("FragmentCalificacionesAnterioresAriel", "loadGradesForCycle ciclo=" + cicloEscolar + "  idxCicloActual:" + idxCicloActual)
        btnSemestreActual!!.setText(cicloEscolar)
        //cicloEscolar = "ciclosEscolares/" + cicloEscolar
        // Limpia las tablas
        if (tablaCalificaciones!!.getChildCount() > 1) tablaCalificaciones!!.removeViews(
            1,
            tablaCalificaciones!!.getChildCount() - 1
        )
        if (tablaExtraordinarios!!.getChildCount() > 1) tablaExtraordinarios!!.removeViews(
            1,
            tablaExtraordinarios!!.getChildCount() - 1
        )

        tablaExtraordinarios!!.setVisibility(View.GONE)
        tvExtraordinariosLabel!!.setVisibility(View.GONE)
        val filtradas: MutableList<Materia> = ArrayList<Materia>()
        CalendariosExamenService.calendariosExamen.clear()
        for (m in todasMaterias!!) {
            m.ciclo?.let { ciclo ->
                if (cicloEscolar?.compareTo(ciclo) == 0) {
                    filtradas.add(m)
                    if(m.calendarioExamenes!=null) {
                        Log.d("Ariel  ", "Agrega Calendario " + m.calendarioExamenes!!.materia )
                        CalendariosExamenService.calendariosExamen.add(m.calendarioExamenes!!)
                    }else{
                        Log.d("Ariel  ", "No hay Calendario")
                    }
                }
            }
        }

        Log.d("Ariel", "Materias filtradas por ciclo: " + filtradas.size)
        if (filtradas.isEmpty()) {
            Toast.makeText(
                requireContext(),
                "No hay materias para este ciclo escolar",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        val hasExtra = booleanArrayOf(false)
        for (mat in filtradas) {


            Log.d(TAG, "Materia: " + mat.materia + ", calificaciones: " + mat.calificaciones)

            if (alumnoActual!!.esRegular) {
                val row = TableRow(requireContext())
                row.setGravity(Gravity.CENTER)

                addCell(row, mat.materia, true)
                addCell(row, format(mat.calificaciones.parcial1), false)
                addCell(row, format(mat.calificaciones.parcial2), false)
                addCell(row, format(mat.calificaciones.parcial3), false)
                val promedioParcial = mat.getPromedioParciales()
                val textoPromedio = if (promedioParcial == 0.0) "-" else format(promedioParcial)
                addCell(row, textoPromedio, false)
                addCell(row, format(mat.calificaciones.ordinario), false)
                addCell(row, format(mat.calificaciones.pFinal), false)

                tablaCalificaciones!!.addView(row)
            }

            // Extraordinarios
            val tieneExtra =
                (mat.calificaciones.extra1 != null || mat.calificaciones.extra2 != null || mat.calificaciones.especial != null
                        )
            if (tieneExtra) {
                tablaExtraordinarios!!.setVisibility(View.VISIBLE)
                tvExtraordinariosLabel!!.setVisibility(View.VISIBLE)
                hasExtra[0] = true
                val rowEx = TableRow(requireContext())
                rowEx.setGravity(Gravity.START)

                addCell(rowEx, mat.materia, true)
                addCell(rowEx, format(mat.calificaciones.extra1), false)
                addCell(rowEx, format(mat.calificaciones.extra2), false)
                addCell(rowEx, format(mat.calificaciones.especial), false)

                tablaExtraordinarios!!.addView(rowEx)
            }
        }
    }

    private fun agregarEncabezado(
        tabla: TableLayout,
        titulos: Array<String?>,
        alinearPrimeraIzquierda: Boolean
    ) {
        val headerRow = TableRow(requireContext())
        headerRow.setGravity(Gravity.CENTER)

        for (i in titulos.indices) {
            val esMateria = alinearPrimeraIzquierda && i == 0
            addCell(headerRow, titulos[i]!!, esMateria)
        }

        tabla.addView(headerRow)
    }


    private fun addCell(row: TableRow, texto: String, esMateria: Boolean) {
        val tv = TextView(getContext())
        tv.setTextColor(android.graphics.Color.BLACK)
        tv.setPadding(8, 8, 8, 8)
        tv.setEllipsize(TextUtils.TruncateAt.END)

        if (esMateria) {
            // Calcula el tamaño de texto en SP a partir de pixels
            val textSizePx = tv.getTextSize()
            val scaledDensity = requireContext().getResources().getDisplayMetrics().scaledDensity
            val textSizeSp = textSizePx / scaledDensity

            // Ajusta el texto a dos líneas si supera el 60% del ancho de pantalla
            val ajustado = mx.edu.unpa.calificacionesunpa.utils.TextUtils.splitTextIfTooLong(
                requireContext(),
                texto,
                textSizeSp,
                60f // umbral en porcentaje
            )
            tv.setText(ajustado)
            tv.setLines(2)
            tv.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START)
            tv.setGravity(Gravity.START or Gravity.CENTER_VERTICAL)
        } else {
            tv.setText(texto)
            tv.setMaxLines(1)
            tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER)
            tv.setGravity(Gravity.CENTER)
        }

        val lp = TableRow.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        tv.setLayoutParams(lp)
        row.addView(tv)
    }


    private fun format(v: Double?): String {
        if (v == null) return "-"

        return when (v) {
            11.0 -> "NP"
            12.0 -> "SD"
            else -> String.format(Locale.getDefault(), "%.1f", v)
        }
    }

    private fun tieneAnterior(): Boolean {
        return idxCicloActual > 1
    }

    private fun tieneSiguiente(): Boolean {
        val maxIdx =
            semestresMapa!!.keys.stream().max(Comparator { obj: Int?, anotherInteger: Int? ->
                obj!!.compareTo(
                    anotherInteger!!
                )
            }).orElse(1)!!
        return idxCicloActual < maxIdx
    }


    fun generarPdf(view: View?) {
        val documento = PdfDocument()
        val paint = Paint()

        val pageInfo = PageInfo.Builder(595, 842, 1).create()
        var page = documento.startPage(pageInfo)
        var canvas = page.getCanvas()

        val x = 10
        var y = 25
        val rowSpacing = 20

        val colMateriaWidth = 200
        val colOthersWidth = 60

        // 🧑 Datos del alumno
        paint.setTextSize(14f)
        paint.setFakeBoldText(true)
        canvas.drawText("Reporte de Calificaciones", x.toFloat(), y.toFloat(), paint)
        y += rowSpacing

        paint.setFakeBoldText(false)
        canvas.drawText("Nombre: " + nombre, x.toFloat(), y.toFloat(), paint)
        y += rowSpacing
        canvas.drawText("Matrícula: " + alumnoActual!!.matricula, x.toFloat(), y.toFloat(), paint)
        y += rowSpacing
        canvas.drawText("Carrera: " + alumnoActual!!.nombreCarrera, x.toFloat(), y.toFloat(), paint)
        y += rowSpacing

        if (txtPromedioGeneral!!.getVisibility() == View.VISIBLE && !txtPromedioGeneral!!.getText()
                .toString().isEmpty()
        ) {
            canvas.drawText(
                txtPromedioGeneral!!.getText().toString(),
                x.toFloat(),
                y.toFloat(),
                paint
            )
            y += rowSpacing
        }

        y += 10

        // 🧾 Encabezados tabla principal
        paint.setTextSize(12f)
        paint.setFakeBoldText(true)
        val headers = arrayOf<String?>("Materia", "1er.", "2o.", "3er.", "P.P", "E.F", "CAL.DEF")
        canvas.drawText(headers[0]!!, x.toFloat(), y.toFloat(), paint)
        for (j in 1..<headers.size) {
            canvas.drawText(
                headers[j]!!,
                (x + colMateriaWidth + (j - 1) * colOthersWidth).toFloat(),
                y.toFloat(),
                paint
            )
        }
        y += rowSpacing

        paint.setFakeBoldText(false)

        // 📋 Filas calificaciones normales
        for (i in 1..<tablaCalificaciones!!.getChildCount()) {
            val fila = tablaCalificaciones!!.getChildAt(i) as TableRow
            val celdaMateria = fila.getChildAt(0) as TextView

            val materiaNombre = celdaMateria.getText().toString()
            val lineasMateria = dividirTexto(materiaNombre, 30)
            for (k in lineasMateria.indices) {
                canvas.drawText(
                    lineasMateria.get(k)!!,
                    x.toFloat(),
                    (y + (k * 12)).toFloat(),
                    paint
                )
            }

            for (j in 1..<fila.getChildCount()) {
                val celda = fila.getChildAt(j) as TextView
                canvas.drawText(
                    celda.getText().toString(),
                    (x + colMateriaWidth + (j - 1) * colOthersWidth).toFloat(),
                    y.toFloat(),
                    paint
                )
            }

            y += rowSpacing + (lineasMateria.size - 1) * 12
            if (y > 800) {
                documento.finishPage(page)
                page = documento.startPage(pageInfo)
                canvas = page.getCanvas()
                y = 25
            }
        }

        // 📌 Tabla de extraordinarios
        if (tablaExtraordinarios!!.getVisibility() == View.VISIBLE && tablaExtraordinarios!!.getChildCount() > 1) {
            y += 30
            paint.setFakeBoldText(true)
            canvas.drawText("Calificaciones Extraordinarias", x.toFloat(), y.toFloat(), paint)
            y += rowSpacing

            paint.setFakeBoldText(false)
            val headersExtra = arrayOf<String?>("Materia", "1er", "2o", "E.E")
            canvas.drawText(headersExtra[0]!!, x.toFloat(), y.toFloat(), paint)
            for (j in 1..<headersExtra.size) {
                canvas.drawText(
                    headersExtra[j]!!,
                    (x + colMateriaWidth + (j - 1) * colOthersWidth).toFloat(),
                    y.toFloat(),
                    paint
                )
            }
            y += rowSpacing

            for (i in 1..<tablaExtraordinarios!!.getChildCount()) {
                val fila = tablaExtraordinarios!!.getChildAt(i) as TableRow
                val celdaMateria = fila.getChildAt(0) as TextView

                val materiaNombre = celdaMateria.getText().toString()
                val lineasMateriaExtra = dividirTexto(materiaNombre, 30)
                for (k in lineasMateriaExtra.indices) {
                    canvas.drawText(
                        lineasMateriaExtra.get(k)!!,
                        x.toFloat(),
                        (y + (k * 12)).toFloat(),
                        paint
                    )
                }

                for (j in 1..<fila.getChildCount()) {
                    val celda = fila.getChildAt(j) as TextView
                    canvas.drawText(
                        celda.getText().toString(),
                        (x + colMateriaWidth + (j - 1) * colOthersWidth).toFloat(),
                        y.toFloat(),
                        paint
                    )
                }

                y += rowSpacing + (lineasMateriaExtra.size - 1) * 12
                if (y > 800) {
                    documento.finishPage(page)
                    page = documento.startPage(pageInfo)
                    canvas = page.getCanvas()
                    y = 25
                }
            }
        }

        documento.finishPage(page)

        // 📥 Guardado con nombre personalizado
        val nombreArchivo = "calificaciones_" + alumnoActual!!.matricula + ".pdf"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues()
            values.put(MediaStore.Downloads.DISPLAY_NAME, nombreArchivo)
            values.put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
            values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)

            val resolver = requireContext().getContentResolver()
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)

            try {
                resolver.openOutputStream(uri!!).use { output ->
                    documento.writeTo(output)
                    Toast.makeText(
                        requireContext(),
                        "PDF guardado en Descargas",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Error al guardar PDF", Toast.LENGTH_LONG).show()
            }

            documento.close()

            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(uri, "application/pdf")
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            try {
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(requireContext(), "No hay visor de PDF instalado", Toast.LENGTH_LONG)
                    .show()
            }
        } else {
            val file = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                nombreArchivo
            )
            try {
                FileOutputStream(file).use { fos ->
                    documento.writeTo(fos)
                    Toast.makeText(requireContext(), "PDF guardado en Descargas", Toast.LENGTH_LONG)
                        .show()
                }
            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Error al guardar PDF", Toast.LENGTH_LONG).show()
            }

            documento.close()

            val uri = FileProvider.getUriForFile(
                requireContext(),
                requireContext().getPackageName() + ".provider",
                file
            )

            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(uri, "application/pdf")
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            try {
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(requireContext(), "No hay visor de PDF instalado", Toast.LENGTH_LONG)
                    .show()
            }
        }
    }


    private fun dividirTexto(texto: String, maxLongitud: Int): MutableList<String?> {
        val lineas: MutableList<String?> = ArrayList<String?>()
        val palabras = texto.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        var lineaActual = StringBuilder()

        for (palabra in palabras) {
            if (lineaActual.length + palabra.length + 1 > maxLongitud) {
                lineas.add(lineaActual.toString().trim { it <= ' ' })
                lineaActual = StringBuilder()
            }
            lineaActual.append(palabra).append(" ")
        }

        if (!lineaActual.toString().isEmpty()) {
            lineas.add(lineaActual.toString().trim { it <= ' ' })
        }

        // Limita a máximo 3 líneas
        while (lineas.size > 3) {
            val nuevaLinea = lineas.get(2) + " " + lineas.removeAt(3)
            lineas.set(2, nuevaLinea)
        }

        return lineas
    }


    companion object {
        private const val TAG = "CalifFrag"
    }
}