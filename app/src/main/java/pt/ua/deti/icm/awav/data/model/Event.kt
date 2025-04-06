package pt.ua.deti.icm.awav.data.model

import java.util.Date

data class Event(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val location: String = "",
    val imageUrl: String? = null,
    val startDate: Date = Date(),
    val endDate: Date = Date(),
    val organizerId: String = "",
    val standIds: List<String> = emptyList(),
    val ticketTypes: List<TicketType> = emptyList(),
    val schedule: List<ScheduleItem> = emptyList(),
    val isActive: Boolean = true
)

data class TicketType(
    val id: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val description: String = "",
    val availableQuantity: Int = 0,
    val soldQuantity: Int = 0
)

data class ScheduleItem(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val startTime: Date = Date(),
    val endTime: Date = Date(),
    val location: String = "",
    val presenters: List<String> = emptyList()
) 