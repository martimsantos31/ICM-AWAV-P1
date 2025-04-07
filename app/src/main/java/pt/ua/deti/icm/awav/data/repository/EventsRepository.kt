package pt.ua.deti.icm.awav.data.repository

import kotlinx.coroutines.flow.Flow
import pt.ua.deti.icm.awav.data.room.entity.Event
import pt.ua.deti.icm.awav.data.room.entity.Presenters
import pt.ua.deti.icm.awav.data.room.entity.ScheduleItem
import pt.ua.deti.icm.awav.data.room.entity.Ticket
import pt.ua.deti.icm.awav.data.room.entity.UserTicket

interface EventsRepository {
    suspend fun insertEvent(event: Event): Long
    suspend fun insertScheduleItem(scheduleItem: ScheduleItem): Long
    suspend fun insertPresenter(presenter: Presenters)
    suspend fun insertTicket(ticket: Ticket)
    suspend fun insertUserTicket(userTicket: UserTicket): Long
    suspend fun updateEvent(event: Event)
    suspend fun deleteEvent(event: Event)
    fun getEventById(id: Int): Flow<Event>
    fun getActiveEvents(): Flow<List<Event>>
    fun getScheduleItemsForEvent(eventId: Int): Flow<List<ScheduleItem>>
    fun getPresentersForScheduleItem(scheduleItemId: Int): Flow<List<Presenters>>
    fun getTicketsForEvent(eventId: Int): Flow<List<Ticket>>
    fun getUserTickets(userId: String): Flow<List<UserTicket>>
    fun getUserTicketByTicketId(userId: String, ticketId: Int): Flow<UserTicket?>
    fun getActiveTicketCount(userId: String): Flow<Int>
    fun getEventsForUserTickets(userId: String): Flow<List<Event>>
}