package pt.ua.deti.icm.awav.data

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
import pt.ua.deti.icm.awav.R
import java.lang.ref.WeakReference
import pt.ua.deti.icm.awav.AWAVApplication

class GoogleAuthHelper(private val context: Context) {
    
    private val credentialManager = CredentialManager.create(context)
    private val auth = FirebaseAuth.getInstance()
    
    // For legacy authentication on Android 12
    private var googleSignInClient: GoogleSignInClient? = null
    private var pendingLegacyCallback: ((FirebaseUser?, Exception?) -> Unit)? = null
    
    // Store activity launchers by activity instance
    private val activityLaunchers = mutableMapOf<ComponentActivity, WeakReference<ActivityResultLauncher<Intent>>>()
    
    companion object {
        private const val TAG = "GoogleAuthHelper"
        private const val TYPE_GOOGLE_ID_TOKEN_CREDENTIAL = "com.google.android.libraries.identity.googleid.GOOGLE_ID_TOKEN_CREDENTIAL"
        // Alternative credential type that might be returned
        private const val TYPE_GOOGLE_ID_TOKEN_ALT = "com.google.android.gms.auth.api.identity.GOOGLE_ID_TOKEN"
        private const val LEGACY_AUTH_THRESHOLD = android.os.Build.VERSION_CODES.S // Android 12
    }
    
