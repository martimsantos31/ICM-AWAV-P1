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
import pt.ua.deti.icm.awav.awavApplication
import pt.ua.deti.icm.awav.data.repository.EventsRepository
import pt.ua.deti.icm.awav.data.repository.StandsRepository
import pt.ua.deti.icm.awav.data.room.entity.Event
import pt.ua.deti.icm.awav.data.room.entity.Stand

class EventDetailsViewModel : ViewModel() {
    // Get repositories from the application container
    private val eventsRepository: EventsRepository = awavApplication.appContainer.eventsRepository
    private val standsRepository: StandsRepository = awavApplication.appContainer.standsRepository
    
    // UI State
    private val _event = MutableStateFlow<Event?>(null)
    val event: StateFlow<Event?> = _event.asStateFlow()
    
    private val _stands = MutableStateFlow<List<Stand>>(emptyList())
    val stands: StateFlow<List<Stand>> = _stands.asStateFlow()
    
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // Load event data
    fun loadEvent(eventId: Int) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                // Load event
                val eventData = withContext(Dispatchers.IO) {
                    eventsRepository.getEventById(eventId).first()
                }
                _event.value = eventData
                
                // Load stands for this event
                loadStands(eventId)
            } catch (e: Exception) {
                // Handle error
                _event.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    // Load stands for an event
    private suspend fun loadStands(eventId: Int) {
        try {
            val standsForEvent = withContext(Dispatchers.IO) {
                standsRepository.getStandsForEvent(eventId)
            }
            _stands.value = standsForEvent
        } catch (e: Exception) {
            // Handle error
            _stands.value = emptyList()
        }
    }
    
    // Update event
    suspend fun updateEvent(event: Event) {
        withContext(Dispatchers.IO) {
            try {
                eventsRepository.updateEvent(event)
                _event.value = event
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    // Delete event
    suspend fun deleteEvent(event: Event) {
        withContext(Dispatchers.IO) {
            try {
                eventsRepository.deleteEvent(event)
                _event.value = null
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    // Add a stand to the event
    suspend fun addStand(name: String, description: String, eventId: Int) {
        withContext(Dispatchers.IO) {
            try {
                val stand = Stand(
                    name = name,
                    description = description,
                    eventId = eventId
                )
                standsRepository.insertStand(stand)
                
                // Refresh stands list
                loadStands(eventId)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    // Update a stand
    suspend fun updateStand(stand: Stand) {
        withContext(Dispatchers.IO) {
            try {
                standsRepository.updateStand(stand)
                
                // Refresh stands list
                loadStands(stand.eventId)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    // Delete a stand
    suspend fun deleteStand(stand: Stand) {
        withContext(Dispatchers.IO) {
            try {
                standsRepository.deleteStand(stand)
                
                // Refresh stands list
                loadStands(stand.eventId)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
} 