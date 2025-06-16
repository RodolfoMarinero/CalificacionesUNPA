package mx.edu.unpa.calificacionesunpa

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
/*import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore*/

class notificaciones_escolares : AppCompatActivity() {
    private lateinit var radioIndividual: RadioButton
    private lateinit var radioCiertos: RadioButton
    private lateinit var radioTodos: RadioButton
    private lateinit var inputMatricula: EditText
    private lateinit var inputTitulo: EditText
    private lateinit var inputMensaje: EditText
    private lateinit var layoutMatricula: LinearLayout
    private lateinit var layoutCiertos: LinearLayout
    private lateinit var spinnerCarrera: Spinner
    private lateinit var spinnerSemestre: Spinner
    private lateinit var spinnerNombres: Spinner
    private lateinit var btnEnviar: Button

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.notificaciones_escolares)

        // Vincular vistas
        radioIndividual = findViewById(R.id.radioIndividual)
        radioCiertos = findViewById(R.id.radioCiertos)
        radioTodos = findViewById(R.id.radioTodos)
        inputMatricula = findViewById(R.id.inputMatricula)
        inputTitulo = findViewById(R.id.inputTitulo)
        inputMensaje = findViewById(R.id.inputMensaje)
        layoutMatricula = findViewById(R.id.layoutMatricula)
        layoutCiertos = findViewById(R.id.layoutCiertos)
        spinnerCarrera = findViewById(R.id.spinnerCarrera)
        spinnerSemestre = findViewById(R.id.spinnerSemestre)
        spinnerNombres = findViewById(R.id.spinnerNombres)
        btnEnviar = findViewById(R.id.btnEnviar)

        // Ocultar campos al inicio
        layoutMatricula.visibility = View.GONE
        layoutCiertos.visibility = View.GONE
        inputTitulo.visibility = View.GONE
        inputMensaje.visibility = View.GONE
        btnEnviar.visibility = View.GONE

        val radioGroup = findViewById<RadioGroup>(R.id.radioGroup)
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radioIndividual -> {
                    layoutMatricula.visibility = View.VISIBLE
                    layoutCiertos.visibility = View.GONE
                    inputTitulo.visibility = View.VISIBLE
                    inputMensaje.visibility = View.VISIBLE
                    btnEnviar.visibility = View.VISIBLE
                }
                R.id.radioCiertos -> {
                    layoutMatricula.visibility = View.GONE
                    layoutCiertos.visibility = View.VISIBLE
                    inputTitulo.visibility = View.VISIBLE
                    inputMensaje.visibility = View.VISIBLE
                    btnEnviar.visibility = View.VISIBLE
                    cargarCarreras(spinnerCarrera)
                    cargarCiclos(spinnerSemestre)
                }
                R.id.radioTodos -> {
                    layoutMatricula.visibility = View.GONE
                    layoutCiertos.visibility = View.GONE
                    inputTitulo.visibility = View.VISIBLE
                    inputMensaje.visibility = View.VISIBLE
                    btnEnviar.visibility = View.VISIBLE
                }
                else -> {
                    layoutMatricula.visibility = View.GONE
                    layoutCiertos.visibility = View.GONE
                    inputTitulo.visibility = View.GONE
                    inputMensaje.visibility = View.GONE
                    btnEnviar.visibility = View.GONE
                }
            }
        }

        // Actualizar lista de alumnos según carrera y semestre
        val actualizarAlumnos = {
            val carrera = spinnerCarrera.selectedItem?.toString()
            val ciclo = spinnerSemestre.selectedItem?.toString()
            if (!carrera.isNullOrEmpty() && !ciclo.isNullOrEmpty()) {
                cargarAlumnos(spinnerNombres, carrera, ciclo)
            }
        }

        spinnerCarrera.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                actualizarAlumnos()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        spinnerSemestre.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                actualizarAlumnos()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        btnEnviar.setOnClickListener {
            val titulo = inputTitulo.text.toString().trim()
            val mensaje = inputMensaje.text.toString().trim()

            if (titulo.isEmpty()) {
                Toast.makeText(this, "Por favor ingresa un título", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (mensaje.isEmpty()) {
                Toast.makeText(this, "Por favor ingresa un mensaje", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            when {
                radioIndividual.isChecked -> {
                    val matricula = inputMatricula.text.toString().trim()
                    if (matricula.isEmpty()) {
                        Toast.makeText(this, "Por favor ingresa la matrícula", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }

                    Toast.makeText(this, "Enviando a matrícula: $matricula\nTítulo: $titulo\nMensaje: $mensaje", Toast.LENGTH_SHORT).show()
                }

                radioCiertos.isChecked -> {
                    if (spinnerNombres.adapter == null || spinnerNombres.adapter.isEmpty) {
                        Toast.makeText(this, "No hay alumnos disponibles", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }

                    val alumno = spinnerNombres.selectedItem?.toString() ?: ""
                    if (alumno.isEmpty()) {
                        Toast.makeText(this, "Selecciona un alumno", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }

                    Toast.makeText(this, "Enviando a: $alumno\nTítulo: $titulo\nMensaje: $mensaje", Toast.LENGTH_SHORT).show()
                }

                radioTodos.isChecked -> {
                    Toast.makeText(this, "Enviando a todos\nTítulo: $titulo\nMensaje: $mensaje", Toast.LENGTH_SHORT).show()
                }

                else -> {
                    Toast.makeText(this, "Selecciona una opción válida", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }

            // Limpiar todos los campos después de enviar
            inputMatricula.setText("")
            inputTitulo.setText("")
            inputMensaje.setText("")
            spinnerCarrera.setSelection(0)
            spinnerSemestre.setSelection(0)
            spinnerNombres.adapter = null
        }
    }

    private fun cargarCarreras(spinner: Spinner) {
        db.collection("alumnos").get()
            .addOnSuccessListener { result ->
                val carreras = result.mapNotNull { it.getString("carrera") }.toSet().toList()
                spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, carreras)
            }
    }

    private fun cargarCiclos(spinner: Spinner) {
        db.collection("alumnos").get()
            .addOnSuccessListener { result ->
                val ciclos = result.mapNotNull { it.getString("ciclo") }.toSet().toList()
                spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, ciclos)
            }
    }

    /*private fun cargarAlumnos(spinner: Spinner, carrera: String, ciclo: String) {
        db.collection("alumnos")
            .whereEqualTo("carrera", carrera)
            .whereEqualTo("ciclo", ciclo)
            .get()
            .addOnSuccessListener { result ->
                val nombres = result.mapNotNull { it.getString("nombre") }
                spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, nombres)
            }
    }*/

    private fun cargarAlumnos(spinner: Spinner, carrera: String, ciclo: String) {
        val context = spinner.context  // Contexto seguro para el ArrayAdapter

        db.collection("alumnos")
            .whereEqualTo("carrera", carrera)
            .whereEqualTo("ciclo", ciclo)
            .get()
            .addOnSuccessListener { result ->
                val nombres = result.documents.mapNotNull { it.getString("nombre") }
                val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, nombres)
                spinner.adapter = adapter
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error al cargar alumnos", e)
                Toast.makeText(context, "Error al cargar alumnos", Toast.LENGTH_SHORT).show()
            }
    }
}