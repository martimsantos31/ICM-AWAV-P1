package pt.ua.deti.icm.awav.ui.viewmodels

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
import pt.ua.deti.icm.awav.data.repository.EventsRepository
import pt.ua.deti.icm.awav.data.repository.FirebaseTicketRepository
import pt.ua.deti.icm.awav.data.repository.StandsRepository
import pt.ua.deti.icm.awav.data.room.entity.Event
import pt.ua.deti.icm.awav.data.room.entity.Stand
import pt.ua.deti.icm.awav.ui.screens.organizer.StandWithoutId

class CreateEventViewModel : ViewModel() {
    // Get repositories from the application container
    private val eventsRepository: EventsRepository = AWAVApplication.appContainer.eventsRepository
    private val standsRepository: StandsRepository = AWAVApplication.appContainer.standsRepository
    private val firebaseTicketRepository: FirebaseTicketRepository = FirebaseTicketRepository.getInstance()
    
    // UI state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    /**
     * Creates an event with its associated stands in a single operation
     * @param event The event to create
     * @param stands The list of stands (without IDs) to associate with the event
     * @param ticketPrice The price for event tickets
     */
    fun createEventWithStands(
        event: Event, 
        stands: List<StandWithoutId>, 
        ticketPrice: Double,
        onComplete: (Boolean) -> Unit
    ) {
        _isLoading.value = true
        _errorMessage.value = null
        
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    // Insert the event first and get its ID
                    val eventId = eventsRepository.insertEvent(event)
                    
                    // Create event with the generated ID for Firebase
                    val eventWithId = event.copy(id = eventId.toInt())
                    
                    // Generate tickets in Firebase
                    val ticketsGenerated = firebaseTicketRepository.generateTicketsForEvent(eventWithId, ticketPrice)
                    
                    if (!ticketsGenerated) {
                        throw Exception("Failed to generate tickets in Firebase")
                    }
                    
                    // Now create and insert each stand with the event ID
                    for (standData in stands) {
                        val stand = Stand(
                            name = standData.name,
                            description = standData.description,
                            eventId = eventId.toInt()
                        )
                        standsRepository.insertStand(stand)
                    }
                }
                
                _isLoading.value = false
                onComplete(true)
            } catch (e: Exception) {
                _errorMessage.value = "Error creating event: ${e.message}"
                _isLoading.value = false
                onComplete(false)
            }
        }
    }
} 