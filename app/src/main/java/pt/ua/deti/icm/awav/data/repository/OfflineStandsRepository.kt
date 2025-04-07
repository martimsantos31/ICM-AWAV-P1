package pt.ua.deti.icm.awav.data.repository

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import pt.ua.deti.icm.awav.data.room.dao.StandDao
import pt.ua.deti.icm.awav.data.room.entity.MenuItem
import pt.ua.deti.icm.awav.data.room.entity.Stand
import pt.ua.deti.icm.awav.data.room.entity.Worker

class OfflineStandsRepository(private val standDao: StandDao) : StandsRepository {
    override fun getStandById(id: Int): Flow<Stand> = standDao.getStandById(id)
        .catch { e -> 
            Log.e("StandsRepository", "Error getting stand by ID $id: ${e.message}", e)
            throw e
        }
    
    override fun getMenuItemsForStand(standId: Int): Flow<List<MenuItem>> = 
        standDao.getMenuItemsForStand(standId)
    
    override fun getWorkersForStand(standId: Int): Flow<List<Worker>> = 
        standDao.getWorkersForStand(standId)

    override suspend fun insertStand(stand: Stand): Long {
        try {
            Log.d("StandsRepository", "Inserting stand: $stand for event ID: ${stand.eventId}")
            val id = standDao.insertStand(stand)
            Log.d("StandsRepository", "Stand inserted successfully with ID: $id")
            return id
        } catch (e: Exception) {
            Log.e("StandsRepository", "Error inserting stand: ${e.message}", e)
            throw e
        }
    }
    
    override suspend fun insertMenuItem(menuItem: MenuItem) {
        try {
            standDao.insertMenuItem(menuItem)
            Log.d("StandsRepository", "Menu item inserted successfully")
        } catch (e: Exception) {
            Log.e("StandsRepository", "Error inserting menu item: ${e.message}", e)
            throw e
        }
    }
    
    override suspend fun insertWorker(worker: Worker) {
        try {
            standDao.insertWorker(worker)
            Log.d("StandsRepository", "Worker inserted successfully")
        } catch (e: Exception) {
            Log.e("StandsRepository", "Error inserting worker: ${e.message}", e)
            throw e
        }
    }

    override suspend fun updateStand(stand: Stand) {
        try {
            standDao.updateStand(stand)
            Log.d("StandsRepository", "Stand updated successfully: ${stand.id}")
        } catch (e: Exception) {
            Log.e("StandsRepository", "Error updating stand: ${e.message}", e)
            throw e
        }
    }

    override suspend fun deleteStand(stand: Stand) {
        try {
            standDao.deleteStand(stand)
            Log.d("StandsRepository", "Stand deleted successfully: ${stand.id}")
        } catch (e: Exception) {
            Log.e("StandsRepository", "Error deleting stand: ${e.message}", e)
            throw e
        }
    }
    
    override suspend fun getStandsForEvent(eventId: Int): List<Stand> {
        try {
            val stands = standDao.getStandsForEvent(eventId)
            Log.d("StandsRepository", "Got ${stands.size} stands for event $eventId")
            return stands
        } catch (e: Exception) {
            Log.e("StandsRepository", "Error getting stands for event $eventId: ${e.message}", e)
            throw e
        }
    }
}

