package pt.ua.deti.icm.awav.data.model

import kotlinx.coroutines.flow.Flow

interface StandsRepository {
    fun getAllStandsStream() : Flow<List<Stand>>
    fun getStandStream(id : Int): Flow<Stand?>
    suspend fun insertStand(stand: Stand)
    suspend fun deleteStand(stand: Stand)
    suspend fun updateStand(stand: Stand)
}