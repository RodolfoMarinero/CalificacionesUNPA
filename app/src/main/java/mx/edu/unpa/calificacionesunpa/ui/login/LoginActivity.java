package mx.edu.unpa.calificacionesunpa.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import mx.edu.unpa.calificacionesunpa.MainActivity;
import mx.edu.unpa.calificacionesunpa.R;

public class LoginActivity extends AppCompatActivity {

    private EditText etMatricula, etPassword;
    private Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Inicializar vistas
        etMatricula = findViewById(R.id.et_matricula);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);

        // Configurar el botón de inicio de sesión
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String matricula = etMatricula.getText().toString();
                String password = etPassword.getText().toString();

                if (validarCredenciales(matricula, password)) {
                    // Si las credenciales son correctas, iniciar la actividad principal
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish(); // Finaliza la actividad de login para que no se quede en el historial
                } else {
                    // Si las credenciales son incorrectas, mostrar un mensaje
                    Toast.makeText(LoginActivity.this, "Matrícula o contraseña incorrectos", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Método para validar las credenciales
    private boolean validarCredenciales(String matricula, String password) {
        // Aquí puedes colocar la lógica para validar las credenciales, por ejemplo, con una base de datos o API.
        // Para este ejemplo, vamos a suponer que la matrícula es "123456" y la contraseña es "password".
        return matricula.equals("123456") && password.equals("password");
    }
}
