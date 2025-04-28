package mx.edu.unpa.calificacionesunpa.providers

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import mx.edu.unpa.calificacionesunpa.models.Student

class StudentProvider {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()


    fun create(student: Student): Task<Void> {
        val matricula = student.matricula
            ?: throw IllegalArgumentException("Matricula is required to create the student document")
        return db.collection("Alumno")
            .document(matricula)
            .set(student)
    }

    /**
     * Recupera un alumno por su correo.
     * Llama a callback.onCallback(null) si no existe o hay error.
     */
    interface StudentCallback {
        fun onCallback(student: Student?)
    }

    fun getStudentByEmail(email: String, callback: StudentCallback) {
        db.collection("Alumno")
            .whereEqualTo("correo", email)
            .get()
            .addOnSuccessListener { result ->
                if (!result.isEmpty) {
                    val student = result.documents[0].toObject(Student::class.java)
                    callback.onCallback(student)
                } else {
                    callback.onCallback(null)
                }
            }
            .addOnFailureListener {
                callback.onCallback(null)
            }
    }
}
