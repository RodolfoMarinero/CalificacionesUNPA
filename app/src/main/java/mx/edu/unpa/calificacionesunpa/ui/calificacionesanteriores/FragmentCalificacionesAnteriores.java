package mx.edu.unpa.calificacionesunpa.ui.calificacionesanteriores;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

import mx.edu.unpa.calificacionesunpa.R;
import mx.edu.unpa.calificacionesunpa.models.StudentBasic;
import mx.edu.unpa.calificacionesunpa.providers.StudentProviderJ;

public class FragmentCalificacionesAnteriores extends Fragment {

    private StudentProviderJ studentProviderJ;
    private TextView tvHolaUsuario;
    private TextView txtMatricula;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(
                R.layout.fragment_calificaciones_anteriores,
                container,
                false
        );

        // 1) Vistas del encabezado
        tvHolaUsuario = root.findViewById(R.id.tvHolaUsuario);
        txtMatricula  = root.findViewById(R.id.txtMatricula);

        // 2) Inicializa el provider
        studentProviderJ = new StudentProviderJ();

        // 3) Obtén el email (argumento o FirebaseAuth)
        String email = null;
        if (getArguments() != null) {
            email = getArguments().getString("email");
        }
        if (email == null && FirebaseAuth.getInstance().getCurrentUser() != null) {
            email = FirebaseAuth.getInstance()
                    .getCurrentUser()
                    .getEmail();
        }

        // 4) Lanza la consulta
        if (email != null) {
            studentProviderJ.fetchBasicByEmail(email, new StudentProviderJ.StudentBasicCallback() {
                @Override
                public void onSuccess(@NonNull StudentBasic student) {
                    // Coloca nombre y matrícula en la UI
                    String nombre = student.getNombre() != null
                            ? student.getNombre()
                            : "Usuario";
                    String matricula = student.getMatricula() != null
                            ? student.getMatricula()
                            : "";
                    tvHolaUsuario.setText("Hola, " + nombre + "!");
                    txtMatricula .setText(matricula);
                }

                @Override
                public void onFailure(@NonNull Exception e) {
                    tvHolaUsuario.setText("Error cargando alumno");
                }
            });
        } else {
            tvHolaUsuario.setText("Email no disponible");
        }

        return root;
    }
}
