package mx.edu.unpa.calificacionesunpa

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import mx.edu.unpa.calificacionesunpa.providers.StorageProvider
import mx.edu.unpa.calificacionesunpa.service.ArchivoUtils

import mx.edu.unpa.calificacionesunpa.adapters.MatriculaAdapter

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
    private val REQUEST_CODE_PDF = 101
    private var uriPDF: Uri? = null

    private lateinit var inputMatriculaIndividual: EditText
    private lateinit var inputMatriculaCiertos: EditText

    private lateinit var inputTitulo: EditText
    private lateinit var inputMensaje: EditText

    private lateinit var layoutMatricula: LinearLayout
    private lateinit var layoutCiertos: LinearLayout

    private lateinit var btnAgregarMatricula: Button
    private lateinit var recyclerMatriculas: RecyclerView

    private lateinit var btnEnviar: Button

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    // Lista mutable para guardar las matrículas agregadas
    private val listaMatriculas = mutableListOf<String>()
    private lateinit var adapterMatriculas: MatriculaAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.notificaciones_escolares)

        // Vincular vistas
        radioIndividual = findViewById(R.id.radioIndividual)
        radioCiertos = findViewById(R.id.radioCiertos)
        radioTodos = findViewById(R.id.radioTodos)

        inputMatriculaIndividual = findViewById(R.id.inputMatricula)
        inputMatriculaCiertos = findViewById(R.id.inputMatriculaCiertos)

        inputTitulo = findViewById(R.id.inputTitulo)
        inputMensaje = findViewById(R.id.inputMensaje)

        layoutMatricula = findViewById(R.id.layoutMatricula)
        layoutCiertos = findViewById(R.id.layoutCiertos)

        btnAgregarMatricula = findViewById(R.id.btnAgregarMatricula)
        recyclerMatriculas = findViewById(R.id.recyclerMatriculas)

        btnEnviar = findViewById(R.id.btnEnviar)
        val btnSeleccionarPdf = findViewById<Button>(R.id.btnSeleccionarPdf)
        val btnConvertirBase64 = findViewById<Button>(R.id.btnConvertir)
        val txtNombreArchivo = findViewById<TextView>(R.id.txtNombreArchivo)
        btnSeleccionarPdf.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "application/pdf"
            startActivityForResult(intent, REQUEST_CODE_PDF)
        }
        btnConvertirBase64.setOnClickListener {
            uriPDF?.let {
                val base64 = ArchivoUtils.convertirA_Base64(this, it)
                if (base64 != null) {
                    val storageProvider = StorageProvider()
                    storageProvider.uploadFile(base64, txtNombreArchivo.toString()) { success ->
                        if (success) {
                            Toast.makeText(this, "Archivo subido exitosamente", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, "Error al subir el archivo", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }

        // Configurar RecyclerView
        recyclerMatriculas.layoutManager = LinearLayoutManager(this)
        adapterMatriculas = MatriculaAdapter(listaMatriculas) { matricula ->
            // Callback para eliminar matrícula
            listaMatriculas.remove(matricula)
            adapterMatriculas.notifyDataSetChanged()
        }
        recyclerMatriculas.adapter = adapterMatriculas

        // Ocultar layouts inicialmente
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

        // Botón para agregar matrícula en "Ciertos alumnos"
        btnAgregarMatricula.setOnClickListener {
            val matricula = inputMatriculaCiertos.text.toString().trim()
            if (matricula.isEmpty()) {
                Toast.makeText(this, "Ingresa una matrícula válida", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (listaMatriculas.contains(matricula)) {
                Toast.makeText(this, "La matrícula ya fue agregada", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            listaMatriculas.add(matricula)
            adapterMatriculas.notifyDataSetChanged()
            inputMatriculaCiertos.text.clear()
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
                    val matricula = inputMatriculaIndividual.text.toString().trim()
                    if (matricula.isEmpty()) {
                        Toast.makeText(this, "Por favor ingresa la matrícula", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }

                    Toast.makeText(this, "Enviando a matrícula: $matricula\nTítulo: $titulo\nMensaje: $mensaje", Toast.LENGTH_LONG).show()
                    // Aquí va la lógica para enviar notificación individual
                }

                radioCiertos.isChecked -> {
                    if (listaMatriculas.isEmpty()) {
                        Toast.makeText(this, "Agrega al menos una matrícula", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }

                    Toast.makeText(this,
                        "Enviando a matrículas: ${listaMatriculas.joinToString(", ")}\nTítulo: $titulo\nMensaje: $mensaje",
                        Toast.LENGTH_LONG).show()
                    // Aquí va la lógica para enviar notificación a las matrículas de la lista
                }

                radioTodos.isChecked -> {
                    Toast.makeText(this, "Enviando a todos\nTítulo: $titulo\nMensaje: $mensaje", Toast.LENGTH_LONG).show()
                    // Aquí va la lógica para enviar notificación a todos
                }

                else -> {
                    Toast.makeText(this, "Selecciona una opción válida", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }

            // Limpiar campos después de enviar
            inputMatriculaIndividual.text.clear()
            listaMatriculas.clear()
            adapterMatriculas.notifyDataSetChanged()
            inputTitulo.text.clear()
            inputMensaje.text.clear()
        }
    }
}