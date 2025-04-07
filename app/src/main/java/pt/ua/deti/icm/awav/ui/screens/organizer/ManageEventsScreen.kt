package pt.ua.deti.icm.awav.ui.screens.organizer

import android.util.Log
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.flow.Flow
import pt.ua.deti.icm.awav.AWAVApplication
import pt.ua.deti.icm.awav.ui.theme.Purple
import pt.ua.deti.icm.awav.data.room.entity.Event
import pt.ua.deti.icm.awav.data.repository.EventsRepository
import java.text.SimpleDateFormat
import java.util.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageEventsScreen(navController: NavController) {
    // Format dates for display
    val dateFormatter = SimpleDateFormat("dd MMM", Locale.getDefault())

    // Get the events repository from the application container
    val eventsRepository = AWAVApplication.appContainer.eventsRepository
    
    // Collect events data as a state
    val eventData by eventsRepository.getActiveEvents().collectAsState(initial = emptyList())
    
    Log.d("ManageEventsScreen", "Loaded ${eventData.size} events")
    eventData.forEach { event ->
        Log.d("ManageEventsScreen", "Event: ID=${event.id}, name=${event.name}, active=${event.isActive}")
    }

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
                            Log.d("ManageEventsScreen", "Navigating to create_event screen")
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
        if (eventData.isEmpty()) {
            // Show empty state
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Event,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = Purple
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "No events yet",
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Create your first event by tapping the + button",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Button(
                        onClick = {
                            Log.d("ManageEventsScreen", "Navigating to create_event screen from empty state")
                            navController.navigate("create_event")
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Purple
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text("Create Event")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(eventData) { event ->
                    EventCard(
                        event = event,
                        dateFormatter = dateFormatter,
                        onEventClick = {
                            // Navigate to event details screen with the event ID as string
                            val eventIdString = event.id.toString()
                            Log.d("ManageEventsScreen", "Navigating to event_details with ID: $eventIdString (original ID: ${event.id})")
                            navController.navigate("event_details/$eventIdString")
                        }
                    )
                }
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
    Log.d("ManageEventsScreen", "Rendering EventCard for event: ${event.id} - ${event.name}")
    
    // Parse dates safely
    val startDate = try {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(event.startDate)
    } catch (e: Exception) {
        Log.e("ManageEventsScreen", "Error parsing start date: ${event.startDate}", e)
        Date()
    }
    
    val endDate = try {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(event.endDate)
    } catch (e: Exception) {
        Log.e("ManageEventsScreen", "Error parsing end date: ${event.endDate}", e)
        Date()
    }
    
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
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(Color.LightGray.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Event,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
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
                    text = event.name,
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
                    text = "${dateFormatter.format(startDate)}-${dateFormatter.format(endDate)}",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 14.sp
                )
            }
        }
    }
} 