    /**
     * Main entry point for Google Sign-in
     */
    suspend fun signInWithGoogle(onSuccess: (FirebaseUser) -> Unit, onFailure: (Exception) -> Unit) {
        // If Google Play Services are disabled, fail early
        if (!AWAVApplication.googlePlayServicesAvailable) {
            Log.w(TAG, "Google Play Services are disabled on this device")
            onFailure(Exception("Google Sign-In is not available on this device"))
            return
        }
        
        try {
            // Get the client ID
            val clientId = getServerClientId(context)
            Log.d(TAG, "Using client ID: $clientId")
            
            if (clientId.isEmpty()) {
                Log.e(TAG, "Web client ID is empty")
                onFailure(Exception("Web client ID is empty or not configured"))
                return
            }
            
            // For Android 12 or below, we need to use the activity directly
            // This method will be called through signInWithGoogleFromActivity
            if (android.os.Build.VERSION.SDK_INT <= LEGACY_AUTH_THRESHOLD) {
                // Store the callbacks for later use when the activity calls us
                pendingLegacyCallback = { user, error ->
                    if (user != null) onSuccess(user)
                    else if (error != null) onFailure(error)
                    else onFailure(Exception("Unknown error during legacy sign-in"))
                }
                
                // Since we're not in an Activity context, we need to inform the caller
                onFailure(Exception("For Android 12 devices, please use signInWithGoogleFromActivity instead"))
                return
            }
            
            // Modern authentication path for Android 13+
            Log.d(TAG, "Using modern authentication for Android 13+")
            
            // Create Google Sign-in request
            val googleIdOption = GetGoogleIdOption.Builder()
                .setServerClientId(clientId)
                .setFilterByAuthorizedAccounts(false)
                .setAutoSelectEnabled(true)
                .setNonce("nonce")
                .build()
                
            // Create credential request
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()
            
            Log.d(TAG, "Requesting credentials...")
            
            // Request credentials
            try {
                val result = credentialManager.getCredential(context, request)
                Log.d(TAG, "Got credential result")
                
                // Process credential
                handleCredentialResponse(result, onSuccess, onFailure)
            } catch (e: Exception) {
                Log.e(TAG, "Error getting credential: ${e.message}", e)
                
                when (e) {
                    is NoCredentialException -> onFailure(Exception("No Google accounts available"))
                    is GetCredentialException -> onFailure(e)
                    else -> onFailure(e)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Critical error in signInWithGoogle: ${e.message}", e)
            onFailure(e)
        }
    }
    
    /**
     * This method should be called directly from UI components for Android 12 devices
     */
    fun signInWithGoogleFromActivity(activity: ComponentActivity) {
        // If Google Play Services are disabled, fail early
        if (!AWAVApplication.googlePlayServicesAvailable) {
            Log.w(TAG, "Google Play Services are disabled on this device")
            handleLegacyFailure(Exception("Google Sign-In is not available on this device"))
            return
        }
        
        try {
            // Log what we're doing
            Log.d(TAG, "Starting signInWithGoogleFromActivity with ${activity.javaClass.simpleName}")
            
            // Register the activity result launcher if not already registered
            val launcher = getOrCreateLauncherSimple(activity)
            
            val clientId = getServerClientId(context)
            Log.d(TAG, "Got client ID: ${clientId.take(10)}... (truncated for security)")
            
            if (clientId.isEmpty()) {
                Log.e(TAG, "Web client ID is empty - failing legacy auth")
                handleLegacyFailure(Exception("Web client ID is empty or not configured"))
                return
            }
            
            // Initialize GoogleSignInClient
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(clientId)
                .requestEmail()
                .build()
                
            try {
                googleSignInClient = GoogleSignIn.getClient(activity, gso)
                Log.d(TAG, "Created GoogleSignInClient: ${googleSignInClient?.hashCode()}")
            } catch (e: SecurityException) {
                Log.e(TAG, "Security exception in Google Sign-In, disabling Google features: ${e.message}", e)
                AWAVApplication.disableGooglePlayServices()
                handleLegacyFailure(Exception("Unable to authenticate with Google on this device"))
                return
            }
            
            // Add timeout to prevent indefinite loading
            activity.window?.decorView?.postDelayed({
                if (pendingLegacyCallback != null) {
                    Log.e(TAG, "Google Sign-in timed out after 30 seconds")
                    handleLegacyFailure(Exception("Google Sign-in timed out. Please try again."))
                }
            }, 30000) // 30 second timeout
            
            // Use the existing account if available
            val account = GoogleSignIn.getLastSignedInAccount(activity)
            if (account != null && account.idToken != null) {
                // User is already signed in with Google
                Log.d(TAG, "User already signed in with Google, using existing account")
                handleGoogleSignInResult(account)
                return
            }
            
            // Launch the sign-in flow
            val signInIntent = googleSignInClient?.signInIntent
            if (signInIntent != null) {
                Log.d(TAG, "About to launch sign-in intent with launcher: ${launcher.hashCode()}")
                launcher.launch(signInIntent)
                Log.d(TAG, "Launched legacy Google sign-in intent")
            } else {
                Log.e(TAG, "Failed to create sign-in intent")
                handleLegacyFailure(Exception("Failed to create sign-in intent"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in legacy sign-in: ${e.message}", e)
            e.printStackTrace() // Print full stack trace
            handleLegacyFailure(e)
        }
    }
    
    /**
     * Simplified launcher creation method to avoid lifecycle issues
     */
    private fun getOrCreateLauncherSimple(activity: ComponentActivity): ActivityResultLauncher<Intent> {
        try {
            // Clean up any stale references first
            activityLaunchers.entries.removeIf { entry -> entry.value.get() == null }
            
            // Check if we already have a launcher for this activity
            val existingLauncher = activityLaunchers[activity]?.get()
            if (existingLauncher != null) {
                Log.d(TAG, "Using existing launcher for ${activity.javaClass.simpleName}: ${existingLauncher.hashCode()}")
                return existingLauncher
            }
            
            Log.d(TAG, "Creating new direct launcher for ${activity.javaClass.simpleName}")
            
            // Create a direct launcher with a simple callback
            val resultCallback = androidx.activity.result.ActivityResultCallback<androidx.activity.result.ActivityResult> { result ->
                Log.d(TAG, "Activity result received: resultCode=${result.resultCode}")
                try {
                    if (result.data == null) {
                        Log.e(TAG, "Google sign-in returned null data - user likely canceled")
                        handleLegacyFailure(Exception("Sign-in was canceled"))
                        return@ActivityResultCallback
                    }
                    
                    val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                    try {
                        val account = task.getResult(ApiException::class.java)
                        Log.d(TAG, "Successfully got account from intent")
                        handleGoogleSignInResult(account)
                    } catch (e: ApiException) {
                        Log.e(TAG, "Google sign-in failed with status code: ${e.statusCode}", e)
                        handleLegacyFailure(Exception("Google sign-in failed with code: ${e.statusCode}"))
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Unexpected error in sign-in result: ${e.message}", e)
                    handleLegacyFailure(e)
                }
            }
            
            // Use the contract directly
            val contract = ActivityResultContracts.StartActivityForResult()
            
            // Register for activity result
            val launcher = activity.registerForActivityResult(contract, resultCallback)
            
            Log.d(TAG, "Successfully registered new direct launcher: ${launcher.hashCode()}")
            
            // Save the launcher with a weak reference
            activityLaunchers[activity] = WeakReference(launcher)
            
            return launcher
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create direct launcher: ${e.message}", e)
            e.printStackTrace()
            Toast.makeText(context, "Failed to create Google Sign-in launcher", Toast.LENGTH_LONG).show()
            
            // Return a simple dummy launcher that will report the error
            return object : ActivityResultLauncher<Intent>() {
                override fun launch(input: Intent, options: androidx.core.app.ActivityOptionsCompat?) {
                    handleLegacyFailure(Exception("Could not create sign-in launcher"))
                }
                
                override fun unregister() {}
                
                override val contract: androidx.activity.result.contract.ActivityResultContract<Intent, androidx.activity.result.ActivityResult> = 
                    ActivityResultContracts.StartActivityForResult()
            }
        }
    }
    
    /**
     * Original launcher creation method - no longer used but kept for reference
     */
    private fun getOrCreateLauncher(activity: ComponentActivity): ActivityResultLauncher<Intent> {
        // Check if we already have a launcher for this activity
        val existingLauncher = activityLaunchers[activity]?.get()
        if (existingLauncher != null) {
            Log.d(TAG, "Using existing launcher for ${activity.javaClass.simpleName}: ${existingLauncher.hashCode()}")
            return existingLauncher
        }
        
        Log.d(TAG, "Creating new launcher for ${activity.javaClass.simpleName}")
        
        // Clean up any stale references
        activityLaunchers.entries.removeIf { entry -> entry.value.get() == null }
        
        try {
            // Register directly on the activity
            val launcher = activity.registerForActivityResult(
                ActivityResultContracts.StartActivityForResult()
            ) { result ->
                Log.d(TAG, "Activity result received: resultCode=${result.resultCode}")
                try {
                    if (result.data == null) {
                        Log.e(TAG, "Google sign-in returned null data - user likely canceled")
                        handleLegacyFailure(Exception("Sign-in was canceled"))
                        return@registerForActivityResult
                    }
                    
                    val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                    try {
                        val account = task.getResult(ApiException::class.java)
                        Log.d(TAG, "Successfully got account from intent")
                        handleGoogleSignInResult(account)
                    } catch (e: ApiException) {
                        Log.e(TAG, "Google sign-in failed with status code: ${e.statusCode}", e)
                        handleLegacyFailure(Exception("Google sign-in failed with code: ${e.statusCode}"))
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Unexpected error in sign-in result: ${e.message}", e)
                    handleLegacyFailure(e)
                }
            }
            
            Log.d(TAG, "Successfully registered new launcher: ${launcher.hashCode()}")
            
            // Save the launcher with a weak reference to avoid memory leaks
            activityLaunchers[activity] = WeakReference(launcher)
            
            return launcher
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create launcher: ${e.message}", e)
            Toast.makeText(context, "Failed to create Google Sign-in launcher: ${e.message}", Toast.LENGTH_LONG).show()
            
            // Return a dummy launcher that will immediately fail when used
            return object : ActivityResultLauncher<Intent>() {
                override fun launch(input: Intent, options: androidx.core.app.ActivityOptionsCompat?) {
                    Log.e(TAG, "Using dummy launcher that immediately fails")
                    handleLegacyFailure(Exception("Failed to create activity launcher: ${e.message}"))
                }
                
                override fun unregister() {
                    // Do nothing
                }
                
                override val contract: androidx.activity.result.contract.ActivityResultContract<Intent, androidx.activity.result.ActivityResult> = 
                    ActivityResultContracts.StartActivityForResult()
            }
        }
    }
    
    /**
     * Handle Google sign-in result for legacy authentication
     */
    private fun handleGoogleSignInResult(account: GoogleSignInAccount) {
        Log.d(TAG, "Legacy Google sign-in successful, authenticating with Firebase")
        
        // Launch coroutine to authenticate with Firebase
        val idToken = account.idToken
        if (idToken != null) {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            auth.signInWithCredential(credential)
                .addOnSuccessListener { authResult ->
                    val user = authResult.user
                    if (user != null) {
                        Log.d(TAG, "Firebase authentication successful with legacy method")
                        
                        // Callback to original caller
                        pendingLegacyCallback?.invoke(user, null)
                        pendingLegacyCallback = null
                    } else {
                        Log.e(TAG, "Firebase user is null after legacy authentication")
                        handleLegacyFailure(Exception("Firebase user is null"))
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Firebase authentication failed with legacy method: ${e.message}")
                    handleLegacyFailure(e)
                }
        } else {
            Log.e(TAG, "ID token is null in Google sign-in account")
            handleLegacyFailure(Exception("ID token is null"))
        }
    }
    
    /**
     * Helper method to handle failures in legacy flow
     */
    private fun handleLegacyFailure(e: Exception) {
        Log.e(TAG, "Handling legacy failure: ${e.message}")
        
        // Call the callback if it exists
        pendingLegacyCallback?.invoke(null, e) 
        pendingLegacyCallback = null
        
        // Also try to reset loading state in the repository
        try {
            // Use reflection-free approach to reset loading state
            AuthRepository.resetLoadingState()
        } catch (ex: Exception) {
            Log.e(TAG, "Error resetting repository loading state: ${ex.message}")
        }
    }
    
    private suspend fun handleCredentialResponse(
        result: GetCredentialResponse,
        onSuccess: (FirebaseUser) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val credential = result.credential
        
        try {
            // First try handling as CustomCredential
            if (credential is CustomCredential) {
                val type = credential.type
                Log.d(TAG, "Processing CustomCredential of type: $type")
                
                if (type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL || type == TYPE_GOOGLE_ID_TOKEN_ALT || type.contains("google", ignoreCase = true)) {
                    try {
                        // Parse the credential
                        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                        val idToken = googleIdTokenCredential.idToken
                        Log.d(TAG, "Successfully parsed Google ID token")
                        
                        // Use the ID token to sign in with Firebase
                        firebaseAuthWithGoogle(idToken, onSuccess, onFailure)
                        return
                    } catch (e: GoogleIdTokenParsingException) {
                        Log.e(TAG, "Error parsing Google ID token: ${e.message}", e)
                        onFailure(e)
                        return
                    }
                }
                
                // If we get here, it's an unsupported custom credential type
                Log.e(TAG, "Unsupported CustomCredential type: $type")
                onFailure(Exception("Unsupported credential type: $type"))
            } else {
                // Try to extract a token from the credential using reflection
                try {
                    Log.d(TAG, "Attempting to extract ID token using reflection")
                    val credentialClass = credential.javaClass
                    val methods = credentialClass.methods
                    
                    // Look for methods that might return the ID token
                    for (method in methods) {
                        if (method.name.contains("getId", ignoreCase = true) || 
                            method.name.contains("getToken", ignoreCase = true) ||
                            method.name.contains("credential", ignoreCase = true)) {
                            if (method.parameterCount == 0) {
                                val result = method.invoke(credential)
                                if (result is String && result.isNotEmpty()) {
                                    Log.d(TAG, "Found ID token using method: ${method.name}")
                                    firebaseAuthWithGoogle(result, onSuccess, onFailure)
                                    return
                                }
                            }
                        }
                    }
                    
                    Log.e(TAG, "Could not extract ID token via reflection")
                } catch (e: Exception) {
                    Log.e(TAG, "Error during reflection: ${e.message}", e)
                }
                
                onFailure(Exception("Received credential is not a supported type: ${credential.javaClass.name}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error handling credential: ${e.message}", e)
            onFailure(e)
        }
    }
    
    private suspend fun firebaseAuthWithGoogle(
        idToken: String,
        onSuccess: (FirebaseUser) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        try {
            Log.d(TAG, "Authenticating with Firebase using Google token")
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = auth.signInWithCredential(credential).await()
            
            authResult.user?.let { user ->
                Log.d(TAG, "Firebase authentication successful. User: ${user.email}")
                onSuccess(user)
            } ?: run {
                Log.e(TAG, "Firebase user is null after authentication")
                onFailure(Exception("Firebase user is null"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Firebase authentication failed: ${e.message}", e)
            onFailure(e)
        }
    }
    
    /**
     * Get the server client ID from strings.xml
     */
    private fun getServerClientId(context: Context): String {
        try {
            // Direct approach: use our configured ID first
            val configuredId = context.getString(R.string.default_web_client_id)
            Log.d(TAG, "Configured web client ID: $configuredId")
            
            if (configuredId.isNotEmpty() && !configuredId.contains("YOUR_WEB_CLIENT_ID")) {
                return configuredId
            }
            
            // Fallback to auto-generated client ID from Google Services
            val resourceId = context.resources.getIdentifier(
                "default_web_client_id", 
                "string", 
                context.packageName
            )
            
            if (resourceId != 0) {
                val autogeneratedId = context.getString(resourceId)
                Log.d(TAG, "Auto-generated web client ID: $autogeneratedId")
                return autogeneratedId
            }
            
            Log.e(TAG, "No valid web client ID found")
            return ""
        } catch (e: Exception) {
            Log.e(TAG, "Error getting web client ID: ${e.message}", e)
            return ""
        }
    }
    
    /**
     * Fallback method when Google Play Services has security issues
     * This provides a way to continue using profile features even when Google auth fails
     */
    fun handleGooglePlaySecurityIssue() {
        // Reset any pending callbacks
        pendingLegacyCallback = null
        
        // Reset loading state in auth repository
        AuthRepository.resetLoadingState()
        
        // Log the error
        Log.e(TAG, "Using fallback due to Google Play Services security exception")
        
        // Show toast to user explaining the issue
        Toast.makeText(
            context, 
            "Google Services authentication unavailable. Some features may be limited.", 
            Toast.LENGTH_LONG
        ).show()
    }
} 