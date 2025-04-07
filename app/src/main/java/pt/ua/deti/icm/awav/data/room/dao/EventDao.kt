package pt.ua.deti.icm.awav.data.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import pt.ua.deti.icm.awav.data.room.entity.Event
import pt.ua.deti.icm.awav.data.room.entity.Presenters
import pt.ua.deti.icm.awav.data.room.entity.ScheduleItem
import pt.ua.deti.icm.awav.data.room.entity.Ticket

@Dao
interface EventDao {
    @Insert
    suspend fun insertEvent(event: Event): Long
    
    @Insert
    suspend fun insertScheduleItem(scheduleItem: ScheduleItem): Long
    
    @Insert
    suspend fun insertPresenter(presenter: Presenters)
    
    @Insert
    suspend fun insertTicket(ticket: Ticket)

    @Update
    suspend fun updateEvent(event: Event)

    @Delete
    suspend fun deleteEvent(event: Event)

    @Query("SELECT * FROM events WHERE name = :name")
    suspend fun getEventByName(name: String): Event?
    
    @Query("SELECT * FROM events WHERE id = :id")
    fun getEventById(id: Int): Flow<Event>

    @Query("SELECT * FROM events WHERE isActive = 1")
    fun getActiveEvents(): Flow<List<Event>>


    
    @Query("SELECT * FROM ScheduleItem WHERE eventId = :eventId")
    fun getScheduleItemsForEvent(eventId: Int): Flow<List<ScheduleItem>>
    
    @Query("SELECT * FROM Presenters WHERE scheduleItemId = :scheduleItemId")
    fun getPresentersForScheduleItem(scheduleItemId: Int): Flow<List<Presenters>>
    
    @Query("SELECT * FROM Ticket WHERE eventId = :eventId")
    fun getTicketsForEvent(eventId: Int): Flow<List<Ticket>>
}