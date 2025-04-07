package pt.ua.deti.icm.awav

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import com.google.firebase.FirebaseApp
import pt.ua.deti.icm.awav.data.AppContainer
import pt.ua.deti.icm.awav.data.AppDataContainer
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.storage.FirebaseStorage
import pt.ua.deti.icm.awav.utils.GoogleServicesHelper
import pt.ua.deti.icm.awav.data.AuthRepository

class AWAVApplication : Application() {

    lateinit var appContainer: AppContainer
        private set

    companion object {
        private const val TAG = "AWAVApplication"

        private lateinit var instance: AWAVApplication

        val appContainer: AppContainer
            get() = instance.appContainer

        lateinit var appContext: Context
            private set

        private var _googlePlayServicesAvailable: Boolean = true

        // Public property for checking if Google Play Services are available
        val googlePlayServicesAvailable: Boolean
            get() = _googlePlayServicesAvailable

        // Method to disable Google Play Services features
        fun disableGooglePlayServices() {
            if (_googlePlayServicesAvailable) {
                _googlePlayServicesAvailable = false
                Log.w(TAG, "Google Play Services have been disabled due to compatibility issues")
            }
        }
    }
    
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

        appContext = applicationContext

        try {
            // Initialize Google Play Services first - this handles security providers
            _googlePlayServicesAvailable = GoogleServicesHelper.init(this)

            // Initialize Firebase
            if (!FirebaseApp.getApps(this).isEmpty()) {
                Log.d(TAG, "Firebase already initialized")
            } else {
                FirebaseApp.initializeApp(this)
                Log.d(TAG, "Firebase initialized")
            }

            // Initialize Firebase App Check for better security
            try {
                val firebaseAppCheck = FirebaseAppCheck.getInstance()
                firebaseAppCheck.installAppCheckProviderFactory(
                    PlayIntegrityAppCheckProviderFactory.getInstance()
                )
                Log.d(TAG, "Firebase App Check initialized")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize Firebase App Check", e)
                // Continue without App Check - it's not critical
            }

            // Initialize Firebase Storage and check if it's working
            if (_googlePlayServicesAvailable) {
                try {
                    val storage = FirebaseStorage.getInstance()
                    val reference = storage.reference
                    Log.d(TAG, "Firebase Storage initialized with bucket: ${reference.bucket}")

                    // Try to access profile_images folder to make sure it exists
                    val profileImagesRef = reference.child("profile_images")
                    Log.d(TAG, "Profile images path: ${profileImagesRef.path}")

                } catch (e: Exception) {
                    Log.e(TAG, "Error initializing Firebase Storage", e)
                    disableGooglePlayServices()
                }
            } else {
                Log.d(TAG, "Skipping Firebase Storage initialization as Google Play Services are disabled")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error initializing app", e)
            // Try to continue without Google Play Services if possible
            disableGooglePlayServices()
        }
        
        // Print important information for debug purposes
        Log.i(TAG, "Application initialized - Google Play Services available: $_googlePlayServicesAvailable")
        if (_googlePlayServicesAvailable) {
            Log.i(TAG, "SHA-1 debug fingerprint: ${GoogleServicesHelper.getDebugFingerprint()}")
            Log.i(TAG, "Make sure this fingerprint is added to your Firebase project!")
        }
        
        // Initialize auth repository to ensure user roles and active role are loaded at app startup
        try {
            val authRepo = AuthRepository(this)
            Log.d(TAG, "Auth repository initialized at application startup")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing auth repository at startup", e)
        }
    }
}