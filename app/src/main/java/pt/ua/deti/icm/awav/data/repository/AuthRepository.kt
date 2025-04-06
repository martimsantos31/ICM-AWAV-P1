package pt.ua.deti.icm.awav.data

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import pt.ua.deti.icm.awav.data.GoogleAuthHelper
import pt.ua.deti.icm.awav.data.model.UserRole
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.auth.UserProfileChangeRequest

class AuthRepository(private val context: Context) {
    
    private val auth: FirebaseAuth = Firebase.auth
    private val db = Firebase.firestore
    private val usersCollection = db.collection("users")
    private val googleAuthHelper = GoogleAuthHelper(context)

    // Current user state
    private val _currentUser = MutableStateFlow<FirebaseUser?>(null)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()
    
    // Available roles for the current user
    private val _userRoles = MutableStateFlow<List<UserRole>>(emptyList())
    val userRoles: StateFlow<List<UserRole>> = _userRoles.asStateFlow()
    
    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    init {
        // Initialize current user
        _currentUser.value = auth.currentUser
        if (auth.currentUser != null) {
            fetchUserRoles(auth.currentUser!!.email!!)
        }
        
        // Register this instance for static access
        INSTANCE = this
        Log.d(TAG, "AuthRepository initialized and registered for static access")
    }
    
    fun checkAuthState() {
        _currentUser.value = auth.currentUser
        if (auth.currentUser != null) {
            fetchUserRoles(auth.currentUser!!.email!!)
        } else {
            _userRoles.value = emptyList()
        }
    }
    
