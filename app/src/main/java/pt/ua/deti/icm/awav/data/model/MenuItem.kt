package pt.ua.deti.icm.awav.data.model

/**
 * Data class representing a menu item for a stand
 */
data class MenuItem(
    val id: String = "",
    val standId: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val description: String = "",
    val isAvailable: Boolean = true,
    val imageUrl: String = ""
) {
    /**
     * Converts this MenuItem object to a Map for Firestore
     */
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "standId" to standId,
            "name" to name,
            "price" to price,
            "description" to description,
            "isAvailable" to isAvailable,
            "imageUrl" to imageUrl
        )
    }
    
    /**
     * Companion object for creating MenuItem objects from Firestore data
     */
    companion object {
        fun fromMap(map: Map<String, Any?>): MenuItem {
            return MenuItem(
                id = map["id"] as? String ?: "",
                standId = map["standId"] as? String ?: "",
                name = map["name"] as? String ?: "",
                price = (map["price"] as? Number)?.toDouble() ?: 0.0,
                description = map["description"] as? String ?: "",
                isAvailable = map["isAvailable"] as? Boolean ?: true,
                imageUrl = map["imageUrl"] as? String ?: ""
            )
        }
    }
} 