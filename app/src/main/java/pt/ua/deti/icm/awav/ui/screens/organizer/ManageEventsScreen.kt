package pt.ua.deti.icm.awav.ui.screens.organizer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import pt.ua.deti.icm.awav.ui.theme.Purple
import java.text.SimpleDateFormat
import java.util.*

// Simple event data class
data class Event(
    val id: String,
    val title: String,
    val description: String,
    val location: String = "DETI UA",
    val startDate: Date,
    val endDate: Date,
    val icon: ImageVector = Icons.Filled.Event,
    val stands: MutableList<Stand> = mutableListOf(),
    val scheduleItems: MutableList<ScheduleItem> = mutableListOf()
)

// Stand data class
data class Stand(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String = "",
    val imageUrl: String? = null,
    val eventId: String = "",
    val location: String = "",
    val icon: ImageVector = Icons.Filled.Store
)

// Schedule Item data class
data class ScheduleItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val time: String,
    val date: Date
)

// Singleton to store events data
object EventsData {
    val events = mutableStateListOf(
        Event(
            id = "1",
            title = "Enterro 25",
            description = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nulla facilisi. Sed euismod, nisl vel ultricies lacinia, nisl nisl aliquam nisl.",
            location = "DETI UA",
            startDate = Calendar.getInstance().apply { 
                set(2023, 3, 26) 
            }.time,
            endDate = Calendar.getInstance().apply { 
                set(2023, 4, 3) 
            }.time,
            icon = Icons.Filled.Celebration,
            stands = mutableListOf(
                Stand(
                    id = "1",
                    name = "Daniel's Chorizo",
                    description = "Traditional Portuguese chorizo sandwiches and more",
                    imageUrl = "chorizo",
                    eventId = "1",
                    location = "Food Court - North"
                ),
                Stand(
                    id = "2",
                    name = "NEI Coffee",
                    description = "Premium coffee and pastries",
                    imageUrl = "coffee",
                    eventId = "1",
                    location = "Main Hall"
                ),
                Stand(
                    id = "3",
                    name = "Tech Booth",
                    description = "Latest tech gadgets and demos",
                    imageUrl = "tech",
                    eventId = "1",
                    location = "Exhibition Area"
                )
            ),
            scheduleItems = mutableListOf(
                ScheduleItem(
                    name = "Opening Ceremony",
                    time = "10:00",
                    date = Calendar.getInstance().apply { 
                        set(2023, 3, 26, 10, 0) 
                    }.time
                ),
                ScheduleItem(
                    name = "Tech Talk: Future of AI",
                    time = "14:00",
                    date = Calendar.getInstance().apply { 
                        set(2023, 3, 26, 14, 0) 
                    }.time
                ),
                ScheduleItem(
                    name = "Workshop: Mobile App Development",
                    time = "16:30",
                    date = Calendar.getInstance().apply { 
                        set(2023, 3, 27, 16, 30) 
                    }.time
                ),
                ScheduleItem(
                    name = "Networking Event",
                    time = "19:00",
                    date = Calendar.getInstance().apply { 
                        set(2023, 3, 28, 19, 0) 
                    }.time
                ),
                ScheduleItem(
                    name = "Closing Party",
                    time = "21:00",
                    date = Calendar.getInstance().apply { 
                        set(2023, 4, 3, 21, 0) 
                    }.time
                )
            )
        ),
        Event(
            id = "2",
            title = "BE NEI",
            description = "The biggest technology event in the university, with workshops, talks, and more.",
            location = "DETI UA",
            startDate = Calendar.getInstance().apply { 
                set(2023, 4, 4) 
            }.time,
            endDate = Calendar.getInstance().apply { 
                set(2023, 4, 5) 
            }.time,
            icon = Icons.Filled.MusicNote,
            stands = mutableListOf(
                Stand(
                    id = "4",
                    name = "Mohamed's Kebab",
                    description = "Authentic Middle Eastern cuisine",
                    imageUrl = "kebab",
                    eventId = "2",
                    location = "Food Court - East"
                ),
                Stand(
                    id = "5",
                    name = "Gaming Zone",
                    description = "Test the latest games and consoles",
                    imageUrl = "gaming",
                    eventId = "2", 
                    location = "Recreation Area"
                ),
                Stand(
                    id = "6",
                    name = "NEECT Food Truck",
                    description = "Burgers, hot dogs and more",
                    imageUrl = "food_truck",
                    eventId = "2",
                    location = "Outdoor Area"
                )
            ),
            scheduleItems = mutableListOf(
                ScheduleItem(
                    name = "Opening Session",
                    time = "09:30",
                    date = Calendar.getInstance().apply { 
                        set(2023, 4, 4, 9, 30) 
                    }.time
                ),
                ScheduleItem(
                    name = "Industry Panel",
                    time = "11:00",
                    date = Calendar.getInstance().apply { 
                        set(2023, 4, 4, 11, 0) 
                    }.time
                ),
                ScheduleItem(
                    name = "Lunch Break",
                    time = "13:00",
                    date = Calendar.getInstance().apply { 
                        set(2023, 4, 4, 13, 0) 
                    }.time
                ),
                ScheduleItem(
                    name = "Hackathon Kickoff",
                    time = "15:00",
                    date = Calendar.getInstance().apply { 
                        set(2023, 4, 4, 15, 0) 
                    }.time
                ),
                ScheduleItem(
                    name = "Awards Ceremony",
                    time = "16:00",
                    date = Calendar.getInstance().apply { 
                        set(2023, 4, 5, 16, 0) 
                    }.time
                )
            )
        )
    )
    
