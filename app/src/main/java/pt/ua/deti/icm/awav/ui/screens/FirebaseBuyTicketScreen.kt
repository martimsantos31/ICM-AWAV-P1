package pt.ua.deti.icm.awav.ui.screens

import android.os.Handler
import android.os.Looper
import android.util.Log
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import pt.ua.deti.icm.awav.data.room.entity.Event
import pt.ua.deti.icm.awav.ui.navigation.Screen
import pt.ua.deti.icm.awav.ui.viewmodels.FirebaseTicketViewModel
import pt.ua.deti.icm.awav.ui.viewmodels.TicketViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FirebaseBuyTicketScreen(
    navController: NavController,
    viewModel: FirebaseTicketViewModel = viewModel(factory = FirebaseTicketViewModel.factory()),
    // Also get the legacy TicketViewModel just for updating the navbar
    ticketViewModel: TicketViewModel = viewModel(factory = TicketViewModel.Factory)
) {
    val activeEvents by viewModel.activeEvents.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val hasActiveTickets by viewModel.hasActiveTickets.collectAsState()
    val remainingTickets by viewModel.remainingTickets.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    
    // State for purchase success dialog
    var showPurchaseSuccessDialog by remember { mutableStateOf(false) }
    var showNoTicketsDialog by remember { mutableStateOf(false) }
    
    // State to track which event is being purchased
    var selectedEvent by remember { mutableStateOf<Event?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Buy Tickets") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (activeEvents.isEmpty()) {
                // No active events
                FirebaseNoActiveEventsView()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Text(
                            text = "Available Events",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    
                    items(activeEvents) { event ->
                        FirebaseEventTicketCard(
                            event = event,
                            remainingTickets = remainingTickets[event.id] ?: 0,
                            onBuyTicket = {
                                selectedEvent = event
                                
                                // Check if tickets are available
                                val ticketsLeft = remainingTickets[event.id] ?: 0
                                if (ticketsLeft <= 0) {
                                    showNoTicketsDialog = true
                                    return@FirebaseEventTicketCard
                                }
                                
                                // Purchase the ticket using Firebase
                                coroutineScope.launch {
                                    viewModel.purchaseTicket(event.id) { success ->
                                        if (success) {
                                            // Show success dialog
                                            showPurchaseSuccessDialog = true
                                        }
                                    }
                                }
                            },
                            onCheckAvailability = {
                                // Check remaining tickets for this event
                                viewModel.getRemainingTickets(event.id)
                            }
                        )
                    }
                }
            }
            
            // Purchase success dialog
            if (showPurchaseSuccessDialog) {
                AlertDialog(
                    onDismissRequest = { showPurchaseSuccessDialog = false },
                    title = { Text("Purchase Successful") },
                    text = { 
                        Text("Your ticket for ${selectedEvent?.name ?: "event"} has been purchased successfully! You now have access to all app features.")
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showPurchaseSuccessDialog = false
                                
                                // Force the navbar to update by updating the legacy TicketViewModel as well
                                ticketViewModel.refreshTicketStatus(forceFirebaseCheck = true)
                                
                                // Delay navigation slightly to ensure state updates
                                Handler(Looper.getMainLooper()).postDelayed({
                                    // Force complete navigation reset to rebuild the entire UI
                                    navController.navigate(Screen.Home.route) {
                                        // Clear the entire back stack
                                        popUpTo(0) { inclusive = true }
                                        launchSingleTop = true
                                    }
                                    
                                    // Log that navigation is complete
                                    Log.d("FirebaseBuyTicketScreen", "Navigation reset after purchase")
                                }, 800) // Slightly longer delay to ensure Firebase updates
                            }
                        ) {
                            Text("Go to Home")
                        }
                    }
                )
            }
            
            // No tickets available dialog
            if (showNoTicketsDialog) {
                AlertDialog(
                    onDismissRequest = { showNoTicketsDialog = false },
                    title = { Text("Sold Out") },
                    text = { 
                        Text("Sorry, all tickets for ${selectedEvent?.name ?: "this event"} have been sold.")
                    },
                    confirmButton = {
                        TextButton(onClick = { showNoTicketsDialog = false }) {
                            Text("OK")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun FirebaseNoActiveEventsView() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.EventBusy,
            contentDescription = "No events",
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "No Active Events",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "There are no active events available at the moment. Check back later!",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FirebaseEventTicketCard(
    event: Event,
    remainingTickets: Int,
    onBuyTicket: () -> Unit,
    onCheckAvailability: () -> Unit
) {
    // Check ticket availability when card is shown
    LaunchedEffect(event.id) {
        onCheckAvailability()
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Event name
            Text(
                text = event.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Event description
            Text(
                text = event.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                maxLines = 2
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Event details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Location
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = "Location",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = event.location,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                // Date
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CalendarToday,
                        contentDescription = "Date",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = event.startDate,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Capacity/Tickets information
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Capacity
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.People,
                        contentDescription = "Capacity",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Capacity: ${event.capacity}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                // Remaining tickets
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.ConfirmationNumber,
                        contentDescription = "Tickets",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Available: $remainingTickets",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Buy button
            val isSoldOut = remainingTickets <= 0
            
            Button(
                onClick = onBuyTicket,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSoldOut
            ) {
                Text(
                    text = if (isSoldOut) "Sold Out" else "Buy Ticket",
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
            
            if (isSoldOut) {
                Text(
                    text = "Sorry, all tickets for this event have been sold.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )
            }
        }
    }
} 