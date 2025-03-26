package mx.edu.unpa.calificacionesunpa.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import mx.edu.unpa.calificacionesunpa.MainActivity;
import mx.edu.unpa.calificacionesunpa.R;
import mx.edu.unpa.calificacionesunpa.ui.recuperarContrasena.RecuperarContrasena;
import mx.edu.unpa.calificacionesunpa.ui.register.Register;

public class LoginActivity extends AppCompatActivity {
    private EditText etMatricula, etPassword;
    private Button btnLogin, btnRegistro;
    private TextView tvForgotPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Asignación de vistas a variables
        etMatricula = findViewById(R.id.et_matricula);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        btnRegistro = findViewById(R.id.btn_registro);
        tvForgotPassword = findViewById(R.id.tv_recuperar_contrasena);

        // Evento para el botón de iniciar sesión
        btnLogin.setOnClickListener(view -> {
            String matricula = etMatricula.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            // Verificar que la matrícula tenga exactamente 8 dígitos
            if (!matricula.matches("\\d{8}")) {
                etMatricula.setError("La matrícula debe tener exactamente 8 dígitos");
                etMatricula.requestFocus();
                return;
            }

            // Validación de autenticación simulada
            if ("12345678".equals(matricula) && "password".equals(password)) {
                // Autenticación exitosa: Limpiar campos
                etMatricula.setText("");
                etPassword.setText("");

                // Transición animada y cambio de actividad
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

                // Cierra la actividad actual para que no regrese con el botón "atrás"
                finish();
            } else {
                // Manejo de error de autenticación
                etPassword.setError("Matrícula o contraseña incorrecta");
                etPassword.requestFocus();
            }
        });



        // Evento para el botón de registro
        btnRegistro.setOnClickListener(view -> {
            startActivity(new Intent(LoginActivity.this, Register.class));
        });

        // Evento para el enlace de recuperar contraseña
        tvForgotPassword.setOnClickListener(view -> {
            // Aquí iría la lógica para recuperar la contraseña
            startActivity(new Intent(LoginActivity.this, RecuperarContrasena.class));
        });
    }
}
