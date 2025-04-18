package pt.ua.deti.icm.awav.ui.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch
import pt.ua.deti.icm.awav.data.model.FirebaseTicket
import pt.ua.deti.icm.awav.data.room.entity.Event
import pt.ua.deti.icm.awav.ui.navigation.Screen
import pt.ua.deti.icm.awav.ui.theme.Purple
import pt.ua.deti.icm.awav.ui.viewmodels.FirebaseTicketViewModel
import pt.ua.deti.icm.awav.ui.viewmodels.TicketViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FirebaseMyTicketsScreen(
    navController: NavController,
    viewModel: FirebaseTicketViewModel = viewModel(factory = FirebaseTicketViewModel.factory()),
    // Also get the legacy TicketViewModel for syncing
    ticketViewModel: TicketViewModel = viewModel(factory = TicketViewModel.Factory)
) {
    val userTickets by viewModel.userTickets.collectAsState()
    val activeEvents by viewModel.activeEvents.collectAsState()
    val loading by viewModel.loading.collectAsState()
    
    // Force refresh of tickets on screen load
    LaunchedEffect(key1 = Unit) {
        Log.d("FirebaseMyTicketsScreen", "Refreshing tickets on screen load")
        
        // First refresh Firebase tickets
        viewModel.refreshUserTickets()
        
        // Then aggressively refresh the legacy ticket status with a forced Firebase check
        ticketViewModel.refreshTicketStatus(forceFirebaseCheck = true)
        
        // Double-check after a slight delay to ensure everything is in sync
        kotlinx.coroutines.delay(500)
        viewModel.refreshUserTickets()
        ticketViewModel.refreshTicketStatus(forceFirebaseCheck = true)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Tickets") },
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
            } else if (userTickets.isEmpty()) {
                FirebaseEmptyTicketsView(navController)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(userTickets) { ticket ->
                        // Find the corresponding event for this ticket
                        val event = activeEvents.find { event -> 
                            event.id == ticket.eventId
                        }
                        
                        if (event != null) {
                            FirebaseTicketCard(ticket, event)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FirebaseEmptyTicketsView(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.ConfirmationNumber,
            contentDescription = "No tickets",
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "You don't have any tickets yet",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Get tickets for active events to participate",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            modifier = Modifier.padding(horizontal = 32.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = { navController.navigate(Screen.BuyTicket.route) },
            colors = ButtonDefaults.buttonColors(
                containerColor = Purple
            )
        ) {
            Icon(Icons.Default.Add, contentDescription = "Find events")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Browse Events")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FirebaseTicketCard(ticket: FirebaseTicket, event: Event) {
    // Format purchase date
    val formattedDate = ticket.purchaseDate?.let { 
        SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(it.toDate())
    } ?: "Unknown"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Purple
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Event name
            Text(
                text = event.name,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color.White
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Event description
            Text(
                text = event.description,
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.8f),
                maxLines = 2
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Location
                Column {
                    Text(
                        text = "Location",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = "Location",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = event.location,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }
                }
                
                // Date
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Purchase Date",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.CalendarToday,
                            contentDescription = "Date",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = formattedDate,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Active status
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        if (ticket.bought) Color(0xFF4CAF50).copy(alpha = 0.2f) 
                        else Color.Red.copy(alpha = 0.2f),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    if (ticket.bought) Icons.Default.CheckCircle else Icons.Default.Cancel,
                    contentDescription = "Status",
                    tint = if (ticket.bought) Color(0xFF4CAF50) else Color.Red,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (ticket.bought) "Active Ticket" else "Inactive Ticket",
                    fontSize = 14.sp,
                    color = if (ticket.bought) Color(0xFF4CAF50) else Color.Red,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
} 