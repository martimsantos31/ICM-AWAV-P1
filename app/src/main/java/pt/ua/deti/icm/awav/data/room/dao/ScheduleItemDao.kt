package pt.ua.deti.icm.awav.data.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import pt.ua.deti.icm.awav.data.room.entity.ScheduleItem

@Dao
interface ScheduleItemDao {
    @Query("SELECT * FROM scheduleitem WHERE eventId = :eventId ORDER BY startTime")
    suspend fun getScheduleItemsForEvent(eventId: Int): List<ScheduleItem>
    
    @Query("SELECT * FROM scheduleitem WHERE eventId = :eventId ORDER BY startTime")
    fun getScheduleItemsForEventFlow(eventId: Int): Flow<List<ScheduleItem>>
    
    @Query("SELECT * FROM scheduleitem WHERE id = :id")
    suspend fun getScheduleItemById(id: Int): ScheduleItem?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScheduleItem(scheduleItem: ScheduleItem): Long
    
    @Update
    suspend fun updateScheduleItem(scheduleItem: ScheduleItem)
    
    @Delete
    suspend fun deleteScheduleItem(scheduleItem: ScheduleItem)
}