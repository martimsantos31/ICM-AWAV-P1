package pt.ua.deti.icm.awav.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pt.ua.deti.icm.awav.AWAVApplication
import pt.ua.deti.icm.awav.data.AuthRepository
import pt.ua.deti.icm.awav.data.repository.StandsRepository
import pt.ua.deti.icm.awav.data.room.entity.Stand
import pt.ua.deti.icm.awav.data.room.entity.Worker

class WorkerViewModel : ViewModel() {
    private val TAG = "WorkerViewModel"
    
    // Repositories
    private val standsRepository: StandsRepository = AWAVApplication.appContainer.standsRepository
    private val authRepository: AuthRepository = AuthRepository.getInstance()
    
    // UI state
    private val _assignedStand = MutableStateFlow<Stand?>(null)
    val assignedStand: StateFlow<Stand?> = _assignedStand.asStateFlow()
    
    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    init {
        loadAssignedStand()
    }
    
    /**
     * Load the stand assigned to the current worker
     */
    fun loadAssignedStand() {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            
            try {
                // Get current user ID
                val currentUserId = authRepository.currentUser.value?.uid
                
                if (currentUserId == null) {
                    _error.value = "Not logged in"
                    return@launch
                }
                
                // Load all stands to search through their workers
                val allStands = withContext(Dispatchers.IO) {
                    standsRepository.getAllStands()
                }
                
                // Find the stand where this user is assigned as a worker
                for (stand in allStands) {
                    val workers = withContext(Dispatchers.IO) {
                        standsRepository.getWorkersForStand(stand.id).first()
                    }
                    
                    // Check if current user is assigned to this stand
                    val workerAssignment = workers.find { it.userId == currentUserId }
                    
                    if (workerAssignment != null) {
                        // Found the assigned stand
                        _assignedStand.value = stand
                        Log.d(TAG, "Found assigned stand: ${stand.name} (ID: ${stand.id})")
                        break
                    }
                }
                
                if (_assignedStand.value == null) {
                    Log.d(TAG, "No stand assigned to user $currentUserId")
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Error loading assigned stand: ${e.message}", e)
                _error.value = "Failed to load assigned stand: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }
} 