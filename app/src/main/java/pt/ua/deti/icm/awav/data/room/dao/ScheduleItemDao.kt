package pt.ua.deti.icm.awav.data.room.dao

import androidx.room.Dao
import androidx.room.Query
import pt.ua.deti.icm.awav.data.room.entity.ScheduleItem

@Dao
interface ScheduleItemDao {
    @Query("SELECT * FROM scheduleitem WHERE eventId = :eventId ORDER BY startTime")
    suspend fun getScheduleItemsForEvent(eventId: Int): List<ScheduleItem>
}