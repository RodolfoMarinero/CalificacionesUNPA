package mx.edu.unpa.calificacionesunpa.providers

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import mx.edu.unpa.calificacionesunpa.models.Student
import com.google.firebase.firestore.FirebaseFirestore



class AuthProvider {
    val auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun register(email: String, pass: String): Task<AuthResult>{
        return auth.createUserWithEmailAndPassword(email,pass)
    }

    fun login(email: String, pass: String): Task<AuthResult>{
        return auth.signInWithEmailAndPassword(email,pass)
    }

    fun getId(): String{
        return auth.currentUser?.uid ?: ""
    }

    fun exitsSession(): Boolean{
        var exits = false
        if (auth.currentUser!=null){
            exits=true
        }
        return exits
    }

    fun exitSession(){
        auth.signOut()
    }


}