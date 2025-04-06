package pt.ua.deti.icm.awav.data

import android.content.Context
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
import pt.ua.deti.icm.awav.R

class GoogleAuthHelper(private val context: Context) {
    
    private val credentialManager = CredentialManager.create(context)
    private val auth = FirebaseAuth.getInstance()
    
    companion object {
        private const val TAG = "GoogleAuthHelper"
        private const val TYPE_GOOGLE_ID_TOKEN_CREDENTIAL = "com.google.android.libraries.identity.googleid.GOOGLE_ID_TOKEN_CREDENTIAL"
        // Alternative credential type that might be returned
        private const val TYPE_GOOGLE_ID_TOKEN_ALT = "com.google.android.gms.auth.api.identity.GOOGLE_ID_TOKEN"
    }
    
    suspend fun signInWithGoogle(onSuccess: (FirebaseUser) -> Unit, onFailure: (Exception) -> Unit) {
        try {
            // Get the client ID
            val clientId = getServerClientId(context)
            Log.d(TAG, "Using client ID: $clientId")
            
            if (clientId.isEmpty()) {
                Log.e(TAG, "Web client ID is empty")
                onFailure(Exception("Web client ID is empty or not configured"))
                return
            }
            
            // Create Google Sign-in request
            val googleIdOption = GetGoogleIdOption.Builder()
                .setServerClientId(clientId)
                .setFilterByAuthorizedAccounts(false)
                .setAutoSelectEnabled(true) // Changed to true to avoid the selector
                .setNonce("nonce")
                .build()
                
            // Create credential request
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()
            
            Log.d(TAG, "Requesting credentials...")
            
            // Request credentials
            val result = credentialManager.getCredential(context, request)
            
            // Full credential details - shows everything for debugging
            val credentialJson = result.credential.toString()
            Log.d(TAG, "Full credential details: $credentialJson")
            Log.d(TAG, "Credential class: ${result.credential.javaClass.name}")
            
            if (result.credential is CustomCredential) {
                val custom = result.credential as CustomCredential
                Log.d(TAG, "CustomCredential type: ${custom.type}")
            }
            
            handleCredentialResponse(result, onSuccess, onFailure)
        } catch (e: NoCredentialException) {
            Log.e(TAG, "No Google accounts available: ${e.message}")
            onFailure(Exception("No Google accounts available on this device. Please add a Google account in your device settings."))
        } catch (e: GetCredentialException) {
            Log.e(TAG, "Error getting credential: ${e.message}", e)
            onFailure(e)
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error during Google sign-in: ${e.message}", e)
            onFailure(e)
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
} 