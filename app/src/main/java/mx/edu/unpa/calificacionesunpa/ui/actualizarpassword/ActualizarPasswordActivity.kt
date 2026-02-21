package mx.edu.unpa.calificacionesunpa.ui.actualizarpassword

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import mx.edu.unpa.calificacionesunpa.MainActivity
import mx.edu.unpa.calificacionesunpa.R
import mx.edu.unpa.calificacionesunpa.data.persistent.UsuarioService


@AndroidEntryPoint
class ActualizarPasswordActivity : AppCompatActivity() {

    private val viewModel: UsuarioViewModel by viewModels()
    private var passwordActualEditText: EditText? = null
    private var passwordNuevoEditText: EditText? = null
    private var passwordConfirmacionEditText: EditText? = null
    private var cambiarButton: Button? = null
    private var cancelarButton: Button? = null
    private var layoutPassAct: RelativeLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_change_password)

        passwordActualEditText = findViewById<EditText?>(R.id.cp_txtActualPassword)
        passwordNuevoEditText = findViewById<EditText?>(R.id.cp_txtnewPassword)
        passwordConfirmacionEditText = findViewById<EditText?>(R.id.cp_txtConfirmPassword)
        cambiarButton = findViewById<Button?>(R.id.cp_changepass)
        cancelarButton = findViewById<Button?>(R.id.cp_cancelchangepass)
        //auth = FirebaseAuth.getInstance()
        layoutPassAct = findViewById<RelativeLayout?>(R.id.layout_actual_password)

        val togglePassword = findViewById<ImageView?>(R.id.toggleActual)
        togglePassword.setOnClickListener(View.OnClickListener { v: View? ->
            if (passwordActualEditText!!.getInputType() == (InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                passwordActualEditText!!.setInputType(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD)
                togglePassword.setImageResource(R.drawable.ic_visibility)
            } else {
                passwordActualEditText!!.setInputType(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)
                togglePassword.setImageResource(R.drawable.ic_visibility_off)
            }
            passwordActualEditText!!.setSelection(passwordActualEditText!!.length())
        })

        val togglePasswordN = findViewById<ImageView?>(R.id.toggleNew)
        togglePasswordN.setOnClickListener(View.OnClickListener { v: View? ->
            if (passwordNuevoEditText!!.getInputType() == (InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                passwordNuevoEditText!!.setInputType(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD)
                togglePasswordN.setImageResource(R.drawable.ic_visibility)
            } else {
                passwordNuevoEditText!!.setInputType(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)
                togglePasswordN.setImageResource(R.drawable.ic_visibility_off)
            }
            passwordNuevoEditText!!.setSelection(passwordNuevoEditText!!.length())
        })

        val togglePasswordNC = findViewById<ImageView?>(R.id.toggleConfirm)
        togglePasswordNC.setOnClickListener(View.OnClickListener { v: View? ->
            if (passwordConfirmacionEditText!!.getInputType() == (InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                passwordConfirmacionEditText!!.setInputType(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD)
                togglePasswordNC.setImageResource(R.drawable.ic_visibility)
            } else {
                passwordConfirmacionEditText!!.setInputType(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)
                togglePasswordNC.setImageResource(R.drawable.ic_visibility_off)
            }
            passwordConfirmacionEditText!!.setSelection(passwordConfirmacionEditText!!.length())
        })




        if (UsuarioService.alumnoActual?.usuario?.esPrimerAcceso==true) {
            passwordActualEditText!!.setVisibility(View.GONE)
            layoutPassAct!!.setVisibility(View.GONE)
        }


        cambiarButton!!.setOnClickListener(View.OnClickListener { view: View? ->
            var actualPass = UsuarioService.alumnoActual?.matricula ?: ""
            if (UsuarioService.alumnoActual?.usuario?.esPrimerAcceso==false) {
                actualPass = passwordActualEditText!!.getText().toString().trim { it <= ' ' }
            }
            val nuevaPass = passwordNuevoEditText!!.getText().toString().trim { it <= ' ' }
            val confirmacion =
                passwordConfirmacionEditText!!.getText().toString().trim { it <= ' ' }
            if (datosValidos(actualPass, nuevaPass, confirmacion)) {
                viewModel.cambiarPassword(UsuarioService.alumnoActual?.matricula ?: "", actualPass, nuevaPass)
            }

            lifecycleScope.launch {
                viewModel.estado.collect { result ->
                    result?.onSuccess {
                        passwordActualEditText!!.setText("")
                        passwordNuevoEditText!!.setText("")
                        passwordConfirmacionEditText!!.setText("")
                        Toast.makeText(
                            this@ActualizarPasswordActivity,
                            "Éxito: "+result.toString(),
                            Toast.LENGTH_LONG
                        ).show()
                        // Redirección en caso de primer acceso
                        if (UsuarioService.alumnoActual?.usuario?.esPrimerAcceso == true) {
                            val intent =
                                Intent(this@ActualizarPasswordActivity, MainActivity::class.java)
                            intent.putExtra("navigateTo", "calificaciones")
                            startActivity(intent)
                            finish()
                        }
                    }?.onFailure {
                        Toast.makeText(
                            this@ActualizarPasswordActivity,
                            "Error:"+result.toString(),
                            Toast.LENGTH_LONG
                        ).show()
                        Log.e("ChangePassword", "Error:" + result.toString())
                    }
                }
            }
        })

        cancelarButton!!.setOnClickListener(View.OnClickListener { view: View? -> cancelarCambio() })
    }

    private fun datosValidos(
        actualPass: String?,
        nuevaPass: String?,
        confirmacion: String?
    ): Boolean {
        var actualPass = actualPass
        //val esPrimerAcceso = getIntent().getBooleanExtra("primerAcceso", false)
        val esPrimerAcceso = UsuarioService.alumnoActual?.usuario?.esPrimerAcceso
        var resultado = true
        if (UsuarioService.alumnoActual == null) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            return false
        } else {
            if (esPrimerAcceso == true) {
                actualPass = UsuarioService.alumnoActual?.matricula
            }
            if (TextUtils.isEmpty(actualPass) || TextUtils.isEmpty(nuevaPass) || TextUtils.isEmpty(
                    confirmacion
                )
            ) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                resultado = false
            } else if (actualPass == nuevaPass) {
                Toast.makeText(
                    this,
                    "La nueva contraseña debe ser distinta a la actual",
                    Toast.LENGTH_SHORT
                ).show()
                resultado = false
            } else if (nuevaPass != confirmacion) {
                Toast.makeText(
                    this,
                    "La nueva contraseña no coincide con la confirmación",
                    Toast.LENGTH_SHORT
                ).show()
                resultado = false
            } else if (nuevaPass!!.length < 6) {
                Toast.makeText(
                    this,
                    "La nueva contraseña debe tener al menos 6 caracteres",
                    Toast.LENGTH_SHORT
                ).show()
                resultado = false
            }
        }
        return resultado
    }

    private fun cancelarCambio() {
        passwordActualEditText!!.setText("")
        passwordNuevoEditText!!.setText("")
        passwordConfirmacionEditText!!.setText("")
        finish()
    }
}