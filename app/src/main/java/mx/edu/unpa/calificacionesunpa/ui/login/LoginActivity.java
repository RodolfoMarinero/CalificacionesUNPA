package mx.edu.unpa.calificacionesunpa.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import mx.edu.unpa.calificacionesunpa.MainActivity;
import mx.edu.unpa.calificacionesunpa.R;
import mx.edu.unpa.calificacionesunpa.providers.AuthProvider;
import mx.edu.unpa.calificacionesunpa.ui.recuperarContrasena.RecuperarContrasena;
import mx.edu.unpa.calificacionesunpa.ui.register.Register;

public class LoginActivity extends AppCompatActivity {
    private EditText etMatricula, etPassword;
    private Button btnLogin, btnRegistro;
    private TextView tvForgotPassword;
    private AuthProvider authProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Asignación de vistas
        etMatricula = findViewById(R.id.txtMatricula);
        etPassword = findViewById(R.id.txtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegistro = findViewById(R.id.btnRegistro);
        tvForgotPassword = findViewById(R.id.btnRecuperar_contrasena);

        // Inicializar el proveedor de autenticación
        authProvider = new AuthProvider();

        // Iniciar sesión con AuthProvider
        btnLogin.setOnClickListener(view -> {
            if (!isValidateForm()) {
                return;
            }

            String email = etMatricula.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            // Usar el método login del AuthProvider para iniciar sesión
            authProvider.login(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Login exitoso
                            // Limpiar campos
                            etMatricula.setText("");
                            etPassword.setText("");

                            // Navegar a MainActivity con la dirección específica ("calificaciones")
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            intent.putExtra("navigateTo", "calificaciones");
                            startActivity(intent);
                            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                            finish();
                        } else {
                            // Login fallido, mostrar mensaje de error
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            intent.putExtra("navigateTo", "calificaciones");
                            startActivity(intent);
                            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                            finish();
                            Toast.makeText(LoginActivity.this, "Matrícula o contraseña incorrecta", Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        // Registro
        btnRegistro.setOnClickListener(view -> {
            startActivity(new Intent(LoginActivity.this, Register.class));
        });

        // Recuperar contraseña
        tvForgotPassword.setOnClickListener(view -> {
            startActivity(new Intent(LoginActivity.this, RecuperarContrasena.class));
        });
    }

    /**
     * Verifica que los campos de correo (o matrícula) y contraseña no estén vacíos.
     * Muestra un Toast informativo en caso de que alguno esté vacío.
     *
     * @return true si ambos campos tienen contenido, false en otro caso.
     */
    private boolean isValidateForm() {
        String email = etMatricula.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(LoginActivity.this, "Por favor, rellena todos los campos", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}
