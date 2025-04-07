package pt.ua.deti.icm.awav.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import pt.ua.deti.icm.awav.AWAVApplication
import pt.ua.deti.icm.awav.data.AuthRepository
import pt.ua.deti.icm.awav.data.model.FirebaseTicket
import pt.ua.deti.icm.awav.data.repository.EventsRepository
import pt.ua.deti.icm.awav.data.repository.FirebaseTicketRepository
import pt.ua.deti.icm.awav.data.room.entity.Event
import pt.ua.deti.icm.awav.data.room.entity.Ticket
import pt.ua.deti.icm.awav.data.room.entity.UserTicket
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FirebaseTicketViewModel(
    private val ticketRepository: FirebaseTicketRepository,
    private val eventsRepository: EventsRepository,
    private val authRepository: AuthRepository = AuthRepository.getInstance()
) : ViewModel() {
    private val TAG = "FirebaseTicketViewModel"
    
    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()
    
    private val _userTickets = MutableStateFlow<List<FirebaseTicket>>(emptyList())
    val userTickets: StateFlow<List<FirebaseTicket>> = _userTickets.asStateFlow()
    
    private val _activeEvents = MutableStateFlow<List<Event>>(emptyList())
    val activeEvents: StateFlow<List<Event>> = _activeEvents.asStateFlow()
    
    private val _remainingTickets = MutableStateFlow<Map<Int, Int>>(emptyMap())
    val remainingTickets: StateFlow<Map<Int, Int>> = _remainingTickets.asStateFlow()
    
    val hasActiveTickets = ticketRepository.hasActiveTickets
    
    init {
        viewModelScope.launch {
            refreshUserTickets()
            loadActiveEvents()
        }
    }
    
    private fun loadActiveEvents() {
        viewModelScope.launch {
            eventsRepository.getActiveEvents().collectLatest { events ->
                _activeEvents.value = events
                
                // Check remaining tickets for each event
                events.forEach { event ->
                    updateRemainingTicketCount(event.id)
                }
            }
        }
    }
    
    fun refreshUserTickets() {
        viewModelScope.launch {
            _loading.value = true
            ticketRepository.refreshUserTickets()
            _userTickets.value = ticketRepository.userTickets.value
            
            // Sync tickets to Room DB to ensure legacy viewmodel can access them
            syncTicketsToRoomDb()
            
            _loading.value = false
        }
    }
    
    /**
     * Sync Firebase tickets to Room database so they can be used by the legacy system
     */
    private suspend fun syncTicketsToRoomDb() {
        val userId = authRepository.currentUser.value?.uid ?: return
        
        try {
            // Get all Firebase tickets for this user
            val firebaseTickets = _userTickets.value
            
            if (firebaseTickets.isEmpty()) {
                Log.d(TAG, "No Firebase tickets to sync to Room DB")
                return
            }
            
            // For each Firebase ticket, create/update a Room ticket and UserTicket
            for (fbTicket in firebaseTickets) {
                if (fbTicket.bought && fbTicket.userId == userId) {
                    // First ensure the Ticket exists
                    val roomTicket = Ticket(
                        id = fbTicket.id.hashCode(),  // Generate a consistent int ID from the Firebase ID string
                        eventId = fbTicket.eventId,
                        price = fbTicket.price
                    )
                    
                    // Insert the ticket
                    eventsRepository.insertTicket(roomTicket)
                    
                    // Format the purchase date string
                    val purchaseDateString = fbTicket.purchaseDate?.let {
                        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(it.toDate())
                    } ?: SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                    
                    // Create and insert the UserTicket
                    val userTicket = UserTicket(
                        userId = userId,
                        ticketId = roomTicket.id,
                        purchaseDate = purchaseDateString,
                        isActive = fbTicket.bought
                    )
                    
                    eventsRepository.insertUserTicket(userTicket)
                    Log.d(TAG, "Synced Firebase ticket ${fbTicket.id} to Room DB")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing tickets to Room DB: ${e.message}", e)
        }
    }
    
    fun generateTicketsForEvent(event: Event, price: Double, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            _loading.value = true
            val success = ticketRepository.generateTicketsForEvent(event, price)
            _loading.value = false
            onComplete(success)
        }
    }
    
    fun purchaseTicket(eventId: Int, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            _loading.value = true
            val success = ticketRepository.purchaseTicket(eventId)
            
            if (success) {
                // Refresh ticket status and user tickets
                ticketRepository.refreshUserTickets()
                _userTickets.value = ticketRepository.userTickets.value
                
                // Update remaining tickets count for this event
                updateRemainingTicketCount(eventId)
                
                // Sync to Room DB
                syncTicketsToRoomDb()
            }
            
            _loading.value = false
            onComplete(success)
        }
    }
    
    fun getRemainingTickets(eventId: Int) {
        viewModelScope.launch {
            updateRemainingTicketCount(eventId)
        }
    }
    
    private suspend fun updateRemainingTicketCount(eventId: Int) {
        val count = ticketRepository.getRemainingTicketCount(eventId)
        _remainingTickets.value = _remainingTickets.value.toMutableMap().apply {
            put(eventId, count)
        }
    }
    
    companion object {
        fun factory(): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(FirebaseTicketViewModel::class.java)) {
                    return FirebaseTicketViewModel(
                        FirebaseTicketRepository.getInstance(),
                        AWAVApplication.appContainer.eventsRepository,
                        AuthRepository.getInstance()
                    ) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
} 