package pt.ua.deti.icm.awave.data.model

data class Message(
    val id: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val content: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val type: MessageType = MessageType.TEXT,
    val mediaUrl: String? = null,
    val location: Location? = null
)

enum class MessageType {
    TEXT,
    IMAGE,
    LOCATION
}

data class Location(
    val latitude: Double,
    val longitude: Double
) 