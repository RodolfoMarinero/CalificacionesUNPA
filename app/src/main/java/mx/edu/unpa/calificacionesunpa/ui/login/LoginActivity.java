package mx.edu.unpa.calificacionesunpa.ui.login;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseUser;

import kotlin.Unit;
import kotlin.jvm.internal.Intrinsics;
import mx.edu.unpa.calificacionesunpa.MainActivity;
import mx.edu.unpa.calificacionesunpa.R;
import mx.edu.unpa.calificacionesunpa.fragments.LoadingFragment;
import mx.edu.unpa.calificacionesunpa.providers.AlumnoProvider;
import mx.edu.unpa.calificacionesunpa.providers.AuthProvider;
import mx.edu.unpa.calificacionesunpa.providers.NotificacionProvider;
import mx.edu.unpa.calificacionesunpa.service.UsuarioService;
import mx.edu.unpa.calificacionesunpa.ui.perfil.FragmentPerfilN;
import mx.edu.unpa.calificacionesunpa.ui.recuperarContrasena.RecuperarContrasena;
//import mx.edu.unpa.calificacionesunpa.ui.register.Register;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";

    // Ahora este campo contendrá la matrícula, no el correo completo
    private EditText etMatricula, etPassword;
    private Button btnLogin, btnRegistro;
    private TextView tvForgotPassword;
    private AuthProvider authProvider;
    private AlumnoProvider alumnoProvider;
    private UsuarioService usuarioService = UsuarioService.INSTANCE;
    private NotificacionProvider notificacionProvider;


    private FirebaseUser userGoogle;

    private FragmentPerfilN fragmentPerfilN;
    private LoadingFragment loadingFragment;


    private boolean isFragmentVisible = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        FirebaseApp.initializeApp(this);

        // Cambiar el id en el layout a et_matricula para mayor claridad,
        // pero si no lo cambias, sigue usando R.id.et_correo aquí:
        etMatricula     = findViewById(R.id.cp_txtConfirmPassword);
        etPassword      = findViewById(R.id.cp_txtPassword);
        btnLogin        = findViewById(R.id.cp_changepass);
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
            showLoadingFragment();

            // Leemos matrícula en lugar de correo
            String matricula = etMatricula.getText().toString().trim();
            String password  = etPassword.getText().toString().trim();

            // Generamos el correo de Firebase a partir de la matrícula
            String email = matricula + "@unpaloma.com";

            authProvider.login(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Limpia los campos
                            etMatricula.setText("");
                            etPassword.setText("");
                            //solicita el alumno
                            alumnoProvider = new AlumnoProvider();
                            notificacionProvider = new NotificacionProvider();
                            alumnoProvider.obtenerAlumnoConMateriasDeUsuario(
                                    authProvider.getId(),
                                    alumno -> {
                                        usuarioService.setAlumnoActual(alumno);
                                        notificacionProvider.cargarNotificacionesDesdeFirestore();
                                        hideLoadingFragment();
                                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                        intent.putExtra("navigateTo", "calificaciones");
                                        startActivity(intent);
                                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                                        finish();
                                        return Unit.INSTANCE;
                                    }
                            );


                        } else {
                            hideLoadingFragment();

                            String err = task.getException() != null
                                    ? task.getException().getMessage()
                                    : "Error desconocido";
                            Log.w(TAG, "Login failed: " + err);
                            Toast.makeText(this, "Falló el login: " + err, Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        hideLoadingFragment();

                        Log.e(TAG, "Login exception", e);
                        if (e instanceof FirebaseNetworkException) {
                            Toast.makeText(this, "Error de red: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(this, "Error inesperado: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        });
        tvForgotPassword.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, RecuperarContrasena.class))
        );

        //Button button = findViewById(R.id.btnGoogle);
        validarGoogle();

    }


    public void validarGoogle(){
        Button button = findViewById(R.id.btnGoogle);
        SharedPreferences sharedPref = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);

        if(sharedPref.contains("accesoConGoogle")){
            boolean acceso= sharedPref.getBoolean(("accesoConGoogle"),false);
            button.setEnabled(acceso);
        }else{
            button.setEnabled(false); // Esto desactiva el botón
        }
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
    private void showLoadingFragment() {
        FrameLayout container = findViewById(R.id.loadingFragmentContainer);
        container.setVisibility(View.VISIBLE);

        if (loadingFragment == null) {
            loadingFragment = new LoadingFragment();
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.loadingFragmentContainer, loadingFragment)
                    .commit();
        }
    }

    private void hideLoadingFragment() {
        FrameLayout container = findViewById(R.id.loadingFragmentContainer);
        container.setVisibility(View.GONE);

        if (loadingFragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .remove(loadingFragment)
                    .commit();
            loadingFragment = null;
        }
    }

    public void loginGoogle(View view) {
        Toast.makeText(this,
                "Entró",
                Toast.LENGTH_SHORT).show();
    }

    /*public void googleAccountExists(){
        if(userGoogle.getEmail())
    }*/

}
