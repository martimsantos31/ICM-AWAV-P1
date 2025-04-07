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
import pt.ua.deti.icm.awav.data.repository.StandsRepository
import pt.ua.deti.icm.awav.data.room.entity.Event
import pt.ua.deti.icm.awav.data.room.entity.ScheduleItem
import pt.ua.deti.icm.awav.data.room.entity.Stand
import java.text.SimpleDateFormat
import java.util.*

class EventDetailsViewModel : ViewModel() {
    // Get repositories from the application container
    private val eventsRepository: EventsRepository = AWAVApplication.appContainer.eventsRepository
    private val standsRepository: StandsRepository = AWAVApplication.appContainer.standsRepository
    
    // UI State
    private val _event = MutableStateFlow<Event?>(null)
    val event: StateFlow<Event?> = _event.asStateFlow()
    
    private val _stands = MutableStateFlow<List<Stand>>(emptyList())
    val stands: StateFlow<List<Stand>> = _stands.asStateFlow()
    
    private val _scheduleItems = MutableStateFlow<List<ScheduleItem>>(emptyList())
    val scheduleItems: StateFlow<List<ScheduleItem>> = _scheduleItems.asStateFlow()
    
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // Load event data
    fun loadEvent(eventId: Int) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                // Load event
                println("DEBUG: Loading event with ID $eventId")
                val eventData = withContext(Dispatchers.IO) {
                    eventsRepository.getEventById(eventId).first()
                }
                println("DEBUG: Event loaded: $eventData")
                _event.value = eventData
                
                // Load stands for this event
                loadStands(eventId)
                
                // Load schedule items for this event
                loadScheduleItems(eventId)
            } catch (e: Exception) {
                // Handle error
                println("DEBUG: Error loading event: ${e.message}")
                e.printStackTrace()
                _event.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    // Load stands for an event
    private suspend fun loadStands(eventId: Int) {
        try {
            println("DEBUG: Loading stands for event $eventId")
            val standsForEvent = withContext(Dispatchers.IO) {
                standsRepository.getStandsForEvent(eventId)
            }
            println("DEBUG: Stands loaded: ${standsForEvent.size}")
            _stands.value = standsForEvent
        } catch (e: Exception) {
            // Handle error
            println("DEBUG: Error loading stands: ${e.message}")
            e.printStackTrace()
            _stands.value = emptyList()
        }
    }
    
    // Load schedule items for an event
    private suspend fun loadScheduleItems(eventId: Int) {
        try {
            println("DEBUG: Loading schedule items for event $eventId")
            val scheduleItemsForEvent = withContext(Dispatchers.IO) {
                eventsRepository.getScheduleItemsForEvent(eventId).first()
            }
            println("DEBUG: Schedule items loaded: ${scheduleItemsForEvent.size}")
            _scheduleItems.value = scheduleItemsForEvent
        } catch (e: Exception) {
            // Handle error
            println("DEBUG: Error loading schedule items: ${e.message}")
            e.printStackTrace()
            _scheduleItems.value = emptyList()
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
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    // Add a stand
    suspend fun addStand(name: String, description: String, eventId: Int) {
        withContext(Dispatchers.IO) {
            try {
                val newStand = Stand(
                    name = name,
                    description = description,
                    eventId = eventId
                )
                val id = standsRepository.insertStand(newStand)
                
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
    
    // Add a schedule item
    suspend fun addScheduleItem(title: String, startTime: String, endTime: String, location: String, eventId: Int) {
        withContext(Dispatchers.IO) {
            try {
                val newScheduleItem = ScheduleItem(
                    id = 0, // Room will auto-generate the ID
                    eventId = eventId,
                    title = title,
                    startTime = startTime,
                    endTime = endTime,
                    location = location
                )
                eventsRepository.insertScheduleItem(newScheduleItem)
                
                // Refresh schedule items list
                loadScheduleItems(eventId)
            } catch (e: Exception) {
                // Handle error
                println("DEBUG: Error adding schedule item: ${e.message}")
                e.printStackTrace()
            }
        }
    }
    
    // Update a schedule item
    suspend fun updateScheduleItem(scheduleItem: ScheduleItem) {
        withContext(Dispatchers.IO) {
            try {
                eventsRepository.updateScheduleItem(scheduleItem)
                
                // Refresh schedule items list
                loadScheduleItems(scheduleItem.eventId)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    // Delete a schedule item
    suspend fun deleteScheduleItem(scheduleItem: ScheduleItem) {
        withContext(Dispatchers.IO) {
            try {
                eventsRepository.deleteScheduleItem(scheduleItem)
                
                // Refresh schedule items list
                loadScheduleItems(scheduleItem.eventId)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
} 