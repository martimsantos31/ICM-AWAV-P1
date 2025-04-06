package pt.ua.deti.icm.awav.ui.screens.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import pt.ua.deti.icm.awav.data.AuthRepository

class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {
    
    // Auth state
    val currentUser = authRepository.currentUser
    
    // UI state
    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()
    
    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()
    
    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()
    
    // Update UI state
    fun updateEmail(email: String) {
        _email.value = email
    }
    
    fun updatePassword(password: String) {
        _password.value = password
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
        _loading.value = true
        authRepository.signUp(_email.value, _password.value) { success ->
            _loading.value = false
            onComplete(success)
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