package pe.edu.upeu.pharmamobile

import android.app.Application
import androidx.activity.ComponentActivity
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.dsl.module
import pe.edu.upeu.pharmamobile.di.initKoin
import pe.edu.upeu.pharmamobile.domain.permiso.GestorPermisos
import pe.edu.upeu.pharmamobile.permiso.GestorPermisosAndroid

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidLogger()
            androidContext(this@MainApplication)
            if (BuildConfig.PHARMASOFT_BASE_URL.isNotEmpty()) {
                properties(mapOf("pharmaSoftBaseUrl" to BuildConfig.PHARMASOFT_BASE_URL))
            }
            modules(module {
                factory<GestorPermisos> { (actividad: ComponentActivity) -> GestorPermisosAndroid(actividad) }
            })
        }
    }
}
