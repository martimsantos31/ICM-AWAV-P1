package pt.ua.deti.icm.awav.data.room.converter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import pt.ua.deti.icm.awav.data.room.entity.MenuItem
import pt.ua.deti.icm.awav.data.room.entity.ScheduleItem
import pt.ua.deti.icm.awav.data.room.entity.Stand
import pt.ua.deti.icm.awav.data.room.entity.Ticket
import pt.ua.deti.icm.awav.data.room.entity.Worker
import java.util.Date

class RoomTypeConverters {
    private val gson = Gson()
    
    // Date converters
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
    
    // Ticket converters
    @TypeConverter
    fun fromTicket(ticket: Ticket): String {
        return gson.toJson(ticket)
    }

    @TypeConverter
    fun toTicket(ticketString: String): Ticket {
        val type = object : TypeToken<Ticket>() {}.type
        return gson.fromJson(ticketString, type)
    }
    
    // Stand list converters
    @TypeConverter
    fun fromStandList(stands: List<Stand>): String {
        return gson.toJson(stands)
    }

    @TypeConverter
    fun toStandList(standsString: String): List<Stand> {
        val type = object : TypeToken<List<Stand>>() {}.type
        return gson.fromJson(standsString, type)
    }
    
    // Schedule item list converters
    @TypeConverter
    fun fromScheduleItemList(scheduleItems: List<ScheduleItem>): String {
        return gson.toJson(scheduleItems)
    }

    @TypeConverter
    fun toScheduleItemList(scheduleItemsString: String): List<ScheduleItem> {
        val type = object : TypeToken<List<ScheduleItem>>() {}.type
        return gson.fromJson(scheduleItemsString, type)
    }
    
    // String list converters
    @TypeConverter
    fun fromStringList(strings: List<String>): String {
        return gson.toJson(strings)
    }

    @TypeConverter
    fun toStringList(stringsString: String): List<String> {
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(stringsString, type)
    }
    
    // MenuItem list converters
    @TypeConverter
    fun fromMenuItemList(menuItems: List<MenuItem>): String {
        return gson.toJson(menuItems)
    }

    @TypeConverter
    fun toMenuItemList(menuItemsString: String): List<MenuItem> {
        val type = object : TypeToken<List<MenuItem>>() {}.type
        return gson.fromJson(menuItemsString, type)
    }
    
    // Worker list converters
    @TypeConverter
    fun fromWorkerList(workers: List<Worker>): String {
        return gson.toJson(workers)
    }

    @TypeConverter
    fun toWorkerList(workersString: String): List<Worker> {
        val type = object : TypeToken<List<Worker>>() {}.type
        return gson.fromJson(workersString, type)
    }
} 