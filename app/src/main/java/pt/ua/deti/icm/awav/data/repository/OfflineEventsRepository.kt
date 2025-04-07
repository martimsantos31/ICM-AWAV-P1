package pt.ua.deti.icm.awav.data.repository

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import pt.ua.deti.icm.awav.data.room.dao.EventDao
import pt.ua.deti.icm.awav.data.room.entity.Event
import pt.ua.deti.icm.awav.data.room.entity.Presenters
import pt.ua.deti.icm.awav.data.room.entity.ScheduleItem
import pt.ua.deti.icm.awav.data.room.entity.Ticket
import pt.ua.deti.icm.awav.data.room.entity.UserTicket

class OfflineEventsRepository(private val eventDao: EventDao) : EventsRepository {
    override fun getActiveEvents(): Flow<List<Event>> = eventDao.getActiveEvents()
    
    override fun getEventById(id: Int): Flow<Event> = eventDao.getEventById(id)
        .catch { e -> 
            Log.e("EventsRepository", "Error getting event by ID $id: ${e.message}", e)
            throw e
        }
    
    override fun getScheduleItemsForEvent(eventId: Int): Flow<List<ScheduleItem>> = 
        eventDao.getScheduleItemsForEvent(eventId)
    
    override fun getPresentersForScheduleItem(scheduleItemId: Int): Flow<List<Presenters>> = 
        eventDao.getPresentersForScheduleItem(scheduleItemId)
    
    override fun getTicketsForEvent(eventId: Int): Flow<List<Ticket>> = 
        eventDao.getTicketsForEvent(eventId)

    override fun getUserTickets(userId: String): Flow<List<UserTicket>> =
        eventDao.getUserTickets(userId)
        
    override fun getUserTicketByTicketId(userId: String, ticketId: Int): Flow<UserTicket?> =
        eventDao.getUserTicketByTicketId(userId, ticketId)
        
    override fun getActiveTicketCount(userId: String): Flow<Int> =
        eventDao.getActiveTicketCount(userId)
        
    override fun getEventsForUserTickets(userId: String): Flow<List<Event>> =
        eventDao.getEventsForUserTickets(userId)

    override suspend fun insertEvent(event: Event): Long {
        try {
            Log.d("EventsRepository", "Inserting event: $event")
            val id = eventDao.insertEvent(event)
            Log.d("EventsRepository", "Event inserted successfully with ID: $id")
            return id
        } catch (e: Exception) {
            Log.e("EventsRepository", "Error inserting event: ${e.message}", e)
            throw e
        }
    }
    
    override suspend fun insertScheduleItem(scheduleItem: ScheduleItem): Long = 
        eventDao.insertScheduleItem(scheduleItem)
    
    override suspend fun insertPresenter(presenter: Presenters) = 
        eventDao.insertPresenter(presenter)
    
    override suspend fun insertTicket(ticket: Ticket) = 
        eventDao.insertTicket(ticket)
        
    override suspend fun insertUserTicket(userTicket: UserTicket): Long =
        eventDao.insertUserTicket(userTicket)

    override suspend fun updateEvent(event: Event) {
        try {
            eventDao.updateEvent(event)
            Log.d("EventsRepository", "Event updated successfully: ${event.id}")
        } catch (e: Exception) {
            Log.e("EventsRepository", "Error updating event: ${e.message}", e)
            throw e
        }
    }

    override suspend fun deleteEvent(event: Event) {
        try {
            eventDao.deleteEvent(event)
            Log.d("EventsRepository", "Event deleted successfully: ${event.id}")
        } catch (e: Exception) {
            Log.e("EventsRepository", "Error deleting event: ${e.message}", e)
            throw e
        }
    }
}

