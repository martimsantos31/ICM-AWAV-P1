package pt.ua.deti.icm.awav.data.model

/**
 * Data class representing a Stand in the application
 */
data class Stand(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val eventId: String = "",
    val waitTimeMinutes: Int = 0,
    val imageUrl: String = ""
) {
    /**
     * Converts this Stand object to a Map for Firestore
     */
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "name" to name,
            "description" to description,
            "eventId" to eventId,
            "waitTimeMinutes" to waitTimeMinutes,
            "imageUrl" to imageUrl
        )
    }
    
    /**
     * Companion object for creating Stand objects from Firestore data
     */
    companion object {
        fun fromMap(map: Map<String, Any?>): Stand {
            return Stand(
                id = map["id"] as? String ?: "",
                name = map["name"] as? String ?: "",
                description = map["description"] as? String ?: "",
                eventId = map["eventId"] as? String ?: "",
                waitTimeMinutes = (map["waitTimeMinutes"] as? Long)?.toInt() ?: 0,
                imageUrl = map["imageUrl"] as? String ?: ""
            )
        }
    }
} 