    fun fetchUserRoles(email: String, onComplete: ((List<UserRole>) -> Unit)? = null) {
        usersCollection.document(email)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val roles = document.get("roles") as? List<String> ?: emptyList()
                    val userRoles = roles.mapNotNull { roleName ->
                        try {
                            UserRole.valueOf(roleName)
                        } catch (e: IllegalArgumentException) {
                            null
                        }
                    }
                    _userRoles.value = userRoles
                    onComplete?.invoke(userRoles)
                } else {
                    _userRoles.value = emptyList()
                    onComplete?.invoke(emptyList())
                }
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error fetching user roles", e)
                _userRoles.value = emptyList()
                onComplete?.invoke(emptyList())
            }
    }
    
    fun addRoleToUser(email: String, role: UserRole, onComplete: (Boolean) -> Unit) {
        usersCollection.document(email)
            .get()
            .addOnSuccessListener { document ->
                val currentRoles = if (document.exists()) {
                    document.get("roles") as? List<String> ?: emptyList()
                } else {
                    emptyList()
                }
                
                // Create a new list with the added role if it doesn't exist
                val updatedRoles = if (role.name !in currentRoles) {
                    currentRoles + role.name
                } else {
                    currentRoles
                }
                
                // Save the updated roles
                usersCollection.document(email)
                    .set(mapOf("roles" to updatedRoles))
                    .addOnSuccessListener {
                        _userRoles.value = updatedRoles.mapNotNull { roleName ->
                            try {
                                UserRole.valueOf(roleName)
                            } catch (e: IllegalArgumentException) {
                                null
                            }
                        }
                        onComplete(true)
                    }
                    .addOnFailureListener { e ->
                        Log.w(TAG, "Error adding role to user", e)
                        onComplete(false)
                    }
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error fetching user for role update", e)
                onComplete(false)
            }
    }
    
    fun signUp(
        email: String, 
        password: String, 
        displayName: String,
        profilePicUri: Uri?,
        role: UserRole, 
        onComplete: (Boolean) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "createUserWithEmail:success")
                    val user = auth.currentUser
                    _currentUser.value = user
                    
                    // Create user document data with default values and role
                    val userData = hashMapOf(
                        "roles" to listOf(role.name),
                        "email" to email,
                        "displayName" to displayName,
                        "profileCreatedAt" to com.google.firebase.Timestamp.now()
                    )
                    
                    // Add the user document to Firestore
                    usersCollection.document(email)
                        .set(userData)
                        .addOnSuccessListener {
                            // Update the Firebase User profile with display name
                            updateUserProfile(displayName, profilePicUri) { profileUpdateSuccess ->
                                if (!profileUpdateSuccess) {
                                    Log.w(TAG, "Failed to update user profile")
                                    Toast.makeText(
                                        context,
                                        "Account created but failed to set profile details",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                onComplete(true)
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.w(TAG, "Error creating user document", e)
                            Toast.makeText(
                                context,
                                "Account created but failed to set user data",
                                Toast.LENGTH_SHORT
                            ).show()
                            onComplete(true) // Still consider signup successful
                        }
                } else {
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    Toast.makeText(
                        context,
                        "Registration failed: ${task.exception?.message}",
                        Toast.LENGTH_SHORT,
                    ).show()
                    onComplete(false)
                }
            }
    }
    
    private fun updateUserProfile(displayName: String, profilePicUri: Uri?, onComplete: (Boolean) -> Unit) {
        val user = auth.currentUser ?: return onComplete(false)
        
        if (profilePicUri != null) {
            // Upload image to Firebase Storage
            val storageRef = FirebaseStorage.getInstance().reference
            val profileImagesRef = storageRef.child("profile_images/${user.uid}.jpg")
            
            profileImagesRef.putFile(profilePicUri)
                .addOnSuccessListener {
                    // Get the download URL
                    profileImagesRef.downloadUrl
                        .addOnSuccessListener { downloadUri: Uri ->
                            // Update user profile with display name and photo URL
                            updateUserProfileData(user, displayName, downloadUri, onComplete)
                            
                            // Update the user document in Firestore with the photo URL
                            user.email?.let { email ->
                                usersCollection.document(email)
                                    .update("photoUrl", downloadUri.toString())
                                    .addOnFailureListener { e ->
                                        Log.w(TAG, "Error updating profile photo URL in Firestore", e)
                                    }
                            }
                        }
                        .addOnFailureListener { e: Exception ->
                            Log.w(TAG, "Error getting download URL for profile image", e)
                            // Still update profile with name only
                            updateUserProfileData(user, displayName, null, onComplete)
                        }
                }
                .addOnFailureListener { e: Exception ->
                    Log.w(TAG, "Error uploading profile image", e)
                    // Still update profile with name only
                    updateUserProfileData(user, displayName, null, onComplete)
                }
        } else {
            // Update user profile with display name only
            updateUserProfileData(user, displayName, null, onComplete)
        }
    }
    
    private fun updateUserProfileData(
        user: FirebaseUser, 
        displayName: String, 
        photoUri: Uri?,
        onComplete: (Boolean) -> Unit
    ) {
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(displayName)
            
        if (photoUri != null) {
            profileUpdates.setPhotoUri(photoUri)
        }
        
        user.updateProfile(profileUpdates.build())
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "User profile updated successfully")
                    // Update currentUser to reflect changes
                    _currentUser.value = auth.currentUser
                    onComplete(true)
                } else {
                    Log.w(TAG, "Failed to update user profile", task.exception)
                    onComplete(false)
                }
            }
    }
    
    fun signIn(email: String, password: String, onComplete: (Boolean) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithEmail:success")
                    _currentUser.value = auth.currentUser
                    
                    // Fetch user roles
                    fetchUserRoles(email) { roles ->
                        if (roles.isEmpty()) {
                            Toast.makeText(
                                context,
                                "Logged in but no roles found for this account",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        onComplete(true)
                    }
                } else {
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    Toast.makeText(
                        context,
                        "Authentication failed: ${task.exception?.message}",
                        Toast.LENGTH_SHORT,
                    ).show()
                    onComplete(false)
                }
            }
    }
    
    /**
     * Sign in with Google
     */
    suspend fun signInWithGoogle(
        activity: ComponentActivity? = null,
        role: UserRole? = null, 
        onComplete: (Boolean, String?) -> Unit
    ) {
        _isLoading.value = true
        
        try {
            // Create a helper with the appropriate context
            val helper = if (activity != null) {
                GoogleAuthHelper(activity)
            } else {
                googleAuthHelper
            }
            
            // For Android 12 devices, we need to use the activity directly
            if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.S && activity != null) {
                Log.d(TAG, "Using direct activity approach for Android 12")
                
                // Set up legacy callback to handle the result later
                googleAuthLegacyCallback = { success, errorMsg ->
                    _isLoading.value = false
                    onComplete(success, errorMsg)
                }
                
                // Store the role for later use after successful auth
                pendingGoogleRole = role
                
                // Launch the Google Sign-in UI directly from the activity
                helper.signInWithGoogleFromActivity(activity)
                return
            }
            
            // For Android 13+, use the suspend function
            helper.signInWithGoogle(
                onSuccess = { user ->
                    handleGoogleSignInSuccess(user, role, onComplete)
                },
                onFailure = { e ->
                    Log.e(TAG, "Google sign-in failed", e)
                    val errorMsg = "Google Sign-in failed: ${e.message}"
                    Toast.makeText(
                        context,
                        errorMsg,
                        Toast.LENGTH_SHORT
                    ).show()
                    _isLoading.value = false
                    onComplete(false, errorMsg)
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error during Google sign-in", e)
            val errorMsg = "Unexpected error: ${e.message}"
            Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
            _isLoading.value = false
            onComplete(false, errorMsg)
        }
    }
    
    // Temporary storage for callbacks and role during legacy auth
    private var googleAuthLegacyCallback: ((Boolean, String?) -> Unit)? = null
    private var pendingGoogleRole: UserRole? = null
    
    /**
     * This method should be called from GoogleAuthHelper when legacy auth completes
     */
    fun onLegacyGoogleSignInComplete(user: FirebaseUser) {
        val role = pendingGoogleRole
        val callback = googleAuthLegacyCallback
        
        pendingGoogleRole = null
        googleAuthLegacyCallback = null
        
        if (callback != null) {
            handleGoogleSignInSuccess(user, role, callback)
        }
    }
    
    /**
     * Shared logic for handling successful Google sign-in
     */
    private fun handleGoogleSignInSuccess(
        user: FirebaseUser,
        role: UserRole?,
        onComplete: (Boolean, String?) -> Unit
    ) {
        _currentUser.value = user
        
        // If a role is provided, add it to the user document
        user.email?.let { email: String ->
            if (role != null) {
                // Add the selected role
                addRoleToUser(email, role) { success ->
                    if (!success) {
                        val errorMsg = "Signed in with Google but failed to assign role"
                        Toast.makeText(
                            context,
                            errorMsg,
                            Toast.LENGTH_SHORT
                        ).show()
                        _isLoading.value = false
                        onComplete(true, errorMsg)
                    } else {
                        _isLoading.value = false
                        onComplete(true, null)
                    }
                }
            } else {
                // Just fetch existing roles
                fetchUserRoles(email) { roles ->
                    if (roles.isEmpty()) {
                        val errorMsg = "Signed in but no roles found for this account"
                        Toast.makeText(
                            context,
                            errorMsg,
                            Toast.LENGTH_SHORT
                        ).show()
                        _isLoading.value = false
                        onComplete(true, errorMsg)
                    } else {
                        _isLoading.value = false
                        onComplete(true, null)
                    }
                }
            }
        } ?: run {
            // Handle case where user has no email
            val errorMsg = "Google Sign-in successful but no email found"
            Toast.makeText(
                context,
                errorMsg,
                Toast.LENGTH_SHORT
            ).show()
            _isLoading.value = false
            onComplete(false, errorMsg)
        }
    }
    
    fun signOut() {
        auth.signOut()
        _currentUser.value = null
        _userRoles.value = emptyList()
    }
    
    /**
     * Set the loading state directly
     */
    fun setLoading(isLoading: Boolean) {
        _isLoading.value = isLoading
    }
    
    /**
     * Authenticate directly with a Google ID token
     */
    fun authenticateWithGoogleToken(
        idToken: String,
        role: UserRole? = null,
        onComplete: (Boolean, String?) -> Unit
    ) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    _currentUser.value = user
                    
                    if (user != null) {
                        // Get user details from Google account
                        val displayName = user.displayName ?: ""
                        val email = user.email ?: ""
                        val photoUrl = user.photoUrl?.toString()
                        
                        // Check if this is a new or existing user
                        val isNewUser = task.result?.additionalUserInfo?.isNewUser ?: false
                        
                        if (isNewUser && role != null) {
                            // Create new user document with Google profile info
                            val userData = hashMapOf(
                                "roles" to listOf(role.name),
                                "email" to email,
                                "displayName" to displayName,
                                "photoUrl" to photoUrl,
                                "profileCreatedAt" to com.google.firebase.Timestamp.now(),
                                "authProvider" to "google"
                            )
                            
                            usersCollection.document(email)
                                .set(userData)
                                .addOnSuccessListener {
                                    fetchUserRoles(email)
                                    _isLoading.value = false
                                    onComplete(true, null)
                                }
                                .addOnFailureListener { e ->
                                    Log.w(TAG, "Error creating user document", e)
                                    _isLoading.value = false
                                    onComplete(true, "Signed in with Google but failed to save user data")
                                }
                        } else {
                            // Existing user - check if they have the selected role
                            if (role != null) {
                                addRoleToUser(email, role) { success ->
                                    fetchUserRoles(email)
                                    _isLoading.value = false
                                    onComplete(true, null)
                                }
                            } else {
                                // Just signing in
                                fetchUserRoles(email)
                                _isLoading.value = false
                                onComplete(true, null)
                            }
                        }
                    } else {
                        _isLoading.value = false
                        onComplete(false, "Authentication successful but user is null")
                    }
                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    _isLoading.value = false
                    onComplete(false, "Authentication failed: ${task.exception?.message}")
                }
            }
    }
    
    companion object {
        private const val TAG = "AuthRepository"
        
        // Keep a single instance that can be accessed from GoogleAuthHelper
        @Volatile
        private var INSTANCE: AuthRepository? = null
        
        fun getInstance(context: Context): AuthRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = AuthRepository(context)
                INSTANCE = instance
                instance
            }
        }
        
        /**
         * Reset the loading state - can be called externally (e.g. from GoogleAuthHelper)
         */
        fun resetLoadingState() {
            INSTANCE?.let {
                it._isLoading.value = false
                it.googleAuthLegacyCallback = null
                it.pendingGoogleRole = null
                Log.d(TAG, "Loading state and callbacks reset")
            }
        }
    }
} 