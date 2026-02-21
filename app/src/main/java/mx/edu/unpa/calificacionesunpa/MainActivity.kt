package mx.edu.unpa.calificacionesunpa

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import dagger.hilt.android.AndroidEntryPoint
import mx.edu.unpa.calificacionesunpa.data.persistent.UsuarioService
import mx.edu.unpa.calificacionesunpa.databinding.ActivityMainBinding
import mx.edu.unpa.calificacionesunpa.ui.login.LoginActivity


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val viewModel: MainViewModel by viewModels()
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(
            androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
        )

        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.appBarMain.toolbar)
        supportActionBar?.title = "UNIVERSIDAD DEL PAPALOAPAN"

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        solicitarPermisoNotificaciones()

        // Destinos
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home,
                R.id.nav_calificaciones_anteriores,
                R.id.nav_notificaciones,
                R.id.nav_calendarioExamen
            ),
            drawerLayout
        )

        // Conectar la Toolbar con NavController + AppBarConfig
        setupActionBarWithNavController(navController, appBarConfiguration)

        // Cambiar color del ícono hamburguesa a negro
        binding.appBarMain.toolbar.navigationIcon?.setTint(
            resources.getColor(
                android.R.color.black,
                theme
            )
        )

        // Conectar NavigationView con NavController
        navView.setupWithNavController(navController)

        // “Cerrar sesión” manualmente
        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_logout -> {
                    logout()
                    true
                }

                else -> {
                    NavigationUI.onNavDestinationSelected(menuItem, navController)
                    drawerLayout.closeDrawers()
                    true
                }
            }
        }

    }

    private fun solicitarPermisoNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1001)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
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
                Log.e("Ariel Main", "Entra a Logout")
                logout()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun logout() {
        viewModel.exitSession()
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }


    override fun onStart() {
        super.onStart()
        val alumno = UsuarioService.alumnoActual
        if(alumno!=null) {
            if(alumno.usuario!=null) {
                if (alumno.usuario!!.esPrimerAcceso) {
                    Toast.makeText(this, "Debes cambiar tu contraseña primero", Toast.LENGTH_LONG)
                        .show()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
            }else{
                Log.e("Ariel Main Activity","No se tiene datos del usuario")
                Toast.makeText(this, "No se tiene datos del usuario", Toast.LENGTH_LONG)
                    .show()
                //deberiamos regresarlo al login
            }
        }else{
            Log.e("Ariel Main Activity","No se pudieron cargar tus datos ")
            Toast.makeText(this, "No se pudieron cargar tus datos ", Toast.LENGTH_LONG)
                .show()
            //deberiamos regresarlo al login
        }
    }

}