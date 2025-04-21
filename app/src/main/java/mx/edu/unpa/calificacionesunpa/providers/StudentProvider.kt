package mx.edu.unpa.calificacionesunpa.providers

import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import mx.edu.unpa.calificacionesunpa.models.Student

class StudentProvider {
    var db = Firebase.firestore.collection("Alumno")
    fun create(student: Student): Task<Void>{
        return db.document(student.matricula!!).set(student)
    }
}