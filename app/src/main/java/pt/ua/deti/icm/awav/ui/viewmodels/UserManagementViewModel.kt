package pt.ua.deti.icm.awav.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import pt.ua.deti.icm.awav.data.model.UserProfile
import pt.ua.deti.icm.awav.data.model.UserRole
import pt.ua.deti.icm.awav.data.repository.StandsRepository
import pt.ua.deti.icm.awav.AWAVApplication

class UserManagementViewModel : ViewModel() {
    private val firestore = Firebase.firestore
    private val usersCollection = firestore.collection("users")
    private val standsCollection = firestore.collection("stands")
    
    private val _users = MutableStateFlow<List<UserProfile>>(emptyList())
    val users: StateFlow<List<UserProfile>> = _users.asStateFlow()
    
    private val _stands = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val stands: StateFlow<List<Pair<String, String>>> = _stands.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val standsRepository: StandsRepository by lazy {
        AWAVApplication.appContainer.standsRepository
    }
    
    init {
        refreshUsers()
        loadStands()
    }
    
    fun refreshUsers() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val result = usersCollection.get().await()
                val usersList = result.documents.mapNotNull { doc ->
                    try {
                        // Parse user data from document
                        val email = doc.getString("email") ?: ""
                        val name = doc.getString("displayName") ?: ""
                        val roleStrings = doc.get("roles") as? List<String> ?: emptyList()
                        
                        // Determine user role (prioritize STAND_WORKER for our purposes)
                        val role = if (roleStrings.contains(UserRole.STAND_WORKER.name)) {
                            UserRole.STAND_WORKER
                        } else if (roleStrings.contains(UserRole.ORGANIZER.name)) {
                            UserRole.ORGANIZER
                        } else {
                            UserRole.PARTICIPANT
                        }
                        
                        // Get stand IDs managed by this user
                        val managedStandIds = doc.get("managedStandIds") as? List<String> ?: emptyList()
                        
                        UserProfile(
                            id = email, // Using email as unique ID
                            email = email,
                            name = name,
                            role = role,
                            managedStandIds = managedStandIds
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing user document", e)
                        null
                    }
                }
                
                // Filter to only show workers for stand assignment
                _users.value = usersList.filter { it.role == UserRole.STAND_WORKER }
                
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching users", e)
                _error.value = "Failed to load users: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    private fun loadStands() {
        viewModelScope.launch {
            try {
                val result = standsCollection.get().await()
                val standsList = result.documents.mapNotNull { doc ->
                    val id = doc.id
                    val name = doc.getString("name") ?: "Unknown Stand"
                    Pair(id, name)
                }
                
                _stands.value = standsList
                
                // If no stands found in Firestore, try to load from local database
                if (standsList.isEmpty()) {
                    try {
                        // Use a different approach to get local stands without await()
                        val localStandsList = mutableListOf<Pair<String, String>>()
                        standsRepository.getAllStands().collect { stands ->
                            localStandsList.clear()
                            stands.forEach { stand ->
                                localStandsList.add(Pair(stand.id.toString(), stand.name))
                            }
                            _stands.value = localStandsList
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error fetching local stands", e)
                    }
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching stands", e)
            }
        }
    }
    
    fun assignStandToUser(userId: String, standId: String) {
        viewModelScope.launch {
            try {
                // Get current stands assigned to this user
                val userDoc = usersCollection.document(userId).get().await()
                val currentStands = userDoc.get("managedStandIds") as? List<String> ?: emptyList()
                
                // Add new stand if not already assigned
                if (standId !in currentStands) {
                    val updatedStands = currentStands + standId
                    
                    // Update in Firestore
                    usersCollection.document(userId)
                        .update("managedStandIds", updatedStands)
                        .await()
                    
                    // Also update the stand document to reference this worker
                    standsCollection.document(standId)
                        .update("workerId", userId)
                        .await()
                    
                    // Update local state
                    _users.value = _users.value.map { user ->
                        if (user.id == userId) {
                            user.copy(managedStandIds = updatedStands)
                        } else {
                            user
                        }
                    }
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Error assigning stand to user", e)
                _error.value = "Failed to assign stand: ${e.message}"
            }
        }
    }
    
    fun removeStandFromUser(userId: String, standId: String) {
        viewModelScope.launch {
            try {
                // Get current stands assigned to this user
                val userDoc = usersCollection.document(userId).get().await()
                val currentStands = userDoc.get("managedStandIds") as? List<String> ?: emptyList()
                
                // Remove the stand
                if (standId in currentStands) {
                    val updatedStands = currentStands.filter { it != standId }
                    
                    // Update in Firestore
                    usersCollection.document(userId)
                        .update("managedStandIds", updatedStands)
                        .await()
                    
                    // Also update the stand document to remove this worker reference
                    standsCollection.document(standId)
                        .update("workerId", null)
                        .await()
                    
                    // Update local state
                    _users.value = _users.value.map { user ->
                        if (user.id == userId) {
                            user.copy(managedStandIds = updatedStands)
                        } else {
                            user
                        }
                    }
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Error removing stand from user", e)
                _error.value = "Failed to remove stand: ${e.message}"
            }
        }
    }
    
    companion object {
        private const val TAG = "UserManagementViewModel"
    }
} 