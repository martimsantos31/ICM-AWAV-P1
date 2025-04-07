package pt.ua.deti.icm.awav.data.repository

import kotlinx.coroutines.flow.Flow
import pt.ua.deti.icm.awav.data.room.dao.EventDao
import pt.ua.deti.icm.awav.data.room.entity.Event
import pt.ua.deti.icm.awav.data.room.entity.Presenters
import pt.ua.deti.icm.awav.data.room.entity.ScheduleItem
import pt.ua.deti.icm.awav.data.room.entity.Ticket

class OfflineEventsRepository(private val eventDao: EventDao) : EventsRepository {
    override fun getActiveEvents(): Flow<List<Event>> = eventDao.getActiveEvents()
    
    override fun getEventById(id: Int): Flow<Event> = eventDao.getEventById(id)
    
    override fun getScheduleItemsForEvent(eventId: Int): Flow<List<ScheduleItem>> = 
        eventDao.getScheduleItemsForEvent(eventId)
    
    override fun getPresentersForScheduleItem(scheduleItemId: Int): Flow<List<Presenters>> = 
        eventDao.getPresentersForScheduleItem(scheduleItemId)
    
    override fun getTicketsForEvent(eventId: Int): Flow<List<Ticket>> = 
        eventDao.getTicketsForEvent(eventId)

    override suspend fun insertEvent(event: Event): Long = eventDao.insertEvent(event)
    
    override suspend fun insertScheduleItem(scheduleItem: ScheduleItem): Long = 
        eventDao.insertScheduleItem(scheduleItem)
    
    override suspend fun insertPresenter(presenter: Presenters) = 
        eventDao.insertPresenter(presenter)
    
    override suspend fun insertTicket(ticket: Ticket) = 
        eventDao.insertTicket(ticket)

    override suspend fun updateEvent(event: Event) = eventDao.updateEvent(event)

    override suspend fun deleteEvent(event: Event) = eventDao.deleteEvent(event)
}

