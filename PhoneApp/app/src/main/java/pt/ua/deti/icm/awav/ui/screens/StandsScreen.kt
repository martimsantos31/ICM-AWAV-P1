package pt.ua.deti.icm.awav.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import pt.ua.deti.icm.awav.data.room.AppDatabase
import pt.ua.deti.icm.awav.data.room.entity.Stand
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StandsScreen(navController: NavController) {
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    val ticketViewModel: TicketViewModel = viewModel(factory = TicketViewModel.Factory)
    val firebaseTicketViewModel: FirebaseTicketViewModel = viewModel(factory = FirebaseTicketViewModel.factory())
    val scope = rememberCoroutineScope()

    // Get the user's tickets from both sources
    val userRoomTickets by ticketViewModel.userTickets.collectAsState(initial = emptyList())
    val userFirebaseTickets by firebaseTicketViewModel.userTickets.collectAsState()
    
    // State for stands and loading
    var stands by remember { mutableStateOf<List<Stand>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Find the eventId from the user's ticket
    LaunchedEffect(userRoomTickets, userFirebaseTickets) {
        isLoading = true
        Log.d("StandsScreen", "Room tickets: ${userRoomTickets.size}, Firebase tickets: ${userFirebaseTickets.size}")
        
        try {
            // Check Firebase tickets first
            if (userFirebaseTickets.isNotEmpty()) {
                val eventId = userFirebaseTickets.first().eventId
                Log.d("StandsScreen", "Using Firebase ticket with eventId: $eventId")
                stands = withContext(Dispatchers.IO) {
                    db.standDao().getStandsForEvent(eventId)
                }
            }
            // Then check Room tickets if no Firebase tickets
            else if (userRoomTickets.isNotEmpty()) {
                val ticket = userRoomTickets.first()
                // Get the event ID from the ticket
                withContext(Dispatchers.IO) {
                    val event = db.eventDao().getEventForTicket(ticket.ticketId)
                    if (event != null) {
                        Log.d("StandsScreen", "Using Room ticket with eventId: ${event.id}")
                        stands = db.standDao().getStandsForEvent(event.id)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("StandsScreen", "Error loading stands: ${e.message}", e)
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Stands",
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
                        imageVector = Icons.Default.Store,
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
                        text = "Please purchase a ticket to view stands",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else if (stands.isEmpty()) {
            // Event has no stands
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
                        imageVector = Icons.Default.Store,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(64.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "No stands available",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "This event doesn't have any stands yet",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            // Display stands list
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(stands) { stand ->
                    StandCard(
                        stand = stand,
                        onDetailsClick = {
                            // Navigate to stand details
                            navController.navigate("stand_details/${stand.id}")
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StandCard(
    stand: Stand,
    onDetailsClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        onClick = onDetailsClick,
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
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = stand.name,
                    style = MaterialTheme.typography.titleMedium
                )
                
                if (stand.description.isNotBlank()) {
                    Text(
                        text = stand.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "View Details",
                tint = Purple
            )
        }
    }
}
