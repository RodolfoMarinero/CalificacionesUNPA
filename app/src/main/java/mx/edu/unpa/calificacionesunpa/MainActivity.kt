package mx.edu.unpa.calificacionesunpa

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import mx.edu.unpa.calificacionesunpa.data.persistent.UsuarioService
import mx.edu.unpa.calificacionesunpa.databinding.ActivityMainBinding
import mx.edu.unpa.calificacionesunpa.ui.login.LoginActivity

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        // Forzar modo claro
        androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(
            androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
        )

        super.onCreate(savedInstanceState)

        // Usamos ViewBinding con tu nuevo activity_main.xml (el de los Tabs)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Solicitar permisos
        solicitarPermisoNotificaciones()

        // --- CONFIGURACIÓN DE PESTAÑAS (TABS) ---

        // 1. Conectar el ViewPager con el adaptador
        val pagerAdapter = MainPagerAdapter(this)
        binding.viewPager.adapter = pagerAdapter

        // Desactiva el swipe si prefieres que solo cambien al tocar la pestaña
        binding.viewPager.isUserInputEnabled = true

        // 2. Conectar TabLayout con ViewPager2
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            when (position) {
                0 -> {
//                    tab.text = "Calificaciones"
                    // Descomenta esta línea cuando tengas el icono en res/drawable/
                    tab.setIcon(R.drawable.ic_calificaciones)
                }
                1 -> {
//                    tab.text = "Calendario"
                    tab.setIcon(R.drawable.ic_event)
                }
                2 -> {
//                    tab.text = "Avisos"
                    tab.setIcon(R.drawable.ic_notifications)
                }
                3 -> {
//                    tab.text = "Perfil"
                    tab.setIcon(R.drawable.ic_perfil)
                }
                4 -> {
//                    tab.text = "Acerca De"
                    tab.setIcon(R.drawable.ic_assignment)
                }
            }
        }.attach()

        // 3. Manejar el intent extra por si venimos del Login directo a "calificaciones"
        val navigateTo = intent.getStringExtra("navigateTo")
        if (navigateTo == "calificaciones") {
            binding.viewPager.currentItem = 0
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

    // Método para cerrar sesión (Puedes llamarlo desde tu Fragmento de Perfil)
    fun logout() {
        Log.e("Ariel Main", "Entra a Logout")
        viewModel.exitSession()
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onStart() {
        super.onStart()

        // --- VALIDACIONES DE SEGURIDAD ---
        val alumno = UsuarioService.alumnoActual
        if (alumno != null) {
            if (alumno.usuario != null) {
                if (alumno.usuario!!.esPrimerAcceso) {
                    Toast.makeText(this, "Debes cambiar tu contraseña primero", Toast.LENGTH_LONG).show()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
            } else {
                Log.e("Ariel Main Activity", "No se tiene datos del usuario")
                Toast.makeText(this, "No se tiene datos del usuario", Toast.LENGTH_LONG).show()
                logout() // Lo regresamos al login limpio
            }
        } else {
            Log.e("Ariel Main Activity", "No se pudieron cargar tus datos")
            Toast.makeText(this, "No se pudieron cargar tus datos", Toast.LENGTH_LONG).show()
            logout() // Lo regresamos al login limpio
        }
    }
}