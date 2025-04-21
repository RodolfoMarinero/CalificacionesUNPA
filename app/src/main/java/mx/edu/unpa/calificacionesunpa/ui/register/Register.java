package mx.edu.unpa.calificacionesunpa.ui.register;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Collections;
import java.util.List;

import mx.edu.unpa.calificacionesunpa.R;
import mx.edu.unpa.calificacionesunpa.models.Materia;
import mx.edu.unpa.calificacionesunpa.models.Student;
import mx.edu.unpa.calificacionesunpa.providers.AuthProvider;
import mx.edu.unpa.calificacionesunpa.providers.StudentProvider;

public class Register extends AppCompatActivity {

    private EditText txtNombre, txtMatricula, txtCorreo, txtPass;
    private Button btnRegistrarse;

    private final AuthProvider authProvider = new AuthProvider();
    private final StudentProvider studentProvider = new StudentProvider();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);

        txtNombre = findViewById(R.id.et_nombre);
        txtMatricula = findViewById(R.id.et_matricula);
        txtCorreo = findViewById(R.id.et_correo);
        txtPass = findViewById(R.id.et_contrasena);
        btnRegistrarse = findViewById(R.id.btn_registrarse);

        btnRegistrarse.setOnClickListener(view -> {
            String nombre = txtNombre.getText().toString().trim();
            String matricula = txtMatricula.getText().toString().trim(); // Se usará como studentID
            String correo = txtCorreo.getText().toString().trim();
            String pass = txtPass.getText().toString().trim();

            if (correo.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "El correo y la contraseña son obligatorios", Toast.LENGTH_SHORT).show();
                return;
            }

            authProvider.register(correo, pass).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Usuario autenticado", Toast.LENGTH_SHORT).show();

                    List<Materia> materias = Collections.singletonList(
                            new Materia(
                                    "5",
                                    "Cálculo Diferencial",
                                    "SEGUNDO",
                                    "2021-2022B",
                                    "/Calificaciones/QkQPXnQnkalDPG4gZlXn",
                                    true
                            )
                    );

                    Student alumno = new Student(
                            matricula,
                            nombre,
                            "Marinero",
                            "Cruz",
                            true,
                            materias
                    );

                    studentProvider.create(alumno).addOnSuccessListener(unused -> {
                        Toast.makeText(this, "Alumno registrado en Firestore", Toast.LENGTH_SHORT).show();
                    }).addOnFailureListener(e -> {
                        Toast.makeText(this, "Error al guardar alumno: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });

                } else {
                    Toast.makeText(this, "Error de registro: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}
