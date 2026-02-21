package mx.edu.unpa.calificacionesunpa.ui.notificaciones

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import mx.edu.unpa.calificacionesunpa.R
import mx.edu.unpa.calificacionesunpa.data.model.NotificationItem
import mx.edu.unpa.calificacionesunpa.ui.sescolares.EscolaresActivity
import java.time.LocalDate

class notificaciones_escolares : AppCompatActivity() {

    private val viewModel: NotificacionViewModel by viewModels()
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
    private lateinit var layoutListaMatriculas: LinearLayout

    private lateinit var btnAgregarMatricula: Button
    private lateinit var btnEnviar: Button


    private val listaMatriculas = mutableListOf<String>()

    private lateinit var notificacion: NotificationItem



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
        layoutListaMatriculas = findViewById(R.id.layoutListaMatriculas)

        btnAgregarMatricula = findViewById(R.id.btnAgregarMatricula)
        btnEnviar = findViewById(R.id.btnEnviar)

        // Inicialmente ocultar campos
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

        btnAgregarMatricula.setOnClickListener {
            val nuevaMatricula = inputMatriculaCiertos.text.toString().trim()
            if (nuevaMatricula.isEmpty()) {
                Toast.makeText(this, "Ingresa una matrícula válida", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (listaMatriculas.contains(nuevaMatricula)) {
                Toast.makeText(this, "La matrícula ya fue agregada", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            listaMatriculas.add(nuevaMatricula)
            agregarMatriculaView(nuevaMatricula)
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
                    // lógica para enviar notificación individual
                    notificacion = NotificationItem(
                        iconResId = R.drawable.notification,
                        destinatarios = listOf(matricula),
                        titulo = titulo,
                        mensaje = mensaje,
                        fueLeida = false,
                        esGlobal = false,
                        expiraEn = LocalDate.now().plusDays(3).toString(),
                        //timestamp = Date(),
                        remitente = "Servicios Escolares"
                        //remitente = UsuarioService.alumnoActual?.nombre ?: "Desconocido"

                    )




                    viewModel.enviarNotificacion(notificacion) { result ->
                        result.onSuccess { enviado ->
                            if (enviado) {
                                Toast.makeText(this, "Notificación enviada", Toast.LENGTH_SHORT)
                                    .show()
                            } else {
                                Toast.makeText(this, "No se pudo enviar", Toast.LENGTH_SHORT).show()
                            }
                        }
                        result.onFailure { error ->
                            Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }

                }

                radioCiertos.isChecked -> {
                    if (listaMatriculas.isEmpty()) {
                        Toast.makeText(this, "Agrega al menos una matrícula", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }

                    Toast.makeText(this,
                        "Enviando a: ${listaMatriculas.joinToString(", ")}\nTítulo: $titulo\nMensaje: $mensaje",
                        Toast.LENGTH_LONG).show()
                    // lógica para enviar notificaciones a la lista
                    notificacion = NotificationItem(
                        iconResId = R.drawable.notification,
                        destinatarios = listaMatriculas,
                        titulo = titulo,
                        mensaje = mensaje,
                        fueLeida = false,
                        esGlobal = false,
                        expiraEn = LocalDate.now().plusDays(3).toString(),
                        //timestamp = Date(),
                        remitente = "Servicios Escolares"
                    )
                    viewModel.enviarNotificacion(notificacion) { result ->
                        result.onSuccess { enviado ->
                            if (enviado) {
                                Toast.makeText(this, "Notificación enviada", Toast.LENGTH_SHORT)
                                    .show()
                            } else {
                                Toast.makeText(this, "No se pudo enviar", Toast.LENGTH_SHORT).show()
                            }
                        }
                        result.onFailure { error ->
                            Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                }

                radioTodos.isChecked -> {
                    Toast.makeText(this, "Enviando a todos\nTítulo: $titulo\nMensaje: $mensaje", Toast.LENGTH_LONG).show()
                    // lógica para enviar notificación global
                    // 11111111 -> All
                    notificacion = NotificationItem(
                        iconResId = R.drawable.notification,
                        destinatarios = listOf("11111111"),
                        titulo = titulo,
                        mensaje = mensaje,
                        fueLeida = false,
                        esGlobal = false,
                        expiraEn = LocalDate.now().plusDays(3).toString(),
                        // timestamp = Date(),
                        remitente = "Servicios Escolares"
                    )
                    viewModel.enviarNotificacion(notificacion) { result ->
                        result.onSuccess { enviado ->
                            if (enviado) {
                                Toast.makeText(this, "Notificación enviada", Toast.LENGTH_SHORT)
                                    .show()
                            } else {
                                Toast.makeText(this, "No se pudo enviar", Toast.LENGTH_SHORT).show()
                            }
                        }
                        result.onFailure { error ->
                            Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                }

                else -> {
                    Toast.makeText(this, "Selecciona una opción válida", Toast.LENGTH_SHORT).show()
                }
            }

            // Limpiar
            inputMatriculaIndividual.text.clear()
            inputTitulo.text.clear()
            inputMensaje.text.clear()
            listaMatriculas.clear()
            layoutListaMatriculas.removeAllViews()
        }
        val salir= findViewById<Button>(R.id.btnBack)
        salir.setOnClickListener{
            val intent= Intent(this, EscolaresActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onStart() {
        super.onStart()
        radioTodos.isChecked = true
    }

    private fun agregarMatriculaView(matricula: String) {
        val inflater = LayoutInflater.from(this)
        val view = inflater.inflate(R.layout.item_matricula, layoutListaMatriculas, false)

        val txtMatricula = view.findViewById<TextView>(R.id.txtMatriculaItem)
        val btnEliminar = view.findViewById<ImageView>(R.id.btnEliminarMatricula)

        txtMatricula.text = matricula
        btnEliminar.setOnClickListener {
            layoutListaMatriculas.removeView(view)
            listaMatriculas.remove(matricula)
        }

        layoutListaMatriculas.addView(view)
    }
}