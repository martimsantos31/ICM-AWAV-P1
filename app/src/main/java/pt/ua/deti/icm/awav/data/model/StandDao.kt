package pt.ua.deti.icm.awav.data.model

@Dao
interface StandDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(stand: Stand)

    @Update
    suspend fun update(stand: Stand)

    @Delete
    suspend fun delete(stand: Stand)

    @Query("SELECT * from stands WHERE id = :id")
    fun getStand(id: Int): Flow<Stand>

    @Query("SELECT * from stands ORDER BY name ASC")
    fun getAllStands(): Flow<List<Stand>>
}