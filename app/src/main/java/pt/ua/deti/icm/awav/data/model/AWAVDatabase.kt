package pt.ua.deti.icm.awav.data.model

@Database(entities = [Stand::class], version = 1, exportSchema = false)
abstract class AWAVDatabase : RoomDatabase() {
    abstract fun itemDao() : ItemDao
}

companion object {
    @Volatile
    private var Instance: AWAVDatabase? = null
    fun getDatabase(context: Context): AWAVDatabase {
        return Instance ?: synchronized(this) {
            Room.databaseBuilder(context, AWAVDatabase::class.java, "stand_database")
                .build()
                .also { Instance = it }
        }
    }
}