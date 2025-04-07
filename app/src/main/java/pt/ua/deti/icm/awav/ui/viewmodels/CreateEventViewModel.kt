package pt.ua.deti.icm.awav.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pt.ua.deti.icm.awav.awavApplication
import pt.ua.deti.icm.awav.data.repository.EventsRepository
import pt.ua.deti.icm.awav.data.repository.StandsRepository
import pt.ua.deti.icm.awav.data.room.entity.Event
import pt.ua.deti.icm.awav.data.room.entity.Stand
import pt.ua.deti.icm.awav.ui.screens.organizer.StandWithoutId

class CreateEventViewModel : ViewModel() {
    // Get repositories from the application container
    private val eventsRepository: EventsRepository = awavApplication.appContainer.eventsRepository
    private val standsRepository: StandsRepository = awavApplication.appContainer.standsRepository
    
    /**
     * Creates an event with its associated stands in a single operation
     * @param event The event to create
     * @param stands The list of stands (without IDs) to associate with the event
     */
    suspend fun createEventWithStands(event: Event, stands: List<StandWithoutId>) {
        withContext(Dispatchers.IO) {
            try {
                // Insert the event first and get its ID
                val eventId = eventsRepository.insertEvent(event)
                
                // Now create and insert each stand with the event ID
                for (standData in stands) {
                    val stand = Stand(
                        name = standData.name,
                        description = standData.description,
                        eventId = eventId.toInt()
                    )
                    standsRepository.insertStand(stand)
                }
            } catch (e: Exception) {
                // Log or handle the error
                throw e // Rethrow to let the UI handle it
            }
        }
    }
} 