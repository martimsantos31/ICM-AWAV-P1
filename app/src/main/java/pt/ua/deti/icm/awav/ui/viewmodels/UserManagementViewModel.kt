package pt.ua.deti.icm.awav.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import pt.ua.deti.icm.awav.AWAVApplication
import pt.ua.deti.icm.awav.data.model.UserProfile
import pt.ua.deti.icm.awav.data.model.UserRole
import pt.ua.deti.icm.awav.data.AuthRepository
import pt.ua.deti.icm.awav.data.repository.StandsRepository
import pt.ua.deti.icm.awav.data.room.entity.Stand
import pt.ua.deti.icm.awav.data.room.entity.Worker

class UserManagementViewModel : ViewModel() {
    private val TAG = "UserManagementViewModel"
    
    // Repositories
    private val standsRepository: StandsRepository = AWAVApplication.appContainer.standsRepository
    private val firestore = FirebaseFirestore.getInstance()
    private val usersCollection = firestore.collection("users")
    
    // UI state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    // Data state
    private val _users = MutableStateFlow<List<UserProfile>>(emptyList())
    val users: StateFlow<List<UserProfile>> = _users.asStateFlow()
    
    private val _stands = MutableStateFlow<List<Stand>>(emptyList())
    val stands: StateFlow<List<Stand>> = _stands.asStateFlow()
    
    private val _workers = MutableStateFlow<List<WorkerWithUser>>(emptyList())
    val workers: StateFlow<List<WorkerWithUser>> = _workers.asStateFlow()
    
    // Initialize data
    init {
        loadUsers()
    }
    
    // Load all users
    fun loadUsers() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val usersSnapshot = withContext(Dispatchers.IO) {
                    usersCollection.get().await()
                }
                
