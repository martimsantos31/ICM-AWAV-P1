package pt.ua.deti.icm.awav.data.model

class OfflineStandsRepository(private val standDao: StandDao) : StandsRepository {
    override fun getAllStandsStream() : Flow<List<Item>> = standDao.getAllStands()

    override fun getStandStream(id: Int): Flow<Stand?> = standDao.getStand(id)

    override suspend fun insertStand(stand: Stand) = standDao.insert(stand)

    override suspend fun updateStand(stand: Stand) = standDao.update(stand)

    override suspend fun deleteStand(stand: Stand) = standDao.delete(stand)
}