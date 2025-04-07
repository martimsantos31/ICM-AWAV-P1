package pt.ua.deti.icm.awav.ui.screens

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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import pt.ua.deti.icm.awav.data.room.entity.Event
import pt.ua.deti.icm.awav.data.room.entity.Ticket
import pt.ua.deti.icm.awav.ui.theme.Purple
import pt.ua.deti.icm.awav.ui.viewmodels.TicketViewModel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuyTicketScreen(
    navController: NavController,
    ticketViewModel: TicketViewModel = viewModel(factory = TicketViewModel.Factory)
) {
    val activeEvents by ticketViewModel.activeEvents.collectAsState()
    val loading by ticketViewModel.loading.collectAsState()
    val hasActiveTickets by ticketViewModel.hasActiveTickets.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    
    // State for purchase success dialog
    var showPurchaseSuccessDialog by remember { mutableStateOf(false) }
    
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
                NoActiveEventsView()
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
                        EventTicketCard(
                            event = event,
                            onBuyTicket = {
                                selectedEvent = event
                                
                                // Create a new ticket
                                val newTicket = Ticket(
                                    id = Random().nextInt(100000), // Simple random ID generation
                                    eventId = event.id,
                                    price = 10.0 // Fixed price for simplicity
                                )
                                
                                // Purchase the ticket
                                coroutineScope.launch {
                                    val success = ticketViewModel.purchaseTicket(newTicket)
                                    if (success) {
                                        showPurchaseSuccessDialog = true
                                    }
                                }
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
                        Text("Your ticket for ${selectedEvent?.name ?: "event"} has been purchased successfully!")
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showPurchaseSuccessDialog = false
                                
                                if (hasActiveTickets) {
                                    // Navigate to my tickets screen
                                    navController.navigate("my_tickets") {
                                        popUpTo("buy_ticket") { inclusive = true }
                                    }
                                } else {
                                    // Something went wrong, just dismiss
                                    navController.navigate("home")
                                }
                            }
                        ) {
                            Text("View My Tickets")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun NoActiveEventsView() {
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
fun EventTicketCard(
    event: Event,
    onBuyTicket: () -> Unit
) {
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
            
            // Buy ticket button
            Button(
                onClick = onBuyTicket,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Purple
                )
            ) {
                Icon(Icons.Default.ShoppingCart, contentDescription = "Buy")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Buy Ticket - $10")
            }
        }
    }
} 