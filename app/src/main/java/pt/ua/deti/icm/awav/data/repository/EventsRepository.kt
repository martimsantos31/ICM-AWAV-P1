package pt.ua.deti.icm.awav.data.repository

import kotlinx.coroutines.flow.Flow
import pt.ua.deti.icm.awav.data.room.entity.Event
import pt.ua.deti.icm.awav.data.room.entity.Presenters
import pt.ua.deti.icm.awav.data.room.entity.ScheduleItem
import pt.ua.deti.icm.awav.data.room.entity.Ticket

interface EventsRepository {
    suspend fun insertEvent(event: Event): Long
    suspend fun insertScheduleItem(scheduleItem: ScheduleItem): Long
    suspend fun insertPresenter(presenter: Presenters)
    suspend fun insertTicket(ticket: Ticket)
    suspend fun updateEvent(event: Event)
    suspend fun deleteEvent(event: Event)
    fun getEventById(id: Int): Flow<Event>
    fun getActiveEvents(): Flow<List<Event>>
    fun getScheduleItemsForEvent(eventId: Int): Flow<List<ScheduleItem>>
    fun getPresentersForScheduleItem(scheduleItemId: Int): Flow<List<Presenters>>
    fun getTicketsForEvent(eventId: Int): Flow<List<Ticket>>
}