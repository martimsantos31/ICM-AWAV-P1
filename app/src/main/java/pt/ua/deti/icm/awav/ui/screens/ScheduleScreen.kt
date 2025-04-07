package pt.ua.deti.icm.awav.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import pt.ua.deti.icm.awav.data.room.AppDatabase
import pt.ua.deti.icm.awav.data.room.entity.ScheduleItem
import pt.ua.deti.icm.awav.data.room.entity.Event
import pt.ua.deti.icm.awav.ui.theme.Purple
import pt.ua.deti.icm.awav.ui.viewmodels.TicketViewModel
import pt.ua.deti.icm.awav.ui.viewmodels.FirebaseTicketViewModel
import pt.ua.deti.icm.awav.data.model.FirebaseTicket
import androidx.lifecycle.viewmodel.compose.viewModel
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(navController: NavController) {
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    val ticketViewModel: TicketViewModel = viewModel(factory = TicketViewModel.Factory)
    val firebaseTicketViewModel: FirebaseTicketViewModel = viewModel(factory = FirebaseTicketViewModel.factory())
    val scope = rememberCoroutineScope()

    // Get the user's tickets from both sources
    val userRoomTickets by ticketViewModel.userTickets.collectAsState(initial = emptyList())
    val userFirebaseTickets by firebaseTicketViewModel.userTickets.collectAsState()
    
    val dateFormatter = SimpleDateFormat("E, dd MMM", Locale.getDefault())
    val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())

    // State for schedule items
    var scheduleItems by remember { mutableStateOf<List<ScheduleItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Find the eventId from the user's ticket
    LaunchedEffect(userRoomTickets, userFirebaseTickets) {
        isLoading = true
        Log.d("ScheduleScreen", "Room tickets: ${userRoomTickets.size}, Firebase tickets: ${userFirebaseTickets.size}")
        
        try {
            // Check Firebase tickets first
            if (userFirebaseTickets.isNotEmpty()) {
                val eventId = userFirebaseTickets.first().eventId
                Log.d("ScheduleScreen", "Using Firebase ticket with eventId: $eventId")
                scheduleItems = withContext(Dispatchers.IO) {
                    db.scheduleItemDao().getScheduleItemsForEvent(eventId)
                }
            }
            // Then check Room tickets if no Firebase tickets
            else if (userRoomTickets.isNotEmpty()) {
                val ticket = userRoomTickets.first()
                // Get the event ID from the ticket
                withContext(Dispatchers.IO) {
                    val event = db.eventDao().getEventForTicket(ticket.ticketId)
                    if (event != null) {
                        Log.d("ScheduleScreen", "Using Room ticket with eventId: ${event.id}")
                        scheduleItems = db.scheduleItemDao().getScheduleItemsForEvent(event.id)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("ScheduleScreen", "Error loading schedule: ${e.message}", e)
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Schedule",
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        if (isLoading) {
            // Show loading indicator
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (userRoomTickets.isEmpty() && userFirebaseTickets.isEmpty()) {
            // No tickets
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Event,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(64.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "No ticket found",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Please purchase a ticket to view the event schedule",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else if (scheduleItems.isEmpty()) {
            // Event has no schedule items
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Event,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(64.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "No schedule available",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "This event doesn't have any schedule items yet",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            // Group schedule items by date
            val groupedSchedule = scheduleItems
                .sortedBy { it.startTime }
                .groupBy {
                    try {
                        val cal = Calendar.getInstance()
                        val dateStr = it.startTime.split(" ")[0] // Extract date part yyyy-MM-dd
                        cal.time = SimpleDateFormat("yyyy-MM-dd").parse(dateStr)!!
                        cal.set(Calendar.HOUR_OF_DAY, 0)
                        cal.set(Calendar.MINUTE, 0)
                        cal.set(Calendar.SECOND, 0)
                        cal.set(Calendar.MILLISECOND, 0)
                        cal.time
                    } catch (e: Exception) {
                        Log.e("ScheduleScreen", "Error parsing date: ${it.startTime}", e)
                        Date() // Default to current date if parsing fails
                    }
                }

            // Display schedule list
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                groupedSchedule.forEach { (date, items) ->
                    item {
                        // Date header
                        Text(
                            text = dateFormatter.format(date),
                            style = MaterialTheme.typography.titleMedium,
                            color = Purple,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    items(items) { item ->
                        ScheduleCard(item = item, timeFormatter = timeFormatter)
                    }
                }
            }
        }
    }
}

@Composable
fun ScheduleCard(
    item: ScheduleItem,
    timeFormatter: SimpleDateFormat
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Time column
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.width(64.dp)
            ) {
                Text(
                    text = item.startTime,
                    style = MaterialTheme.typography.titleSmall,
                    color = Purple
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Vertical line separator
            HorizontalDivider(
                modifier = Modifier
                    .height(40.dp)
                    .width(1.dp),
                color = Purple
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Event details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}