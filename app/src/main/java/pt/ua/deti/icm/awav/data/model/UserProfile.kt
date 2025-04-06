package pt.ua.deti.icm.awav.data.model

enum class UserRole {
    PARTICIPANT,   // Regular event attendee
    STAND_WORKER,  // Staff member working at a stand
    ORGANIZER      // Event organizer with admin privileges
}

data class UserProfile(
    val id: String = "",
    val email: String = "",
    val name: String = "",
    val photoUrl: String? = null,
    val phoneNumber: String? = null,
    val nfcTagId: String? = null,
    val walletBalance: Double = 0.0,
    val attendedEvents: List<String> = emptyList(),
    val upcomingEvents: List<String> = emptyList(),
    val role: UserRole = UserRole.PARTICIPANT,
    val managedStandIds: List<String> = emptyList(),  // For STAND_WORKER
    val organizedEventIds: List<String> = emptyList(), // For ORGANIZER
    val preferences: UserPreferences = UserPreferences()
)

data class UserPreferences(
    val notificationsEnabled: Boolean = true,
    val locationSharingEnabled: Boolean = true,
    val darkModeEnabled: Boolean = false,
    val language: String = "en"
) 