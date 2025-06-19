package mx.edu.unpa.calificacionesunpa.ui.changePass;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import mx.edu.unpa.calificacionesunpa.MainActivity;
import mx.edu.unpa.calificacionesunpa.R;


public class ChangePassword extends AppCompatActivity {

    private EditText etActualPass;
    private EditText etNuevaPass;
    private EditText etConfirmarPass;
    private Button btnCambiar;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_change_password);

        etActualPass = findViewById(R.id.cp_txtActualPassword);
        etNuevaPass = findViewById(R.id.cp_txtnewPassword);
        etConfirmarPass = findViewById(R.id.cp_txtConfirmPassword);
        btnCambiar = findViewById(R.id.cp_changepass);
        auth = FirebaseAuth.getInstance();

        btnCambiar.setOnClickListener(view -> {
            String actualPass = etActualPass.getText().toString().trim();
            String nuevaPass = etNuevaPass.getText().toString().trim();
            String confirmacion = etConfirmarPass.getText().toString().trim();
            cambiarPassword(actualPass, nuevaPass, confirmacion);
        });
    }

    private void cambiarPassword(String actualPass, String nuevaPass, String confirmacion) {
        FirebaseUser user = auth.getCurrentUser();

        if (TextUtils.isEmpty(actualPass) || TextUtils.isEmpty(nuevaPass) || TextUtils.isEmpty(confirmacion)) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (actualPass.equals(nuevaPass)) {
            Toast.makeText(this, "La nueva contraseña debe ser distinta a la actual", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!nuevaPass.equals(confirmacion)) {
            Toast.makeText(this, "La nueva contraseña no coincide con la confirmación", Toast.LENGTH_SHORT).show();
            return;
        }

        if (user == null) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        String email = user.getEmail();

        if (email == null) {
            Toast.makeText(this, "No se pudo obtener el correo del usuario", Toast.LENGTH_SHORT).show();
            return;
        }

        // Reautenticación con la contraseña actual
        AuthCredential credential = EmailAuthProvider.getCredential(email, actualPass);
        user.reauthenticate(credential).addOnCompleteListener(reauthTask -> {
            if (reauthTask.isSuccessful()) {
                // Cambiar contraseña en Firebase Auth
                user.updatePassword(nuevaPass).addOnCompleteListener(updateTask -> {
                    if (updateTask.isSuccessful()) {
                        if (getIntent().getBooleanExtra("primerAcceso", false)) {
                            FirebaseFirestore.getInstance()
                                    .collection("usuarios")
                                    .document(user.getUid())
                                    .update("primerAcceso", false)
                                    .addOnSuccessListener(unused -> {
                                        Toast.makeText(this, "Contraseña actualizada", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(this, MainActivity.class);
                                        intent.putExtra("navigateTo", "calificaciones");
                                        startActivity(intent);
                                        finish();
                                    })
                                    .addOnFailureListener(e ->
                                            Toast.makeText(this, "Error al actualizar primer acceso", Toast.LENGTH_LONG).show()
                                    );
                        } else {
                            Toast.makeText(this, "Contraseña actualizada correctamente", Toast.LENGTH_SHORT).show();
                            etActualPass.setText("");
                            etNuevaPass.setText("");
                            etConfirmarPass.setText("");
                        }
                    } else {
                        Toast.makeText(ChangePassword.this, "Error al cambiar la contraseña en Auth", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(ChangePassword.this, "Error de reautenticación: contraseña incorrecta", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
