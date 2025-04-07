package pt.ua.deti.icm.awav.data.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import pt.ua.deti.icm.awav.data.room.entity.MenuItem
import pt.ua.deti.icm.awav.data.room.entity.Stand
import pt.ua.deti.icm.awav.data.room.entity.Worker

@Dao
interface StandDao {
    @Insert
    suspend fun insertStand(stand: Stand): Long
    
    @Insert
    suspend fun insertMenuItem(menuItem: MenuItem)
    
    @Insert
    suspend fun insertWorker(worker: Worker)

    @Update
    suspend fun updateStand(stand: Stand)

    @Update
    suspend fun updateMenuItem(menuItem: MenuItem)

    @Delete
    suspend fun deleteStand(stand: Stand)

    @Delete
    suspend fun deleteMenuItem(menuItem: MenuItem)

    @Query("DELETE FROM MenuItem WHERE id = :menuItemId")
    suspend fun deleteMenuItemById(menuItemId: String)

    @Query("SELECT * FROM stand")
    fun getAllStands() : Flow<List<Stand>>

    @Query("SELECT * FROM stand WHERE eventId = :eventId")
    suspend fun getStandsForEvent(eventId: Int): List<Stand>

    @Query("SELECT * FROM Stand WHERE id = :id")
    fun getStandById(id: Int): Flow<Stand>
    
    @Query("SELECT * FROM MenuItem WHERE standId = :standId")
    fun getMenuItemsForStand(standId: Int): Flow<List<MenuItem>>
    
    @Query("SELECT * FROM Worker WHERE standId = :standId")
    fun getWorkersForStand(standId: Int): Flow<List<Worker>>
}