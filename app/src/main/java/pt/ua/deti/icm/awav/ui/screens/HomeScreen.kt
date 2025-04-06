package pt.ua.deti.icm.awav.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import pt.ua.deti.icm.awav.R
import pt.ua.deti.icm.awav.ui.navigation.AwavNavigation
import pt.ua.deti.icm.awav.ui.screens.organizer.EventsData
import pt.ua.deti.icm.awav.ui.theme.AWAVStyles
import pt.ua.deti.icm.awav.ui.theme.Purple
import java.text.SimpleDateFormat
import java.util.*

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
fun HomeScreen() {
    // Selected event state from EventsData
    var selectedEvent by remember { mutableStateOf(EventsData.selectedEvent) }
    var expanded by remember { mutableStateOf(false) }
    val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    
    // Update when EventsData.selectedEvent changes
    LaunchedEffect(EventsData.selectedEvent) {
        selectedEvent = EventsData.selectedEvent
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
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Event Selector
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Selected Event",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Event Dropdown
                        OutlinedButton(
                            onClick = { expanded = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Purple
                            )
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = selectedEvent?.title ?: "No events available",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Select Event"
                                )
                            }
                        }
                        
                        if (selectedEvent != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.CalendarMonth,
                                    contentDescription = "Date",
                                    tint = Purple,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${dateFormatter.format(selectedEvent!!.startDate)} - ${dateFormatter.format(selectedEvent!!.endDate)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = selectedEvent!!.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2
                            )
                        }
                    }
                }
                
                // Dropdown menu
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    EventsData.events.forEach { event ->
                        DropdownMenuItem(
                            text = { 
                                Column {
                                    Text(text = event.title)
                                    Text(
                                        text = "${dateFormatter.format(event.startDate)} - ${dateFormatter.format(event.endDate)}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            },
                            onClick = {
                                selectedEvent = event
                                EventsData.setSelectedEvent(event) // Update the shared selected event
                                expanded = false
                            }
                        )
                    }
                }
            }

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
    
    // Charge Wallet Dialog
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