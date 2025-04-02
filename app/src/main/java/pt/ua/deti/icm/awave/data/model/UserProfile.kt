package pt.ua.deti.icm.awave.data.model

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
    val preferences: UserPreferences = UserPreferences()
)

data class UserPreferences(
    val notificationsEnabled: Boolean = true,
    val locationSharingEnabled: Boolean = true,
    val darkModeEnabled: Boolean = false,
    val language: String = "en"
) 