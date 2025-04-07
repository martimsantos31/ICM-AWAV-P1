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
    suspend fun insertWorker(worker: Worker): Long

    @Update
    suspend fun updateStand(stand: Stand)

    @Delete
    suspend fun deleteStand(stand: Stand)
    
    @Delete
    suspend fun deleteWorker(worker: Worker)

    @Query("SELECT * FROM stand WHERE eventId = :eventId")
    suspend fun getStandsForEvent(eventId: Int): List<Stand>

    @Query("SELECT * FROM Stand WHERE id = :id")
    fun getStandById(id: Int): Flow<Stand>
    
    @Query("SELECT * FROM MenuItem WHERE standId = :standId")
    fun getMenuItemsForStand(standId: Int): Flow<List<MenuItem>>
    
    @Query("SELECT * FROM Worker WHERE standId = :standId")
    fun getWorkersForStand(standId: Int): Flow<List<Worker>>
    
    @Query("SELECT * FROM Stand")
    suspend fun getAllStands(): List<Stand>
}