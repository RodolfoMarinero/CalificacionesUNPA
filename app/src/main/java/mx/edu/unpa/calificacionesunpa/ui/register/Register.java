package mx.edu.unpa.calificacionesunpa.ui.register;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


import androidx.appcompat.app.AppCompatActivity;

import mx.edu.unpa.calificacionesunpa.R;

public class Register extends AppCompatActivity {

    private EditText txtNombre,txtMatricula,txtCorreo,txtPass;
    private Button btnRegistrarse;
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
            String matricula = txtMatricula.getText().toString().trim();
            String correo = txtCorreo.getText().toString().trim();
            String pass = txtPass.getText().toString().trim();

            // Validación básica de campos
            if (correo.isEmpty() || pass.isEmpty()) {
                Toast.makeText(Register.this, "El correo y la contraseña son obligatorios", Toast.LENGTH_SHORT).show();
                return;
            }

            // Instancia de FirebaseAuth
            FirebaseAuth auth = FirebaseAuth.getInstance();

            // Intento de registro
            auth.createUserWithEmailAndPassword(correo, pass)
                    .addOnCompleteListener(Register.this, task -> {
                        if (task.isSuccessful()) {
                            // Registro exitoso
                            FirebaseUser user = auth.getCurrentUser();
                            Toast.makeText(Register.this, "Usuario registrado correctamente", Toast.LENGTH_SHORT).show();

                            // Opcional: guardar datos adicionales en Firestore o Realtime Database
                            // Por ejemplo, utilizando FirebaseFirestore:
                    /*
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    Map<String, Object> usuario = new HashMap<>();
                    usuario.put("nombre", nombre);
                    usuario.put("matricula", matricula);
                    usuario.put("correo", correo);

                    db.collection("usuarios")
                      .document(user.getUid())
                      .set(usuario)
                      .addOnSuccessListener(aVoid -> Log.d("Register", "Datos guardados correctamente"))
                      .addOnFailureListener(e -> Log.w("Register", "Error al guardar los datos", e));
                    */

                        } else {
                            // Registro fallido. Mostrar el mensaje de error.
                            Toast.makeText(Register.this, "Error de registro: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });

    }




}
