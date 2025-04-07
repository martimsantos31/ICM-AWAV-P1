package pt.ua.deti.icm.awav.data.room

import androidx.room.TypeConverter
import java.util.Date

class Converters {
    // Date converters
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}