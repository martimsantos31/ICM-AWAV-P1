package pt.ua.deti.icm.awav.ui.screens.auth

import android.content.Context
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

class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {
    
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
    
    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()
    
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
        authRepository.fetchUserRoles(_email.value) { roles ->
            onComplete(roles)
        }
    }
    
    fun signOut() {
        authRepository.signOut()
    }
    
    fun checkAuthState() {
        authRepository.checkAuthState()
    }
    
    companion object {
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