package mx.edu.unpa.calificacionesunpa.ui.login;

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import mx.edu.unpa.calificacionesunpa.MainActivity
import mx.edu.unpa.calificacionesunpa.R
import mx.edu.unpa.calificacionesunpa.data.persistent.UsuarioService
import mx.edu.unpa.calificacionesunpa.ui.actualizarpassword.ActualizarPasswordActivity
import mx.edu.unpa.calificacionesunpa.ui.fragments.LoadingFragment
import mx.edu.unpa.calificacionesunpa.ui.sescolares.EscolaresActivity
import mx.edu.unpa.calificacionesunpa.utils.Utilerias


@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private val viewModel: LoginViewModel by viewModels()

    //Permite definir atributos static
    companion object {
        private const val TAG = "LoginActivity"
    }

    private lateinit var etMatricula: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnRegistro: Button
    private lateinit var tvForgotPassword: TextView

    private val usuarioService = UsuarioService
    private var loadingFragment: LoadingFragment? = null
    private var matricula: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Inicialización de vistas
        etMatricula = findViewById(R.id.cp_txtConfirmPassword)
        etPassword = findViewById(R.id.cp_txtPassword)
        btnLogin = findViewById(R.id.cp_changepass)
        //btnRegistro = findViewById(R.id.btnRegistro)
//        tvForgotPassword = findViewById(R.id.btnRecuperar_contrasena)
        val togglePassword: ImageView = findViewById(R.id.togglePassword)

        // Mostrar/ocultar contraseña
        togglePassword.setOnClickListener {
            if (etPassword.inputType == (InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                etPassword.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                togglePassword.setImageResource(R.drawable.ic_visibility)
            } else {
                etPassword.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                togglePassword.setImageResource(R.drawable.ic_visibility_off)
            }
            etPassword.setSelection(etPassword.length())
        }


        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.alumnoState.collect { result ->
                    result?.let {
                        it.onSuccess { alumno ->
                            usuarioService.alumnoActual = alumno
                            val activitySRC: Intent
                            if (viewModel.esPrimerAcceso()) {
                                activitySRC = Intent(
                                    this@LoginActivity,
                                    ActualizarPasswordActivity::class.java
                                )
                            } else {
                                activitySRC = if (alumno.usuario?.matricula == "100000") {
                                    Intent(this@LoginActivity, EscolaresActivity::class.java)
                                } else {
                                    Intent(this@LoginActivity, MainActivity::class.java)
                                        .apply { putExtra("navigateTo", "calificaciones") }
                                }
                            }
                            //hideLoadingFragment()
                            startActivity(activitySRC)
                            finish()
                        }

                        it.onFailure { error ->
                            hideLoadingFragment()
                            Toast.makeText(
                                this@LoginActivity,
                                "Error al obtener alumno: ${error.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }
        }


        // --- BOTÓN LOGIN ---
        btnLogin.setOnClickListener {
            if (!formIsValid()) return@setOnClickListener
            if (!Utilerias.isNetworkAvailable(this)) {
                Toast.makeText(this, "Sin conexión a Internet. Revisa tu red.", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }
            showLoadingFragment()
            val matricula = etMatricula.text.toString().trim()
            val password = etPassword.text.toString().trim()
            viewModel.iniciarSesion(matricula, password)

        }



    }

    private fun formIsValid(): Boolean {
        val matricula = etMatricula.text.toString().trim()
        val password = etPassword.text.toString().trim()
        if (matricula.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }


    private fun showLoadingFragment() {
        val container: FrameLayout = findViewById(R.id.loadingFragmentContainer)
        container.visibility = FrameLayout.VISIBLE
        if (loadingFragment == null) {
            loadingFragment = LoadingFragment()
            supportFragmentManager.beginTransaction()
                .add(R.id.loadingFragmentContainer, loadingFragment!!)
                .commit()
        }
    }

    private fun hideLoadingFragment() {
        val container: FrameLayout = findViewById(R.id.loadingFragmentContainer)
        container.visibility = FrameLayout.GONE

        if (loadingFragment != null) {
            supportFragmentManager.beginTransaction()
                .remove(loadingFragment as Fragment)
                .commitAllowingStateLoss()
            loadingFragment = null
        }
    }
}

