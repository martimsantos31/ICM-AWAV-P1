package pt.ua.deti.icm.awav.data.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import pt.ua.deti.icm.awav.data.room.dao.EventDao
import pt.ua.deti.icm.awav.data.room.dao.ScheduleItemDao
import pt.ua.deti.icm.awav.data.room.dao.StandDao
import pt.ua.deti.icm.awav.data.room.entity.Event
import pt.ua.deti.icm.awav.data.room.entity.MenuItem
import pt.ua.deti.icm.awav.data.room.entity.Presenters
import pt.ua.deti.icm.awav.data.room.entity.ScheduleItem
import pt.ua.deti.icm.awav.data.room.entity.Stand
import pt.ua.deti.icm.awav.data.room.entity.Ticket
import pt.ua.deti.icm.awav.data.room.entity.Worker

@Database(
    entities = [
        Event::class, 
        Stand::class,
        MenuItem::class,
        Worker::class,
        ScheduleItem::class,
        Presenters::class,
        Ticket::class
    ], 
    version = 2, 
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun eventDao() : EventDao
    abstract fun standDao() : StandDao
    abstract fun scheduleItemDao() : ScheduleItemDao

    companion object {
        @Volatile
        private var Instance: AppDatabase? = null

        // Migration from version 1 to 2: Add waitTimeMinutes column to Stand table
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE Stand ADD COLUMN waitTimeMinutes INTEGER NOT NULL DEFAULT 0")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, AppDatabase::class.java, "awav_database")
                    .fallbackToDestructiveMigration()
                    .addMigrations(MIGRATION_1_2)
                    .build()
                    .also { Instance = it }
            }
        }
    }
}