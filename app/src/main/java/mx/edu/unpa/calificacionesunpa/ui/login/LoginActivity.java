package mx.edu.unpa.calificacionesunpa.ui.login;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseNetworkException;

import mx.edu.unpa.calificacionesunpa.MainActivity;
import mx.edu.unpa.calificacionesunpa.R;
import mx.edu.unpa.calificacionesunpa.providers.AuthProvider;
import mx.edu.unpa.calificacionesunpa.ui.recuperarContrasena.RecuperarContrasena;
import mx.edu.unpa.calificacionesunpa.ui.register.Register;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";

    private EditText etCorreo, etPassword;
    private Button btnLogin, btnRegistro;
    private TextView tvForgotPassword;
    private AuthProvider authProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Inicializa Firebase explícitamente
        FirebaseApp.initializeApp(this);

        etCorreo = findViewById(R.id.et_correo);
        etPassword = findViewById(R.id.txtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegistro = findViewById(R.id.btnRegistro);
        tvForgotPassword = findViewById(R.id.btnRecuperar_contrasena);

        authProvider = new AuthProvider();

        btnLogin.setOnClickListener(v -> {
            // 1) Validar que no estén vacíos
            if (!isValidateForm()) return;

            // 2) Verificar conexión de red
            if (!isNetworkAvailable()) {
                Toast.makeText(this,
                        "Sin conexión a Internet. Revisa tu red.",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            // 3) Intentar login
            String email = etCorreo.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            authProvider.login(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Login exitoso
                            etCorreo.setText("");
                            etPassword.setText("");

                            // Obtener el correo del usuario autenticado
                            String loggedEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

                            if (loggedEmail != null) {
                                // Crear un Intent para pasar el correo a MainActivity
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                intent.putExtra("email", loggedEmail);  // Pasar solo el correo como extra
                                intent.putExtra("navigateTo", "calificaciones"); // Si necesitas redirigir a calificaciones
                                startActivity(intent);
                                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                                finish();
                            }
                        } else {
                            // Credenciales inválidas u otro fallo
                            String err = task.getException() != null
                                    ? task.getException().getMessage()
                                    : "Error desconocido";
                            Log.w(TAG, "Login failed: " + err);
                            Toast.makeText(this, "Falló el login: " + err, Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Error de red o timeout
                        Log.e(TAG, "Login exception", e);
                        if (e instanceof FirebaseNetworkException) {
                            Toast.makeText(this, "Error de red: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(this, "Error inesperado: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        });

        btnRegistro.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, Register.class))
        );

        tvForgotPassword.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, RecuperarContrasena.class))
        );
    }

    /** Verifica que correo y contraseña no estén vacíos */
    private boolean isValidateForm() {
        String email = etCorreo.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this,
                    "Por favor, completa todos los campos",
                    Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    /** Comprueba si hay conexión de red activa */
    private boolean isNetworkAvailable() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm != null ? cm.getActiveNetworkInfo() : null;
        return ni != null && ni.isConnected();
    }
}
