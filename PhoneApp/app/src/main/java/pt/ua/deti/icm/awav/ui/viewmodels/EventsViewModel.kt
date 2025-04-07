package pt.ua.deti.icm.awav.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pt.ua.deti.icm.awav.AWAVApplication
import pt.ua.deti.icm.awav.data.repository.EventsRepository
import pt.ua.deti.icm.awav.data.room.entity.Event

class EventsViewModel : ViewModel() {
    private val TAG = "EventsViewModel"
    
    // Repository
    private val eventsRepository: EventsRepository = AWAVApplication.appContainer.eventsRepository
    
    // UI state
    private val _activeEvents = MutableStateFlow<List<Event>>(emptyList())
    val activeEvents: StateFlow<List<Event>> = _activeEvents.asStateFlow()
    
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // Initialize
    init {
        loadEvents()
    }
    
    // Load active events
    private fun loadEvents() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                eventsRepository.getActiveEvents().collect { events ->
                    _activeEvents.value = events
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading events: ${e.message}")
                _isLoading.value = false
            }
        }
    }
} 