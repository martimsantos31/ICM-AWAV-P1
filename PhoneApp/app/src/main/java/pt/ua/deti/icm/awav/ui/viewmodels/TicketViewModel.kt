package pt.ua.deti.icm.awav.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import pt.ua.deti.icm.awav.AWAVApplication
import pt.ua.deti.icm.awav.data.AuthRepository
import pt.ua.deti.icm.awav.data.repository.EventsRepository
import pt.ua.deti.icm.awav.data.room.entity.Event
import pt.ua.deti.icm.awav.data.room.entity.Ticket
import pt.ua.deti.icm.awav.data.room.entity.UserTicket
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot

class TicketViewModel(
    private val eventsRepository: EventsRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val TAG = "TicketViewModel"

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _userTickets = MutableStateFlow<List<UserTicket>>(emptyList())
    val userTickets: StateFlow<List<UserTicket>> = _userTickets.asStateFlow()

    private val _userEvents = MutableStateFlow<List<Event>>(emptyList())
    val userEvents: StateFlow<List<Event>> = _userEvents.asStateFlow()
    
    private val _activeEvents = MutableStateFlow<List<Event>>(emptyList())
    val activeEvents: StateFlow<List<Event>> = _activeEvents.asStateFlow()

    // Current user ID
    private val _currentUserId = MutableStateFlow<String?>(null)
    
    // Has active tickets flag
    private val _hasActiveTickets = MutableStateFlow(false)
    val hasActiveTickets: StateFlow<Boolean> = _hasActiveTickets.asStateFlow()

    init {
        // ALWAYS Initialize with restricted access by default
        _hasActiveTickets.value = false
        
        // Force immediate ticket status check on launch
        refreshTicketStatus()
        
        // Load active events for purchase
        loadActiveEvents()
    }

    /**
     * Force user to start with RESTRICTED access, then check Firebase for tickets
     * This is especially important for new registrations and logins
     */
    fun forceRestrictedStart() {
        // First, fully reset all state to ensure no leftover data from previous sessions
        resetTicketStatus()
        
        // CRITICAL: Always start with restricted access by default
        _hasActiveTickets.value = false
        
        Log.d(TAG, "forceRestrictedStart: Setting initial state to RESTRICTED")
        
        // Then check Firebase for tickets in the background
        viewModelScope.launch {
            // Wait a moment to ensure Firebase Auth is fully initialized
            kotlinx.coroutines.delay(500)
            Log.d(TAG, "Delayed force check for tickets after login/registration")
            refreshTicketStatus(forceFirebaseCheck = true)
            
            // Double-check again after a longer delay to ensure Firebase data is loaded
            kotlinx.coroutines.delay(1500)
            Log.d(TAG, "Second force check for tickets after login/registration")
            refreshTicketStatus(forceFirebaseCheck = true)
        }
    }

    /**
     * Force a refresh of the ticket status - can be called from any screen
     */
    fun refreshTicketStatus(forceFirebaseCheck: Boolean = false) {
        viewModelScope.launch {
            // Get current user
            val currentUser = authRepository.currentUser.value
            if (currentUser != null) {
                val userId = currentUser.uid
                val userEmail = currentUser.email
                _currentUserId.value = userId
                
                // Force load all tickets first
                _userTickets.value = emptyList()
                
                try {
                    Log.d(TAG, "Refreshing ticket status for user: $userId (email: $userEmail)")
                    
                    if (forceFirebaseCheck && userEmail != null) {
                        // Try to directly check Firebase for tickets in the user document
                        val db = FirebaseFirestore.getInstance()
                        val usersCollection = db.collection("users")
                        
                        try {
                            // Query Firebase for the user document (using email as document ID)
                            val userDoc = usersCollection.document(userEmail).get().await()
                            
                            if (userDoc.exists()) {
                                // Check for tickets array in the user document
                                val tickets = userDoc.get("tickets") as? List<Map<String, Any>> ?: emptyList()
                                val activeTickets = tickets.filter { ticket -> 
                                    ticket["isActive"] as? Boolean ?: false 
                                }
                                
                                val hasTicketsInFirebase = activeTickets.isNotEmpty()
                                Log.d(TAG, "Firebase user doc ticket check: User has tickets = $hasTicketsInFirebase")
                                
                                // Set status based on Firebase check
                                _hasActiveTickets.value = hasTicketsInFirebase
                                
                                // If we found tickets in Firebase, make sure they're in Room too
                                if (hasTicketsInFirebase) {
                                    for (ticketData in activeTickets) {
                                        val ticketId = (ticketData["id"] as? Long)?.toInt() ?: 0
                                        Log.d(TAG, "Found ticket in Firebase user doc: $ticketId")
                                        
                                        // TODO: Sync with local database if needed
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error checking tickets in Firebase user document", e)
                            
                            // Fall back to checking the separate tickets collection
                            try {
                                val ticketsCollection = db.collection("user_tickets")
                                val query = ticketsCollection.whereEqualTo("userId", userId)
                                    .whereEqualTo("isActive", true)
                                    .get()
                                    .await()
                                    
                                val hasTicketsInFirebase = !query.isEmpty
                                Log.d(TAG, "Firebase fallback ticket check: User has tickets = $hasTicketsInFirebase")
                                
                                // Set status based on Firebase check
                                _hasActiveTickets.value = hasTicketsInFirebase
                            } catch (e2: Exception) {
                                Log.e(TAG, "Error in fallback Firebase ticket check", e2)
                            }
                        }
                    }
                    
                    // Always also check the local database
                    try {
                        // Use collect instead of direct value access
                        eventsRepository.getActiveTicketCount(userId).collect { count ->
                            Log.d(TAG, "Local DB active ticket count: $count")
                            val hasTickets = count > 0
                            
                            // Only update if it would grant access
                            // This ensures Firebase-granted access isn't accidentally revoked
                            if (hasTickets) {
                                _hasActiveTickets.value = true
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error checking local ticket count", e)
                    }
                    
                    // Also load ticket data
                    loadUserTickets(userId)
                    
                    // Finally log the result for debugging
                    Log.d(TAG, "Final ticket status after all checks: ${_hasActiveTickets.value}")
                } catch (e: Exception) {
                    Log.e(TAG, "Error refreshing ticket status", e)
                    _hasActiveTickets.value = false
                }
            } else {
                // No user logged in, ensure access is restricted
                _hasActiveTickets.value = false
                _userTickets.value = emptyList()
                _userEvents.value = emptyList()
            }
        }
    }

    private fun loadActiveEvents() {
        viewModelScope.launch {
            eventsRepository.getActiveEvents().collectLatest { events ->
                _activeEvents.value = events
            }
        }
    }
    
    private fun loadUserTickets(userId: String) {
        viewModelScope.launch {
            _loading.value = true
            eventsRepository.getUserTickets(userId).collectLatest { tickets ->
                _userTickets.value = tickets
                _loading.value = false
                
                // Also load events for those tickets
                loadUserEvents(userId)
            }
        }
    }
    
    private fun loadUserEvents(userId: String) {
        viewModelScope.launch {
            eventsRepository.getEventsForUserTickets(userId).collectLatest { events ->
                _userEvents.value = events
            }
        }
    }
    
    /**
     * Purchase a ticket for an event
     */
    suspend fun purchaseTicket(ticket: Ticket): Boolean {
        val userId = _currentUserId.value
        if (userId == null) {
            Log.e(TAG, "Purchase ticket failed: No user ID available")
            return false
        }
        
        _loading.value = true
        
        return try {
            Log.d(TAG, "Purchasing ticket: $ticket for user: $userId")
            
            // First insert the ticket
            eventsRepository.insertTicket(ticket)
            
            // Get current date as string in format "yyyy-MM-dd"
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val currentDate = dateFormat.format(Date())
            
            // Then create a UserTicket linking the user to the ticket
            val userTicket = UserTicket(
                userId = userId,
                ticketId = ticket.id,
                purchaseDate = currentDate,
                isActive = true
            )
            
            Log.d(TAG, "Creating user ticket: $userTicket")
            eventsRepository.insertUserTicket(userTicket)
            
            // Immediately update the active tickets state to unlock the app
            Log.d(TAG, "Setting hasActiveTickets to true")
            _hasActiveTickets.value = true
            
            // Reload user tickets in the background
            Log.d(TAG, "Reloading user tickets")
            loadUserTickets(userId)
            
            _loading.value = false
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error purchasing ticket", e)
            _loading.value = false
            false
        }
    }

    /**
     * Reset ticket status when user logs out or switches accounts
     */
    fun resetTicketStatus() {
        Log.d(TAG, "Explicitly resetting ticket status to false")
        _hasActiveTickets.value = false
        _userTickets.value = emptyList()
        _userEvents.value = emptyList()
        _currentUserId.value = null
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                TicketViewModel(
                    AWAVApplication.appContainer.eventsRepository,
                    AuthRepository.getInstance()
                )
            }
        }
    }
} 