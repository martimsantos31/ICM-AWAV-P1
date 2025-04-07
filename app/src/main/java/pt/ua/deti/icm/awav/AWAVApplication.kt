package pt.ua.deti.icm.awav

import android.app.Application
import android.util.Log
import androidx.credentials.CredentialManager
import com.google.firebase.FirebaseApp
import pt.ua.deti.icm.awav.data.AppContainer
import pt.ua.deti.icm.awav.data.AppDataContainer

class AWAVApplication : Application() {
    lateinit var appContainer: AppContainer
        private set
    
    override fun onCreate() {
        super.onCreate()

        FirebaseApp.initializeApp(this)
        Log.d(TAG, "Firebase initialized successfully")

        try {
            val credentialManager = CredentialManager.create(this)
            Log.d(TAG, "CredentialManager initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing CredentialManager: ${e.message}", e)
        }
        
        Log.d(TAG, "Application initialization completed")
        appContainer = AppDataContainer(this)

        instance = this
    }
    
    companion object {
        private const val TAG = "AWAVApplication"
        private lateinit var instance: AWAVApplication

        val appContainer: AppContainer
            get() = instance.appContainer
    }
}