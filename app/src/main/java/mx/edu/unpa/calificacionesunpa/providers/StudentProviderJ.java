// StudentProvider.java
package mx.edu.unpa.calificacionesunpa.providers;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import mx.edu.unpa.calificacionesunpa.models.StudentBasic;

public class StudentProviderJ {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public interface StudentBasicCallback {
        void onSuccess(@NonNull StudentBasic student);
        void onFailure(@NonNull Exception e);
    }

    /**
     * Recupera los campos básicos de Alumno por correo,
     * incluyendo la lista de rutas de materias.
     */
    public void fetchBasicByEmail(@NonNull String email,
                                  @NonNull StudentBasicCallback callback) {
        db.collection("Alumno")
                .whereEqualTo("correo", email)
                .get()
                .addOnSuccessListener(query -> {
                    if (!query.isEmpty()) {
                        DocumentSnapshot doc = query.getDocuments().get(0);

                        // Extrae campos sencillos
                        String nombre     = doc.getString("nombre");
                        String apeP       = doc.getString("apePaterno");
                        String apeM       = doc.getString("apeMaterno");
                        String correo     = doc.getString("correo");
                        String matricula  = doc.getString("matricula");
                        Boolean activoObj = doc.getBoolean("activo");
                        boolean activo    = activoObj != null && activoObj;

                        // Extrae el arreglo de rutas de materias
                        List<String> materiasPaths = new ArrayList<>();
                        Object raw = doc.get("materias");
                        if (raw instanceof List<?>) {
                            for (Object o : (List<?>) raw) {
                                if (o instanceof String) {
                                    materiasPaths.add((String) o);
                                }
                            }
                        }

                        // Construye el modelo y llama al callback
                        StudentBasic student = new StudentBasic(
                                nombre,
                                apeP,
                                apeM,
                                correo,
                                matricula,
                                activo,
                                materiasPaths
                        );
                        callback.onSuccess(student);

                    } else {
                        // Ningún alumno con ese correo: devolvemos lista vacía
                        StudentBasic empty = new StudentBasic(
                                "", "", "", "", "", false, new ArrayList<>()
                        );
                        callback.onSuccess(empty);
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }
}
