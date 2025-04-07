package pt.ua.deti.icm.awav.ui.screens.auth

import android.content.Context
import android.content.SharedPreferences
import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pt.ua.deti.icm.awav.data.AuthRepository
import pt.ua.deti.icm.awav.data.model.UserRole
import android.net.Uri
import android.util.Log

class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {
    
    private val prefs: SharedPreferences = authRepository.appContext.getSharedPreferences(
        "awav_preferences", Context.MODE_PRIVATE
    )
    
    // Auth state
    val currentUser = authRepository.currentUser
    val userRoles = authRepository.userRoles
    val repositoryLoading = authRepository.isLoading
    
    // UI state
    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()
    
    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()
    
    private val _displayName = MutableStateFlow("")
    val displayName: StateFlow<String> = _displayName.asStateFlow()
    
    private val _profilePicUri = MutableStateFlow<Uri?>(null)
    val profilePicUri: StateFlow<Uri?> = _profilePicUri.asStateFlow()
    
    private val _selectedRole = MutableStateFlow<UserRole?>(null)
    val selectedRole: StateFlow<UserRole?> = _selectedRole.asStateFlow()
    
    // New state for active role in current session
    private val _activeRole = MutableStateFlow<UserRole?>(null)
    val activeRole: StateFlow<UserRole?> = _activeRole.asStateFlow()
    
    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()
    
