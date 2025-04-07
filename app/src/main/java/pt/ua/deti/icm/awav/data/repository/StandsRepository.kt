package pt.ua.deti.icm.awav.data.repository

import kotlinx.coroutines.flow.Flow
import pt.ua.deti.icm.awav.data.room.entity.MenuItem
import pt.ua.deti.icm.awav.data.room.entity.Stand
import pt.ua.deti.icm.awav.data.room.entity.Worker

interface StandsRepository {
    suspend fun insertStand(stand: Stand): Long
    suspend fun insertMenuItem(menuItem: MenuItem)
    suspend fun insertWorker(worker: Worker): Long
    suspend fun updateStand(stand: Stand)
    suspend fun deleteStand(stand: Stand)
    suspend fun deleteWorker(worker: Worker)
    fun getStandById(id: Int): Flow<Stand>
    fun getMenuItemsForStand(standId: Int): Flow<List<MenuItem>>
    fun getWorkersForStand(standId: Int): Flow<List<Worker>>
    suspend fun getStandsForEvent(eventId: Int): List<Stand>
    suspend fun getAllStands(): List<Stand>
}