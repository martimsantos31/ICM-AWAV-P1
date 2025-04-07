package pt.ua.deti.icm.awav.data.room.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "events")
data class Event(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String,
    val location: String,
    val startDate: String,
    val endDate: String,
    val isActive: Boolean,
    val capacity: Int
)

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Event::class,
            parentColumns = ["id"],
            childColumns = ["eventId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("eventId")]
)
data class ScheduleItem(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val eventId: Int,
    val title: String,
    val startTime: String,
    val endTime: String,
    val location: String = ""
)

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = ScheduleItem::class,
            parentColumns = ["id"],
            childColumns = ["scheduleItemId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("scheduleItemId")]
)
data class Presenters(
    @PrimaryKey val id: Int,
    val scheduleItemId: Int,
    val name: String
)

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Event::class,
            parentColumns = ["id"],
            childColumns = ["eventId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("eventId")]
)
data class Ticket(
    @PrimaryKey val id: Int,
    val eventId: Int,
    val price: Double
)

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Ticket::class,
            parentColumns = ["id"],
            childColumns = ["ticketId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("ticketId")]
)
data class UserTicket(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String,  // Firebase user ID
    val ticketId: Int,   // Reference to the ticket
    val purchaseDate: String,
    val isActive: Boolean = true
)
