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

    // Ahora este campo contendrá la matrícula, no el correo completo
    private EditText etMatricula, etPassword;
    private Button btnLogin, btnRegistro;
    private TextView tvForgotPassword;
    private AuthProvider authProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        FirebaseApp.initializeApp(this);

        // Cambiar el id en el layout a et_matricula para mayor claridad,
        // pero si no lo cambias, sigue usando R.id.et_correo aquí:
        etMatricula     = findViewById(R.id.et_correo);
        etPassword      = findViewById(R.id.txtPassword);
        btnLogin        = findViewById(R.id.btnLogin);
        btnRegistro     = findViewById(R.id.btnRegistro);
        tvForgotPassword= findViewById(R.id.btnRecuperar_contrasena);

        authProvider = new AuthProvider();

        btnLogin.setOnClickListener(v -> {
            if (!isValidateForm()) return;

            if (!isNetworkAvailable()) {
                Toast.makeText(this,
                        "Sin conexión a Internet. Revisa tu red.",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            // Leemos matrícula en lugar de correo
            String matricula = etMatricula.getText().toString().trim();
            String password  = etPassword.getText().toString().trim();

            // Generamos el correo de Firebase a partir de la matrícula
            String email = matricula + "@gmail.com";

            authProvider.login(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Limpia los campos
                            etMatricula.setText("");
                            etPassword.setText("");

                            // Pasamos la matrícula (y si lo necesitas, el e-mail generado)
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            intent.putExtra("email", email);
                            intent.putExtra("matricula", matricula);
                            intent.putExtra("navigateTo", "calificaciones");
                            startActivity(intent);
                            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                            finish();
                        } else {
                            String err = task.getException() != null
                                    ? task.getException().getMessage()
                                    : "Error desconocido";
                            Log.w(TAG, "Login failed: " + err);
                            Toast.makeText(this, "Falló el login: " + err, Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnFailureListener(e -> {
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

    private boolean isValidateForm() {
        String matricula = etMatricula.getText().toString().trim();
        String password  = etPassword.getText().toString().trim();

        if (matricula.isEmpty() || password.isEmpty()) {
            Toast.makeText(this,
                    "Por favor, completa todos los campos",
                    Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm != null ? cm.getActiveNetworkInfo() : null;
        return ni != null && ni.isConnected();
    }
}
