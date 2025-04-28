package mx.edu.unpa.calificacionesunpa

import android.content.Intent
import android.os.Bundle
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
import androidx.navigation.ui.NavigationUI
import mx.edu.unpa.calificacionesunpa.databinding.ActivityMainBinding
import mx.edu.unpa.calificacionesunpa.providers.AuthProvider
import mx.edu.unpa.calificacionesunpa.ui.calificacionesanteriores.FragmentCalificacionesAnteriores
import mx.edu.unpa.calificacionesunpa.ui.login.LoginActivity

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var authProvider: AuthProvider

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

        // Navegación desde Login
        intent.getStringExtra("navigateTo")?.let { destino ->
            if (destino == "calificaciones") {
                navController.navigate(R.id.nav_calificaciones_anteriores)
            }
        }

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


//    override fun onStart(){
//        super.onStart()
//        if (authProvider.exitsSession()){
//            val intent = Intent(this, FragmentCalificacionesAnteriores::class.java)
//            startActivity(intent)
//        }
//    }
}