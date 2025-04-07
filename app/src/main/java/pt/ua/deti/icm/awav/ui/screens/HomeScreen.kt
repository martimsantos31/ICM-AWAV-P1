package pt.ua.deti.icm.awav.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import pt.ua.deti.icm.awav.R
import pt.ua.deti.icm.awav.data.room.AppDatabase
import pt.ua.deti.icm.awav.ui.navigation.AwavNavigation
import pt.ua.deti.icm.awav.ui.theme.AWAVStyles
import pt.ua.deti.icm.awav.ui.theme.Purple
import pt.ua.deti.icm.awav.ui.viewmodels.TicketViewModel
import pt.ua.deti.icm.awav.data.room.entity.Event
import java.text.SimpleDateFormat
import java.util.*
import androidx.navigation.NavController
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Singleton for wallet data
object WalletData {
    private var _balance = mutableStateOf(7.20f)
    val balance: Float
        get() = _balance.value
    
    fun addFunds(amount: Float) {
        _balance.value += amount
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    hasActiveTickets: Boolean = false,  // This parameter is ignored - we use ViewModel directly
    navController: NavController? = null,
    ticketViewModel: TicketViewModel = viewModel(factory = TicketViewModel.Factory)
) {
    // IMPORTANT: Get the current ticket status directly from ViewModel for reliability
    val currentHasActiveTickets by ticketViewModel.hasActiveTickets.collectAsState()
    
    // Force a refresh of ticket status when HomeScreen appears
    LaunchedEffect(Unit) {
        Log.d("HomeScreen", "Refreshing ticket status on HomeScreen appearance")
        // Use forceFirebaseCheck to make sure we get the latest data from Firebase
        ticketViewModel.refreshTicketStatus(forceFirebaseCheck = true)
    }
    
    val context = LocalContext.current
    // Get the database instance using the singleton pattern instead of creating a new instance
    val db = AppDatabase.getDatabase(context)
    
    // Collect events data as a state with explicit type declaration
    val eventData by db.eventDao().getActiveEvents().collectAsState(initial = emptyList<Event>())
    var selectedEvent by remember { mutableStateOf<Event?>(eventData.firstOrNull()) }
    var expanded by remember { mutableStateOf(false) }
    val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val storageFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    
    // Function to safely format stored dates
    fun formatStoredDate(dateStr: String?): String {
        return try {
            if (dateStr == null) return "Unknown"
            val date = storageFormatter.parse(dateStr)
            date?.let { dateFormatter.format(it) } ?: "Unknown"
        } catch (e: Exception) {
            Log.e("HomeScreen", "Error parsing date: $dateStr", e)
            "Unknown"
        }
    }
    
    // Wallet charge states
    var showChargeDialog by remember { mutableStateOf(false) }
    var chargeAmount by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Home",
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
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (!currentHasActiveTickets) {
                // Show prominent ticket purchase view
                NoTicketsView(navController)
            } else {
                // Regular home content for users with tickets
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Wallet Section
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "My Wallet",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                
                                Button(
                                    onClick = { showChargeDialog = true },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Purple
                                    ),
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Add Funds",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Add Funds")
                                }
                            }

                            Text(
                                text = "${String.format("%.2f", WalletData.balance)} €",
                                style = MaterialTheme.typography.displayMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                    
                    // Event Notifications
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Text(
                            text = "Event Notifications",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            elevation = CardDefaults.cardElevation(
                                defaultElevation = AWAVStyles.cardElevation
                            ),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "Basic dialog title",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "A dialog is a type of modal window that appears in front of app content to provide critical information, or prompt for a decision to be made.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 8.dp)
                                )

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 16.dp),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    Text(
                                        text = "Action 1",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Charge dialog
    if (showChargeDialog) {
        AlertDialog(
            onDismissRequest = { 
                showChargeDialog = false
                chargeAmount = ""
            },
            title = { Text("Add Funds to Wallet") },
            text = {
                Column {
                    Text(
                        text = "Enter the amount you want to add to your wallet:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = chargeAmount,
                        onValueChange = {
                            // Only allow numbers and a single decimal point
                            if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) {
                                chargeAmount = it
                            }
                        },
                        label = { Text("Amount (€)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        prefix = { Text("€") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        try {
                            val amount = chargeAmount.toFloatOrNull() ?: 0f
                            if (amount > 0) {
                                WalletData.addFunds(amount)
                            }
                            showChargeDialog = false
                            chargeAmount = ""
                        } catch (e: Exception) {
                            // Handle error
                        }
                    },
                    enabled = chargeAmount.isNotBlank() && chargeAmount.toFloatOrNull() != null && chargeAmount.toFloatOrNull()!! > 0
                ) {
                    Text("Add Funds")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showChargeDialog = false
                        chargeAmount = ""
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun NoTicketsView(navController: NavController?) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.EventAvailable,
            contentDescription = "Events available",
            modifier = Modifier.size(100.dp),
            tint = Purple
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Get Your Ticket Now!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Purchase a ticket to unlock full app features and participate in the event.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = { navController?.navigate("buy_ticket") },
            colors = ButtonDefaults.buttonColors(
                containerColor = Purple
            ),
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(60.dp)
        ) {
            Icon(
                Icons.Default.ShoppingCart, 
                contentDescription = "Buy Tickets",
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Browse Available Events",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}