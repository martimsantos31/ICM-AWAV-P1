package pt.ua.deti.icm.awav.ui.viewmodels

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
import kotlinx.coroutines.launch
import pt.ua.deti.icm.awav.AWAVApplication
import pt.ua.deti.icm.awav.data.AuthRepository
import pt.ua.deti.icm.awav.data.repository.EventsRepository
import pt.ua.deti.icm.awav.data.room.entity.Event
import pt.ua.deti.icm.awav.data.room.entity.Ticket
import pt.ua.deti.icm.awav.data.room.entity.UserTicket
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TicketViewModel(
    private val eventsRepository: EventsRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

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
        viewModelScope.launch {
            // Get current user
            authRepository.currentUser.collectLatest { user ->
                user?.uid?.let { userId ->
                    _currentUserId.value = userId
                    loadUserTickets(userId)
                    checkHasActiveTickets(userId)
                }
            }
        }
        
        // Load active events for purchase
        loadActiveEvents()
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
    
    private fun checkHasActiveTickets(userId: String) {
        viewModelScope.launch {
            eventsRepository.getActiveTicketCount(userId).collectLatest { count ->
                _hasActiveTickets.value = count > 0
            }
        }
    }
    
    /**
     * Purchase a ticket for an event
     */
    suspend fun purchaseTicket(ticket: Ticket): Boolean {
        val userId = _currentUserId.value ?: return false
        _loading.value = true
        
        return try {
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
            
            eventsRepository.insertUserTicket(userTicket)
            
            // Reload user tickets
            loadUserTickets(userId)
            checkHasActiveTickets(userId)
            
            _loading.value = false
            true
        } catch (e: Exception) {
            _loading.value = false
            false
        }
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