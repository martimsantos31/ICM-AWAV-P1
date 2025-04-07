package pt.ua.deti.icm.awav.data.model

import com.google.firebase.Timestamp

/**
 * Model for tickets stored in Firebase
 */
data class FirebaseTicket(
    val id: String = "", // Firebase document ID
    val eventId: Int = 0,
    val price: Double = 0.0,
    val bought: Boolean = false,
    val userId: String? = null, // Only set when bought is true
    val purchaseDate: Timestamp? = null, // Only set when bought is true
    val seat: String? = null // Optional seat information
) 