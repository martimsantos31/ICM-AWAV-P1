package pt.ua.deti.icm.awav.data.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
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
import pt.ua.deti.icm.awav.data.room.entity.UserTicket
import pt.ua.deti.icm.awav.data.room.entity.Worker

@Database(
    entities = [
        Event::class, 
        Stand::class,
        MenuItem::class,
        Worker::class,
        ScheduleItem::class,
        Presenters::class,
        Ticket::class,
        UserTicket::class
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

        fun getDatabase(context: Context): AppDatabase {
            return Instance ?: synchronized(this) {
                val instance = Room.databaseBuilder(context, AppDatabase::class.java, "awav_database")
                .addMigrations(
                        object : androidx.room.migration.Migration(1, 2) {
                            override fun migrate(database: SupportSQLiteDatabase) {
                                database.execSQL(
                                    "CREATE TABLE IF NOT EXISTS UserTicket (" +
                                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                                    "userId TEXT NOT NULL, " +
                                    "ticketId INTEGER NOT NULL, " +
                                    "purchaseDate TEXT NOT NULL, " +
                                    "isActive INTEGER NOT NULL, " +
                                    "FOREIGN KEY(ticketId) REFERENCES Ticket(id) ON DELETE CASCADE)"
                                )
                                database.execSQL("CREATE INDEX IF NOT EXISTS index_UserTicket_ticketId ON UserTicket (ticketId)")
                            }
                        }
                    )
                    .build()
                Instance = instance
                instance
            }
        }
    }
}