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
    
    // Keep track of the current event ID
    private var currentEventId: Int? = null
    
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
                Log.d(TAG, "Starting to load all users")
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
                
                // If we already have an event ID, refresh the workers data with the new user list
                if (currentEventId != null) {
                    Log.d(TAG, "Already have event ID $currentEventId, refreshing worker data")
                    withContext(Dispatchers.IO) {
                        // Add a small delay to ensure Firebase updates are complete
                        kotlinx.coroutines.delay(200)
                    }
                    refreshWorkersData(currentEventId!!)
                } else {
                    Log.d(TAG, "No current event ID, skipping worker refresh")
                }
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
            _error.value = null
            
            try {
                // Store the current event ID
                currentEventId = eventId
                Log.d(TAG, "Loading stands for event ID: $eventId")
                
                // First load the users
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
                Log.d(TAG, "Loaded ${usersList.size} users while initializing stands for event $eventId")
                
                // Then load the stands
                val stands = withContext(Dispatchers.IO) {
                    standsRepository.getStandsForEvent(eventId)
                }
                
                _stands.value = stands
                Log.d(TAG, "Loaded ${stands.size} stands for event $eventId")
                
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
    
    // Refresh workers data with the current stands
    private suspend fun refreshWorkersData(eventId: Int) {
        try {
            Log.d(TAG, "Starting worker data refresh for event $eventId")
            val stands = withContext(Dispatchers.IO) {
                standsRepository.getStandsForEvent(eventId)
            }
            
            Log.d(TAG, "Found ${stands.size} stands for event $eventId during refresh")
            _stands.value = stands
            
            // Refresh workers for these stands
            loadWorkersForStands(stands.map { it.id })
        } catch (e: Exception) {
            Log.e(TAG, "Error refreshing workers data: ${e.message}")
        }
    }
    
    // Load workers for all stands
    private fun loadWorkersForStands(standIds: List<Int>) {
        viewModelScope.launch {
            try {
                val allWorkers = mutableListOf<WorkerWithUser>()
                
                // Keep track of user IDs that we've already added to prevent duplicates
                val addedUserIds = mutableSetOf<String>()
                
                for (standId in standIds) {
                    val workers = withContext(Dispatchers.IO) {
                        standsRepository.getWorkersForStand(standId).first()
                    }
                    
                    Log.d(TAG, "Found ${workers.size} workers for stand $standId")
                    
                    // Find corresponding user profiles
                    for (worker in workers) {
                        Log.d(TAG, "Processing worker ${worker.id}: userId=${worker.userId}, standId=${worker.standId}")
                        
                        // Skip if we've already added this user
                        if (worker.userId in addedUserIds) {
                            Log.w(TAG, "Skipping duplicate worker with userId=${worker.userId} - already added")
                            continue
                        }
                        
                        val userProfile = _users.value.find { it.id == worker.userId }
                        if (userProfile != null) {
                            allWorkers.add(
                                WorkerWithUser(
                                    worker = worker,
                                    userProfile = userProfile,
                                    standName = _stands.value.find { it.id == worker.standId }?.name ?: "Unknown Stand"
                                )
                            )
                            // Mark this user ID as added
                            addedUserIds.add(worker.userId)
                            Log.d(TAG, "Added worker ${worker.name} to list, assigned to ${_stands.value.find { it.id == worker.standId }?.name}")
                        } else {
                            Log.w(TAG, "Could not find user profile for worker ${worker.name} (userId: ${worker.userId})")
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
            _error.value = null
            
            try {
                Log.d(TAG, "Assigning worker $userName (userId: $userId) to stand $standId")
                
                // Check if this user is already assigned to ANY stand
                var isAlreadyAssigned = false
                
                // Get all stands
                val allStands = withContext(Dispatchers.IO) {
                    standsRepository.getAllStands()
                }
                
                // Check each stand for this user
                for (stand in allStands) {
                    val standWorkers = withContext(Dispatchers.IO) {
                        standsRepository.getWorkersForStand(stand.id).first()
                    }
                    
                    if (standWorkers.any { it.userId == userId }) {
                        isAlreadyAssigned = true
                        _error.value = "User is already assigned to stand: ${stand.name}"
                        Log.w(TAG, "User $userName is already assigned to stand ${stand.id} (${stand.name})")
                        break
                    }
                }
                
                if (isAlreadyAssigned) {
                    return@launch
                }
                
                // Create a new worker
                val worker = Worker(
                    standId = standId,
                    name = userName,
                    userId = userId
                )
                
                // Add worker to database and get the ID
                val workerId = withContext(Dispatchers.IO) {
                    val id = standsRepository.insertWorker(worker)
                    Log.d(TAG, "Inserted worker into database with ID: $id")
                    id
                }
                
                // Update the user's role in Firebase to include STAND_WORKER
                updateUserRole(userId)
                
                // Force a refresh of the worker list by re-loading all data
                withContext(Dispatchers.IO) {
                    // Small delay to ensure database operations have completed
                    kotlinx.coroutines.delay(300)
                }
                
                // Reload users first to ensure they have the new role
                loadUsers()
                
                // Then explicitly reload stands and workers
                currentEventId?.let { eventId ->
                    Log.d(TAG, "Refreshing worker data for event $eventId after assigning new worker")
                    refreshWorkersData(eventId)
                }
                
                // Show success message
                Log.d(TAG, "Successfully assigned worker $userName to stand $standId")
            } catch (e: Exception) {
                Log.e(TAG, "Error assigning worker: ${e.message}", e)
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
                    
                    // Reload users to get updated roles
                    loadUsers()
                }
            } else {
                Log.w(TAG, "Could not find user with UID $userId")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating user role: ${e.message}")
            throw e
        }
    }
    
    // Get worker by ID
    fun getWorkerById(workerId: Int): Worker? {
        return _workers.value.find { it.worker.id == workerId }?.worker
    }
    
    // Data class to hold worker info with user profile
    data class WorkerWithUser(
        val worker: Worker,
        val userProfile: UserProfile,
        val standName: String
    )
} 