    // Currently selected event for participants
    private var _selectedEvent = mutableStateOf<Event?>(if (events.isNotEmpty()) events[0] else null)
    val selectedEvent: Event?
        get() = _selectedEvent.value
    
    fun setSelectedEvent(event: Event?) {
        _selectedEvent.value = event
    }
    
    fun addEvent(event: Event) {
        events.add(event)
    }
    
    fun getEvent(id: String): Event? {
        return events.find { it.id == id }
    }
    
    fun updateEvent(updatedEvent: Event) {
        val index = events.indexOfFirst { it.id == updatedEvent.id }
        if (index != -1) {
            events[index] = updatedEvent
            
            // Update selected event if needed
            if (_selectedEvent.value?.id == updatedEvent.id) {
                _selectedEvent.value = updatedEvent
            }
        }
    }
    
    fun deleteEvent(id: String) {
        events.removeIf { it.id == id }
        
        // Clear selected event if it was deleted
        if (_selectedEvent.value?.id == id) {
            _selectedEvent.value = if (events.isNotEmpty()) events[0] else null
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageEventsScreen(navController: NavController) {
    // Format dates for display
    val dateFormatter = SimpleDateFormat("dd MMM", Locale.getDefault())
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Manage Events",
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                actions = {
                    // Add event button
                    IconButton(
                        onClick = { 
                            navController.navigate("create_event")
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Create Event"
                        )
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(EventsData.events) { event ->
                EventCard(
                    event = event,
                    dateFormatter = dateFormatter,
                    onEventClick = {
                        // Navigate to event details screen
                        navController.navigate("event_details/${event.id}")
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventCard(
    event: Event,
    dateFormatter: SimpleDateFormat,
    onEventClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth(),
        onClick = onEventClick,
        colors = CardDefaults.cardColors(
            containerColor = Purple
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
        ) {
            // Event Icon Container
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(Color.LightGray.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = event.icon,
                    contentDescription = event.title,
                    tint = Color.White,
                    modifier = Modifier.size(64.dp)
                )
            }
            
            // Event Details
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Title
                Text(
                    text = event.title,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                
                // Description
                Text(
                    text = event.description,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 14.sp,
                    maxLines = 3
                )
                
                // Date
                Text(
                    text = "${dateFormatter.format(event.startDate)}-${dateFormatter.format(event.endDate)}",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 14.sp
                )
            }
        }
    }
} 