    init {
        loadActiveRoleFromPrefs()
        authRepository.addRoleUpdateListener { initializeActiveRole() }
        
        // Automatically refresh user state when ViewModel initializes
        viewModelScope.launch {
            refreshUserState()
            Log.d(TAG, "AuthViewModel initialized and user state refreshed")
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        // Clean up listener
        authRepository.removeRoleUpdateListener { _ ->
            initializeActiveRole()
        }
    }
    
    // Update UI state
    fun updateEmail(email: String) {
        _email.value = email
    }
    
    fun updatePassword(password: String) {
        _password.value = password
    }
    
    fun updateDisplayName(name: String) {
        _displayName.value = name
    }
    
    fun updateProfilePicUri(uri: Uri?) {
        _profilePicUri.value = uri
    }
    
    fun updateSelectedRole(role: UserRole?) {
        _selectedRole.value = role
        // Also set as active role if setting a new role 
        if (role != null) {
            setActiveRole(role)
        }
    }
    
    // Functions to handle active role
    fun setActiveRole(role: UserRole) {
        _activeRole.value = role
        // Save to preferences for persistence
        saveActiveRoleToPrefs(role)
    }
    
    private fun saveActiveRoleToPrefs(role: UserRole) {
        prefs.edit().putString("active_role", role.name).apply()
    }
    
    private fun loadActiveRoleFromPrefs() {
        val savedRole = prefs.getString("active_role", null)
        
        if (savedRole != null) {
            try {
                Log.d(TAG, "Loading active role from preferences: $savedRole")
                _activeRole.value = UserRole.valueOf(savedRole)
            } catch (e: IllegalArgumentException) {
                Log.e(TAG, "Invalid active role stored in preferences: $savedRole", e)
                _activeRole.value = null
            }
        } else {
            Log.d(TAG, "No active role found in preferences")
            _activeRole.value = null
        }
    }
    
    // Initialize active role from available roles if not set
    fun initializeActiveRole() {
        val availableRoles = userRoles.value
        Log.d("AuthViewModel", "Initializing active role. Current: ${_activeRole.value}, Available: $availableRoles")
        
        if (_activeRole.value == null) {
            // If user has the ORGANIZER role, prioritize it over other roles
            val organizer = availableRoles.find { it == UserRole.ORGANIZER }
            if (organizer != null) {
                _activeRole.value = organizer
                Log.d("AuthViewModel", "Setting active role to ORGANIZER")
                saveActiveRoleToPrefs(organizer)
            } 
            // Or if user has STAND_WORKER, prioritize it over PARTICIPANT
            else if (availableRoles.find { it == UserRole.STAND_WORKER } != null) {
                val standWorker = UserRole.STAND_WORKER
                _activeRole.value = standWorker
                Log.d("AuthViewModel", "Setting active role to STAND_WORKER")
                saveActiveRoleToPrefs(standWorker)
            }
            // Fallback to the first available role
            else if (availableRoles.isNotEmpty()) {
                _activeRole.value = availableRoles.first()
                Log.d("AuthViewModel", "Setting active role to first available: ${availableRoles.first()}")
                saveActiveRoleToPrefs(availableRoles.first())
            }
        } else {
            // If we have an active role, make sure it's in the available roles
            val currentActive = _activeRole.value
            
            if (currentActive != null && !availableRoles.contains(currentActive)) {
                // If current active role isn't available, reset to an available role
                Log.d("AuthViewModel", "Current active role $currentActive not in available roles")
                if (availableRoles.isNotEmpty()) {
                    _activeRole.value = availableRoles.first()
                    Log.d("AuthViewModel", "Resetting to first available: ${availableRoles.first()}")
                    saveActiveRoleToPrefs(availableRoles.first())
                } else {
                    _activeRole.value = null
                    Log.d("AuthViewModel", "No roles available, setting active role to null")
                }
            } else {
                Log.d("AuthViewModel", "Active role already set correctly to: ${_activeRole.value}")
            }
        }
    }
    
    // Call this after login or role updates
    fun refreshUserState() {
        viewModelScope.launch {
            Log.d(TAG, "Refreshing user state")
            // Use the current user's email or an empty callback if no user
            val currentUserEmail = authRepository.currentUser.value?.email
            if (currentUserEmail != null) {
                authRepository.fetchUserRoles(currentUserEmail) { roles ->
                    Log.d(TAG, "Roles fetched: $roles")
                    initializeActiveRole()
                }
            } else {
                Log.d(TAG, "No current user email found, can't fetch roles")
                initializeActiveRole()
            }
        }
    }
    
    // Add method to directly set Google loading state
    fun setGoogleLoading(isLoading: Boolean) {
        viewModelScope.launch {
            authRepository.setLoading(isLoading)
        }
    }
    
    /**
     * Update user profile information
     */
    fun updateUserProfile(displayName: String, profilePicUri: Uri?, onComplete: (Boolean) -> Unit) {
        _loading.value = true
        authRepository.updateUserProfile(displayName, profilePicUri) { success ->
            _loading.value = false
            onComplete(success)
        }
    }
    
    /**
     * Initialize editing fields with current user data
     */
    fun initializeEditFields() {
        currentUser.value?.let { user ->
            _displayName.value = user.displayName ?: ""
            // We don't set profile pic URI because it's stored in Firebase Storage
            // and we'll need a new URI from the image picker when editing
        }
    }
    
    /**
     * Authenticate directly with a Google ID token
     */
    fun authenticateWithGoogleToken(idToken: String, onComplete: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                authRepository.authenticateWithGoogleToken(
                    idToken = idToken,
                    role = _selectedRole.value,
                    onComplete = onComplete
                )
            } catch (e: Exception) {
                onComplete(false, "Authentication error: ${e.message}")
                setGoogleLoading(false)
            }
        }
    }
    
    // Auth functions
    fun signIn(onComplete: (Boolean) -> Unit) {
        _loading.value = true
        authRepository.signIn(_email.value, _password.value) { success ->
            _loading.value = false
            onComplete(success)
        }
    }
    
    fun signUp(onComplete: (Boolean) -> Unit) {
        val selectedRole = _selectedRole.value
        if (selectedRole == null) {
            onComplete(false)
            return
        }
        
        _loading.value = true
        authRepository.signUp(
            email = _email.value, 
            password = _password.value, 
            displayName = _displayName.value,
            profilePicUri = _profilePicUri.value,
            role = selectedRole
        ) { success ->
            _loading.value = false
            onComplete(success)
        }
    }
    
    fun signInWithGoogle(activity: ComponentActivity, onComplete: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            authRepository.signInWithGoogle(
                activity = activity,
                role = _selectedRole.value
            ) { success, errorMessage ->
                onComplete(success, errorMessage)
            }
        }
    }
    
    fun signUpWithGoogle(activity: ComponentActivity, onComplete: (Boolean, String?) -> Unit) {
        val selectedRole = _selectedRole.value
        if (selectedRole == null) {
            onComplete(false, "Please select a role first")
            return
        }
        
        viewModelScope.launch {
            authRepository.signInWithGoogle(
                activity = activity,
                role = selectedRole
            ) { success, errorMessage ->
                onComplete(success, errorMessage)
            }
        }
    }
    
    fun fetchUserRoles(onComplete: (List<UserRole>) -> Unit) {
        val email = _email.value
        if (email.isNotEmpty()) {
            authRepository.fetchUserRoles(email) { roles ->
                onComplete(roles)
            }
        } else {
            // Try to use current user email if available
            val currentUserEmail = authRepository.currentUser.value?.email
            if (currentUserEmail != null) {
                authRepository.fetchUserRoles(currentUserEmail) { roles ->
                    onComplete(roles)
                }
            } else {
                // No email available
                onComplete(emptyList())
            }
        }
    }
    
    fun signOut() {
        authRepository.signOut()
    }
    
    fun checkAuthState() {
        authRepository.checkAuthState()
    }
    
    companion object {
        private const val TAG = "AuthViewModel"
        
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val context = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Context
                AuthViewModel(
                    authRepository = AuthRepository(context)
                )
            }
        }
    }
} 