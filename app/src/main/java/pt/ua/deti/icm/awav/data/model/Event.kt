package pt.ua.deti.icm.awav.data.model

import java.util.Date

/**
 * Data class representing an Event in the application
 */
data class Event(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val date: Date = Date(),
    val location: String = "",
    val imageUrl: String = ""
) {
    /**
     * Converts this Event object to a Map for Firestore
     */
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "name" to name,
            "description" to description,
            "date" to date,
            "location" to location,
            "imageUrl" to imageUrl
        )
    }
    
    /**
     * Companion object for creating Event objects from Firestore data
     */
    companion object {
        fun fromMap(map: Map<String, Any?>): Event {
            return Event(
                id = map["id"] as? String ?: "",
                name = map["name"] as? String ?: "",
                description = map["description"] as? String ?: "",
                date = map["date"] as? Date ?: Date(),
                location = map["location"] as? String ?: "",
                imageUrl = map["imageUrl"] as? String ?: ""
            )
        }
    }
} 