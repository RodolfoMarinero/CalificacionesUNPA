package mx.edu.unpa.calificacionesunpa.data.di




import android.content.Context
import android.content.SharedPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import mx.edu.unpa.calificacionesunpa.data.api.StorageApi
import mx.edu.unpa.calificacionesunpa.data.api.alumno.AlumnoAPIretrofit
import mx.edu.unpa.calificacionesunpa.data.api.avisos.AvisoAPI
import mx.edu.unpa.calificacionesunpa.data.api.avisos.NotificacionAPI
import mx.edu.unpa.calificacionesunpa.data.api.calendarioescolar.CalendarioEscolarAPI
import mx.edu.unpa.calificacionesunpa.data.api.login.AuthAPI
import mx.edu.unpa.calificacionesunpa.data.api.login.AuthInterceptor
import mx.edu.unpa.calificacionesunpa.data.api.users.UsuarioAPI
import mx.edu.unpa.calificacionesunpa.data.repository.StorageRepository
import mx.edu.unpa.calificacionesunpa.data.service.StorageService
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

//    private const val BASE_URL = "http://201.144.254.11/vm2/"
    private const val BASE_URL = "http://192.168.1.71:8080/"
    // Retrofit global

    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    }

    @Provides
    @Singleton
    fun provideAuthInterceptor(prefs: SharedPreferences): AuthInterceptor {
        return AuthInterceptor(prefs)
    }
    @Provides
    fun provideOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient {

        // 1. Creamos el "espía" para ver los logs
        val logging = HttpLoggingInterceptor()
        // Le decimos que muestre TODO el contenido (Body)
        logging.setLevel(HttpLoggingInterceptor.Level.BODY)

        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor) // Tu token sigue aquí
            .addInterceptor(logging)         // <--- AGREGAMOS EL LOGGING AQUÍ
            // --- AGREGA ESTAS 3 LÍNEAS PARA EL TIEMPO ---
            .connectTimeout(60, TimeUnit.SECONDS) // 60 segundos para conectar
            .readTimeout(60, TimeUnit.SECONDS)    // 60 segundos esperando el PDF (La clave)
            .writeTimeout(60, TimeUnit.SECONDS)   // 60 segundos enviando datos
            // --------------------------------------------
            .build()
    }


    @Provides
    fun provideRetrofit(client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL) // backend local en emulador
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

   /* @Provides
    @Singleton
    fun provideRetrofit(): Retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()*/

    // ---- Storage ----
    @Provides
    fun provideCalendarioEscolarAPI(retrofit: Retrofit): CalendarioEscolarAPI =
        retrofit.create(CalendarioEscolarAPI::class.java)

    @Provides
    @Singleton
    fun provideAuthAPI(retrofit: Retrofit): AuthAPI =
        retrofit.create(AuthAPI::class.java)

    @Provides
    @Singleton
    fun provideNotificacionAPI(retrofit: Retrofit): NotificacionAPI =
        retrofit.create(NotificacionAPI::class.java)

    @Provides
    @Singleton
    fun provideAvisoApi(retrofit: Retrofit): AvisoAPI =
        retrofit.create(AvisoAPI::class.java)

    @Provides
    @Singleton
    fun provideUsuarioAPI(retrofit: Retrofit): UsuarioAPI =
        retrofit.create(UsuarioAPI::class.java)

    @Provides
    @Singleton
    fun provideAlumnoAPIretrofit(retrofit: Retrofit): AlumnoAPIretrofit =
        retrofit.create(AlumnoAPIretrofit::class.java)






    @Provides
    fun provideStorageRepository(service: StorageService): StorageRepository =
        StorageRepository(service)

    @Provides
    @Singleton
    fun provideStorageService(api: StorageApi): StorageService =
        StorageService(api)

    @Provides
    fun provideStorageApi(retrofit: Retrofit): StorageApi =
        retrofit.create(StorageApi::class.java)

    /*
    // ---- User ----
    @Provides
    fun provideUserApi(retrofit: Retrofit): UserApi =
        retrofit.create(UserApi::class.java)

    @Provides
    fun provideUserService(api: UserApi): UserService =
        UserService(api)

    @Provides
    fun provideUserRepository(service: UserService): UserRepository =
        UserRepository(service)

     */

    @Provides
    @Singleton
    fun provideReportesAPI(retrofit: Retrofit): mx.edu.unpa.calificacionesunpa.data.api.reportes.ReportesAPI =
        retrofit.create(mx.edu.unpa.calificacionesunpa.data.api.reportes.ReportesAPI::class.java)
}

