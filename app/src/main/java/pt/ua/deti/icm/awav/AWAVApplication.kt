package pt.ua.deti.icm.awav

import android.app.Application
import android.util.Log
import androidx.credentials.CredentialManager
import com.google.firebase.FirebaseApp

class AWAVApplication : Application() {
    companion object {
        private const val TAG = "AWAVApplication"
    }
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        Log.d(TAG, "Firebase initialized successfully")
        
        // Initialize Credential Manager (for Google Sign-In)
        try {
            val credentialManager = CredentialManager.create(this)
            Log.d(TAG, "CredentialManager initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing CredentialManager: ${e.message}", e)
        }
        
        Log.d(TAG, "Application initialization completed")
    }
}