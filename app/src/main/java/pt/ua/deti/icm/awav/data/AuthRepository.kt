package pt.ua.deti.icm.awav.data

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import pt.ua.deti.icm.awav.data.model.UserRole

class AuthRepository(private val context: Context) {
    
    private val auth: FirebaseAuth = Firebase.auth
    private val db = Firebase.firestore
    private val usersCollection = db.collection("users")

    // Current user state
    private val _currentUser = MutableStateFlow<FirebaseUser?>(null)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()
    
    // Available roles for the current user
    private val _userRoles = MutableStateFlow<List<UserRole>>(emptyList())
    val userRoles: StateFlow<List<UserRole>> = _userRoles.asStateFlow()
    
    init {
        // Initialize current user
        _currentUser.value = auth.currentUser
        if (auth.currentUser != null) {
            fetchUserRoles(auth.currentUser!!.email!!)
        }
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
    
    fun signUp(email: String, password: String, role: UserRole, onComplete: (Boolean) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "createUserWithEmail:success")
                    _currentUser.value = auth.currentUser
                    
                    // Add the selected role to the user document
                    addRoleToUser(email, role) { success ->
                        if (!success) {
                            Toast.makeText(
                                context,
                                "Created account but failed to assign role",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        onComplete(true)
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
    
    fun signOut() {
        auth.signOut()
        _currentUser.value = null
        _userRoles.value = emptyList()
    }
    
    companion object {
        private const val TAG = "AuthRepository"
    }
} 