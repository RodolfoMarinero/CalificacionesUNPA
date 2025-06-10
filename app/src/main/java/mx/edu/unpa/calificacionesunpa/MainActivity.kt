package mx.edu.unpa.calificacionesunpa

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.NonNull
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.credentials.Credential
import androidx.credentials.CustomCredential
import androidx.navigation.ui.NavigationUI
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
//import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
//import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import mx.edu.unpa.calificacionesunpa.databinding.ActivityMainBinding
import mx.edu.unpa.calificacionesunpa.providers.AuthProvider
import mx.edu.unpa.calificacionesunpa.ui.calificacionesanteriores.FragmentCalificacionesAnteriores
import mx.edu.unpa.calificacionesunpa.ui.login.LoginActivity

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var authProvider: AuthProvider
    private lateinit var auth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        authProvider = AuthProvider()


        setSupportActionBar(binding.appBarMain.toolbar)
        supportActionBar?.title = "UNIVERSIDAD DEL PAPALOAPAN"

        val drawerLayout: DrawerLayout   = binding.drawerLayout
        val navView: NavigationView       = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)

        // ① Defino los destinos top‑level de mi Drawer
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home,
                R.id.nav_calificaciones_anteriores,
                R.id.nav_notificaciones
            ),
            drawerLayout
        )

        // ② Conecto la Toolbar con NavController + AppBarConfig
        setupActionBarWithNavController(navController, appBarConfiguration)

        // Cambiar color del ícono hamburguesa a negro
        binding.appBarMain.toolbar.navigationIcon?.setTint(resources.getColor(android.R.color.black, theme))

        // ③ Conecto el NavigationView con NavController
        navView.setupWithNavController(navController)

        // ④ Manejo “Cerrar sesión” manualmente
        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_logout -> {
                    logout()
                    true
                }
                else -> {
                    // Para el resto de items, que NavController haga la navegación
                    NavigationUI.onNavDestinationSelected(menuItem, navController)
                    drawerLayout.closeDrawers()
                    true
                }
            }
        }

        auth = Firebase.auth
    }

    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        updateUI(currentUser)
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.nav_logout -> {
                // Aquí implementamos el cierre de sesión manualmente
                logout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // Método para el cierre de sesión
    private fun logout() {
        // Limpia cualquier sesión guardada o SharedPreferences aquí si es necesario
        authProvider.exitSession()
        // Redirige a la actividad de Login
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish() // Finaliza la actividad actual para no poder volver atrás
    }
    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            // Usuario autenticado
            Log.d("Auth", "Usuario: ${user.displayName}")
            // Puedes redirigir al home o mostrar info del usuario
        } else {
            // Usuario no autenticado
            Log.d("Auth", "No hay sesión activa")
            // Mostrar botón de login, etc.
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    updateUI(null)
                }
            }
    }
}