                val usersList = usersSnapshot.documents.mapNotNull { doc ->
                    try {
                        val email = doc.id
                        val displayName = doc.getString("displayName") ?: "Unknown"
                        val photoUrl = doc.getString("photoUrl")
                        val roles = doc.get("roles") as? List<String> ?: emptyList()
                        val userRoles = roles.mapNotNull { roleName ->
                            try {
                                UserRole.valueOf(roleName)
                            } catch (e: IllegalArgumentException) {
                                null
                            }
                        }
                        
                        val uid = doc.getString("uid") ?: ""
                        
                        UserProfile(
                            id = uid,
                            email = email,
                            name = displayName,
                            photoUrl = photoUrl,
                            role = userRoles.firstOrNull() ?: UserRole.PARTICIPANT
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing user document: ${e.message}")
                        null
                    }
                }
                
                _users.value = usersList
                Log.d(TAG, "Loaded ${usersList.size} users")
            } catch (e: Exception) {
                Log.e(TAG, "Error loading users: ${e.message}")
                _error.value = "Failed to load users: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    // Load all stands for an event
    fun loadStandsForEvent(eventId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            
            try {
                val stands = withContext(Dispatchers.IO) {
                    standsRepository.getStandsForEvent(eventId)
                }
                
                _stands.value = stands
                
                // After loading stands, load all workers for these stands
                loadWorkersForStands(stands.map { it.id })
            } catch (e: Exception) {
                Log.e(TAG, "Error loading stands: ${e.message}")
                _error.value = "Failed to load stands: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    // Load workers for all stands
    private fun loadWorkersForStands(standIds: List<Int>) {
        viewModelScope.launch {
            try {
                val allWorkers = mutableListOf<WorkerWithUser>()
                
                for (standId in standIds) {
                    val workers = withContext(Dispatchers.IO) {
                        standsRepository.getWorkersForStand(standId).first()
                    }
                    
                    // Find corresponding user profiles
                    for (worker in workers) {
                        val userProfile = _users.value.find { it.id == worker.userId }
                        if (userProfile != null) {
                            allWorkers.add(
                                WorkerWithUser(
                                    worker = worker,
                                    userProfile = userProfile,
                                    standName = _stands.value.find { it.id == worker.standId }?.name ?: "Unknown Stand"
                                )
                            )
                        }
                    }
                }
                
                _workers.value = allWorkers
                Log.d(TAG, "Loaded ${allWorkers.size} workers for ${standIds.size} stands")
            } catch (e: Exception) {
                Log.e(TAG, "Error loading workers: ${e.message}")
                _error.value = "Failed to load workers: ${e.message}"
            }
        }
    }
    
    // Assign a user as a worker to a stand
    fun assignWorkerToStand(userId: String, userName: String, standId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            
            try {
                // First check if this user is already assigned to this stand
                val existingWorkers = withContext(Dispatchers.IO) {
                    standsRepository.getWorkersForStand(standId).first()
                }
                
                // If user is already assigned, don't create a duplicate
                if (existingWorkers.any { it.userId == userId }) {
                    _error.value = "User is already assigned to this stand"
                    return@launch
                }
                
                // Create a new worker
                val worker = Worker(
                    standId = standId,
                    name = userName,
                    userId = userId
                )
                
                // Add worker to database
                withContext(Dispatchers.IO) {
                    standsRepository.insertWorker(worker)
                }
                
                // Update the user's role in Firebase to include STAND_WORKER
                updateUserRole(userId)
                
                // Refresh workers list
                val updatedWorkers = withContext(Dispatchers.IO) {
                    standsRepository.getWorkersForStand(standId).first()
                }
                
                // Find the stand name
                val standName = _stands.value.find { it.id == standId }?.name ?: "Unknown Stand"
                
                // Find corresponding user profile
                val userProfile = _users.value.find { it.id == userId }
                if (userProfile != null) {
                    // Add the new worker to the list
                    val newWorkerWithUser = WorkerWithUser(
                        worker = updatedWorkers.last(), // Assume the last one is the one we just added
                        userProfile = userProfile,
                        standName = standName
                    )
                    
                    _workers.value = _workers.value + newWorkerWithUser
                }
                
                Log.d(TAG, "Assigned worker $userName to stand ID $standId")
            } catch (e: Exception) {
                Log.e(TAG, "Error assigning worker: ${e.message}")
                _error.value = "Failed to assign worker: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    // Remove a worker from a stand
    fun removeWorker(worker: Worker) {
        viewModelScope.launch {
            _isLoading.value = true
            
            try {
                withContext(Dispatchers.IO) {
                    standsRepository.deleteWorker(worker)
                }
                
                // Remove from local list
                _workers.value = _workers.value.filter { it.worker.id != worker.id }
                
                Log.d(TAG, "Removed worker ${worker.name} from stand ID ${worker.standId}")
            } catch (e: Exception) {
                Log.e(TAG, "Error removing worker: ${e.message}")
                _error.value = "Failed to remove worker: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    // Update user role to STAND_WORKER in Firebase
    private suspend fun updateUserRole(userId: String) {
        try {
            // Find user by UID
            val userQuery = withContext(Dispatchers.IO) {
                usersCollection.whereEqualTo("uid", userId).get().await()
            }
            
            if (!userQuery.isEmpty) {
                val userDoc = userQuery.documents.first()
                val email = userDoc.id
                
                // Get current roles
                val currentRoles = userDoc.get("roles") as? List<String> ?: emptyList()
                
                // Add STAND_WORKER role if not present
                if (!currentRoles.contains(UserRole.STAND_WORKER.name)) {
                    val updatedRoles = currentRoles + UserRole.STAND_WORKER.name
                    
                    // Update roles in Firebase
                    withContext(Dispatchers.IO) {
                        usersCollection.document(email)
                            .update("roles", updatedRoles)
                            .await()
                    }
                    
                    Log.d(TAG, "Updated user $email roles to include STAND_WORKER")
                }
            } else {
                Log.w(TAG, "Could not find user with UID $userId")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating user role: ${e.message}")
            throw e
        }
    }
    
    // Data class to hold worker info with user profile
    data class WorkerWithUser(
        val worker: Worker,
        val userProfile: UserProfile,
        val standName: String
    )
} 