package pt.ua.deti.icm.awav

import android.app.Application
import com.google.firebase.FirebaseApp

class awavApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}