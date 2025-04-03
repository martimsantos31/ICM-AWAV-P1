package pt.ua.deti.icm.awav.data.model

data class Message(
    val id: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val content: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val type: MessageType = MessageType.TEXT,
    val mediaUrl: String? = null,
    val location: SimpleLocation? = null
)

enum class MessageType {
    TEXT,
    IMAGE,
    LOCATION
}

data class SimpleLocation(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)
