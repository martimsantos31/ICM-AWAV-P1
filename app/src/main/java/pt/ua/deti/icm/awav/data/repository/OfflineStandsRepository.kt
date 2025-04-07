package pt.ua.deti.icm.awav.data.repository

import kotlinx.coroutines.flow.Flow
import pt.ua.deti.icm.awav.data.room.dao.StandDao
import pt.ua.deti.icm.awav.data.room.entity.MenuItem
import pt.ua.deti.icm.awav.data.room.entity.Stand
import pt.ua.deti.icm.awav.data.room.entity.Worker

class OfflineStandsRepository(private val standDao: StandDao) : StandsRepository {
    override fun getStandById(id: Int): Flow<Stand> = standDao.getStandById(id)
    
    override fun getMenuItemsForStand(standId: Int): Flow<List<MenuItem>> = 
        standDao.getMenuItemsForStand(standId)
    
    override fun getWorkersForStand(standId: Int): Flow<List<Worker>> = 
        standDao.getWorkersForStand(standId)

    override suspend fun insertStand(stand: Stand): Long = standDao.insertStand(stand)
    
    override suspend fun insertMenuItem(menuItem: MenuItem) = 
        standDao.insertMenuItem(menuItem)
    
    override suspend fun insertWorker(worker: Worker) = 
        standDao.insertWorker(worker)

    override suspend fun updateStand(stand: Stand) = standDao.updateStand(stand)

    override suspend fun deleteStand(stand: Stand) = standDao.deleteStand(stand)
    
    override suspend fun getStandsForEvent(eventId: Int): List<Stand> = 
        standDao.getStandsForEvent(eventId)
}

