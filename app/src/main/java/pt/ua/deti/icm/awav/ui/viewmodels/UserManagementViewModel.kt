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
                // First, get the Auth Repository instance to check current user
                val authRepository = AuthRepository.getInstance()
                val currentUser = authRepository.currentUser.value
                Log.d(TAG, "Current authenticated user: ${currentUser?.email}, UID: ${currentUser?.uid}")
                
                val usersSnapshot = withContext(Dispatchers.IO) {
                    usersCollection.get().await()
                }
                
                Log.d(TAG, "Loaded ${usersSnapshot.documents.size} user documents from Firestore")
                
                val usersList = usersSnapshot.documents.mapNotNull { doc ->
                    try {
                        val email = doc.id
                        val displayName = doc.getString("displayName") ?: "Unknown"
                        val photoUrl = doc.getString("photoUrl")
                        
                        // Retrieve UID and roles directly from the document
                        val uid = doc.getString("uid") ?: "" // Allow empty UID
                        val roles = doc.get("roles") as? List<String> ?: emptyList()
                        
                        // Convert roles string to enum
                        val userRoles = roles.mapNotNull { roleName ->
                            try {
                                UserRole.valueOf(roleName)
                            } catch (e: IllegalArgumentException) {
                                null
                            }
                        }
                        
                        // Log the raw document data for debugging
                        Log.d(TAG, "User document for $email: ${doc.data}")
                        
                        // Check if UID is missing and log it (but don't skip the user)
                        if (uid.isBlank()) {
                            Log.w(TAG, "User $displayName (email: $email) has no UID in Firestore. Using email as primary identifier.")
                        }
                        
                        val userProfile = UserProfile(
                            id = uid, // May be empty
                            email = email, // Use email as the primary identifier
                            name = displayName,
                            photoUrl = photoUrl,
                            role = userRoles.firstOrNull() ?: UserRole.PARTICIPANT
                        )
                        
                        Log.d(TAG, "Created user profile: name=${userProfile.name}, id='${userProfile.id}', email=${userProfile.email}, role=${userProfile.role}")
                        userProfile
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing user document: ${e.message}", e)
                        null
                    }
                }
                
                _users.value = usersList
                Log.d(TAG, "Loaded ${usersList.size} valid users")
            } catch (e: Exception) {
                Log.e(TAG, "Error loading users: ${e.message}", e)
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
                Log.d(TAG, "Loading stands for event ID: $eventId")
                val stands = withContext(Dispatchers.IO) {
                    standsRepository.getStandsForEvent(eventId)
                }
                
                Log.d(TAG, "Found ${stands.size} stands for event ID: $eventId")
                _stands.value = stands
                
                // After loading stands, load all workers for these stands
                if (stands.isNotEmpty()) {
                    loadWorkersForStands(stands.map { it.id })
                } else {
                    _workers.value = emptyList()
                    Log.d(TAG, "No stands found for event ID: $eventId, so no workers to load")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading stands: ${e.message}", e)
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
                Log.d(TAG, "Loading workers for ${standIds.size} stands")
                val allWorkers = mutableListOf<WorkerWithUser>()
                
                for (standId in standIds) {
                    val workers = withContext(Dispatchers.IO) {
                        standsRepository.getWorkersForStand(standId).first()
                    }
                    
                    Log.d(TAG, "Found ${workers.size} workers for stand ID: $standId")
                    
                    // Find corresponding user profiles
                    for (worker in workers) {
                        // Check if userId is an email or UID
                        val isEmail = worker.userId.contains("@")
                        
                        // Find user by either ID or email
                        val userProfile = if (isEmail) {
                            // If userId is an email, find by email
                            _users.value.find { it.email == worker.userId }
                        } else {
                            // If userId is a UID, find by id
                            _users.value.find { it.id == worker.userId }
                        }
                        
                        if (userProfile != null) {
                            allWorkers.add(
                                WorkerWithUser(
                                    worker = worker,
                                    userProfile = userProfile,
                                    standName = _stands.value.find { it.id == worker.standId }?.name ?: "Unknown Stand"
                                )
                            )
                            Log.d(TAG, "Found user profile for worker ${worker.name} (ID: ${worker.userId})")
                        } else {
                            Log.w(TAG, "Could not find user profile for worker ${worker.name} (ID: ${worker.userId})")
                        }
                    }
                }
                
                _workers.value = allWorkers
                Log.d(TAG, "Loaded ${allWorkers.size} workers for ${standIds.size} stands")
            } catch (e: Exception) {
                Log.e(TAG, "Error loading workers: ${e.message}", e)
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
                // Find the user profile to get the email
                val userProfile = _users.value.find { it.id == userId || it.email == userId }
                
                // If userId is empty but we found a user profile, use the email
                val userIdentifier = if (userId.isBlank() && userProfile != null) {
                    Log.d(TAG, "Using email as identifier: ${userProfile.email}")
                    userProfile.email
                } else {
                    userId
                }
                
                // Validate user identifier
                if (userIdentifier.isBlank()) {
                    Log.e(TAG, "Cannot assign worker with empty user identifier: $userName")
                    _error.value = "Cannot assign worker: User identifier is missing"
                    return@launch
                }
                
                Log.d(TAG, "Assigning user $userName (ID: $userIdentifier) to stand ID: $standId")
                
                // First check if this user is already assigned to this stand
                val existingWorkers = withContext(Dispatchers.IO) {
                    standsRepository.getWorkersForStand(standId).first()
                }
                
                // If user is already assigned, don't create a duplicate
                if (existingWorkers.any { it.userId == userIdentifier }) {
                    Log.w(TAG, "User $userName is already assigned to stand ID: $standId")
                    _error.value = "User is already assigned to this stand"
                    return@launch
                }
                
                // Use the actual user profile if available
                val actualUserProfile = userProfile ?: _users.value.find { it.email == userIdentifier }
                if (actualUserProfile == null) {
                    Log.e(TAG, "Cannot find user with identifier: $userIdentifier in the loaded users list")
                    _error.value = "Cannot find user to assign as worker"
                    return@launch
                }
                
                // Create a new worker
                val worker = Worker(
                    standId = standId,
                    name = actualUserProfile.name,
                    userId = userIdentifier // Use email as the identifier
                )
                
                // Add worker to database
                val workerId = withContext(Dispatchers.IO) {
                    standsRepository.insertWorker(worker)
                }
                
                Log.d(TAG, "Created worker with ID: $workerId for user: ${actualUserProfile.name}")
                
                // Update the user's role in Firebase to include STAND_WORKER
                updateUserRole(userIdentifier)
                
                // Refresh workers list
                val updatedWorkers = withContext(Dispatchers.IO) {
                    standsRepository.getWorkersForStand(standId).first()
                }
                
                // Find the stand name
                val standName = _stands.value.find { it.id == standId }?.name ?: "Unknown Stand"
                
                // Find the newly created worker with its assigned ID
                val newWorker = updatedWorkers.find { it.userId == userIdentifier }
                if (newWorker != null) {
                    // Add the new worker to the list with the correct user profile
                    val newWorkerWithUser = WorkerWithUser(
                        worker = newWorker,
                        userProfile = actualUserProfile,
                        standName = standName
                    )
                    
                    _workers.value = _workers.value + newWorkerWithUser
                    Log.d(TAG, "Added worker ${actualUserProfile.name} to UI list for stand: $standName")
                } else {
                    Log.e(TAG, "Could not find newly created worker in database after insertion")
                }
                
                Log.d(TAG, "Successfully assigned worker ${actualUserProfile.name} to stand ID $standId")
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
            // Check if the userId is an email address
            val isEmail = userId.contains("@")
            
            if (isEmail) {
                // If userId is an email, use it directly to update the document
                val userDoc = withContext(Dispatchers.IO) {
                    usersCollection.document(userId).get().await()
                }
                
                if (userDoc.exists()) {
                    // Get current roles
                    val currentRoles = userDoc.get("roles") as? List<String> ?: emptyList()
                    
                    // Add STAND_WORKER role if not present
                    if (!currentRoles.contains(UserRole.STAND_WORKER.name)) {
                        val updatedRoles = currentRoles + UserRole.STAND_WORKER.name
                        
                        // Update roles in Firebase
                        withContext(Dispatchers.IO) {
                            usersCollection.document(userId)
                                .update("roles", updatedRoles)
                                .await()
                        }
                        
                        Log.d(TAG, "Updated user $userId roles to include STAND_WORKER")
                    }
                } else {
                    Log.w(TAG, "Could not find user document with email $userId")
                }
            } else {
                // Find user by UID (original implementation)
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
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating user role: ${e.message}", e)
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