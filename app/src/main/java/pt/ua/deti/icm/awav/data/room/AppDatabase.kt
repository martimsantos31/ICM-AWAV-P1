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
    version = 4, 
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

        // Migration from 1 to 2: Added UserTicket table
        private val MIGRATION_1_2 = object : Migration(1, 2) {
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
        
        // Migration from 2 to 3: Modified entity tables to have auto-generated IDs
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 1. Handle ScheduleItem table
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS ScheduleItem_new (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "eventId INTEGER NOT NULL, " +
                    "title TEXT NOT NULL, " +
                    "startTime TEXT NOT NULL, " +
                    "endTime TEXT NOT NULL, " +
                    "location TEXT NOT NULL DEFAULT '', " +
                    "FOREIGN KEY(eventId) REFERENCES events(id) ON DELETE CASCADE)"
                )
                
                // Copy data from old table to new table (if it exists)
                database.execSQL(
                    "INSERT OR IGNORE INTO ScheduleItem_new (id, eventId, title, startTime, endTime, location) " +
                    "SELECT id, eventId, title, startTime, endTime, location FROM ScheduleItem"
                )
                
                // Drop the old table
                database.execSQL("DROP TABLE IF EXISTS ScheduleItem")
                
                // Rename the new table to the original name
                database.execSQL("ALTER TABLE ScheduleItem_new RENAME TO ScheduleItem")
                
                // Recreate index
                database.execSQL("CREATE INDEX IF NOT EXISTS index_ScheduleItem_eventId ON ScheduleItem (eventId)")
                
                // 2. Handle Presenters table
                database.execSQL("DROP INDEX IF EXISTS index_Presenters_scheduleItemId")
                database.execSQL("DROP TABLE IF EXISTS Presenters")
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS Presenters (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "scheduleItemId INTEGER NOT NULL, " +
                    "name TEXT NOT NULL, " +
                    "FOREIGN KEY(scheduleItemId) REFERENCES ScheduleItem(id) ON DELETE CASCADE)"
                )
                database.execSQL("CREATE INDEX IF NOT EXISTS index_Presenters_scheduleItemId ON Presenters (scheduleItemId)")
                
                // 3. Handle Ticket table
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS Ticket_new (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "eventId INTEGER NOT NULL, " +
                    "price REAL NOT NULL, " +
                    "FOREIGN KEY(eventId) REFERENCES events(id) ON DELETE CASCADE)"
                )
                
                // Copy data from old table to new table
                database.execSQL(
                    "INSERT OR IGNORE INTO Ticket_new (id, eventId, price) " +
                    "SELECT id, eventId, price FROM Ticket"
                )
                
                // Drop the old table
                database.execSQL("DROP TABLE IF EXISTS Ticket")
                
                // Rename the new table to the original name
                database.execSQL("ALTER TABLE Ticket_new RENAME TO Ticket")
                
                // Recreate indexes
                database.execSQL("CREATE INDEX IF NOT EXISTS index_Ticket_eventId ON Ticket (eventId)")
                
                // Update UserTicket foreign key if needed
                database.execSQL("PRAGMA foreign_keys=off")
                database.execSQL("DROP INDEX IF EXISTS index_UserTicket_ticketId")
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS UserTicket_new (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "userId TEXT NOT NULL, " +
                    "ticketId INTEGER NOT NULL, " +
                    "purchaseDate TEXT NOT NULL, " +
                    "isActive INTEGER NOT NULL, " +
                    "FOREIGN KEY(ticketId) REFERENCES Ticket(id) ON DELETE CASCADE)"
                )
                
                // Copy data from old table to new table
                database.execSQL(
                    "INSERT OR IGNORE INTO UserTicket_new (id, userId, ticketId, purchaseDate, isActive) " +
                    "SELECT id, userId, ticketId, purchaseDate, isActive FROM UserTicket"
                )
                
                // Drop the old table
                database.execSQL("DROP TABLE IF EXISTS UserTicket")
                
                // Rename the new table to the original name
                database.execSQL("ALTER TABLE UserTicket_new RENAME TO UserTicket")
                
                // Recreate indexes
                database.execSQL("CREATE INDEX IF NOT EXISTS index_UserTicket_ticketId ON UserTicket (ticketId)")
                database.execSQL("PRAGMA foreign_keys=on")
            }
        }
        
        // Migration from 3 to 4: Add userId field to Worker table
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create a new Worker table with the userId field
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS Worker_new (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "standId INTEGER NOT NULL, " +
                    "name TEXT NOT NULL, " +
                    "userId TEXT NOT NULL DEFAULT '', " +
                    "FOREIGN KEY(standId) REFERENCES Stand(id) ON DELETE CASCADE)"
                )
                
                // Copy data from old table to new table
                database.execSQL(
                    "INSERT INTO Worker_new (id, standId, name) " +
                    "SELECT id, standId, name FROM Worker"
                )
                
                // Drop the old table
                database.execSQL("DROP TABLE Worker")
                
                // Rename the new table to the original name
                database.execSQL("ALTER TABLE Worker_new RENAME TO Worker")
                
                // Recreate index
                database.execSQL("CREATE INDEX IF NOT EXISTS index_Worker_standId ON Worker (standId)")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return Instance ?: synchronized(this) {
                val instance = Room.databaseBuilder(context, AppDatabase::class.java, "awav_database")
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    // This allows a complete rebuild of the database if migration fails
                    // WARNING: This will delete all data if schema version changes don't have matching migrations
                    .fallbackToDestructiveMigration()
                    .build()
                Instance = instance
                instance
            }
        }
    }
}