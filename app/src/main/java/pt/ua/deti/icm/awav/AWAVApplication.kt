package pt.ua.deti.icm.awav

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
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
import com.google.firebase.messaging.FirebaseMessaging
import pt.ua.deti.icm.awav.utils.FCMUtils
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import pt.ua.deti.icm.awav.BuildConfig

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

        const val CHANNEL_ID = "channel_awav_default"
    }
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        
        // Log FCM token for debugging
        FCMUtils.getFCMToken { token ->
            if (token != null) {
                Log.i(TAG, "✅ FCM TOKEN FOR THIS DEVICE: $token")
                Log.i(TAG, "✅ Use this token for testing notifications")
            } else {
                Log.e(TAG, "❌ Failed to retrieve FCM token")
            }
        }

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

            // Initialize Firebase App Check with error handling
            try {
                val firebaseAppCheck = FirebaseAppCheck.getInstance()
                
                // Use Debug provider in debug builds
                if (BuildConfig.DEBUG) {
                    firebaseAppCheck.installAppCheckProviderFactory(
                        DebugAppCheckProviderFactory.getInstance()
                    )
                    Log.d(TAG, "Firebase App Check initialized with Debug provider")
                } else {
                    firebaseAppCheck.installAppCheckProviderFactory(
                        PlayIntegrityAppCheckProviderFactory.getInstance()
                    )
                    Log.d(TAG, "Firebase App Check initialized with Play Integrity provider")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing App Check: ${e.message}", e)
                // Continue without App Check if there's an error
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

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "AWAV Default Channel"
            val descriptionText = "Default notification channel for AWAV app"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            
            val notificationManager: NotificationManager = 
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    // Function to get the FCM token
    fun getFCMToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCMToken", "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }

            // Get the token
            val token = task.result
            Log.d("FCMToken", "Current FCM Token: $token")
        }
    }
}