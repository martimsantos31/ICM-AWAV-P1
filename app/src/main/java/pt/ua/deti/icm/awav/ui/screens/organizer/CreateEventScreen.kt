package pt.ua.deti.icm.awav.ui.screens.organizer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import pt.ua.deti.icm.awav.ui.theme.AWAVStyles
import pt.ua.deti.icm.awav.ui.theme.Purple
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventScreen(navController: NavController) {
    var eventName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf(Calendar.getInstance().time) }
    var endDate by remember { mutableStateOf(Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, 1) }.time) }
    
    // Dialog visibility states
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    
    // States for temporary stands
    var stands by remember { mutableStateOf(listOf<Stand>()) }
    var showAddStandDialog by remember { mutableStateOf(false) }
    var newStandName by remember { mutableStateOf("") }
    var newStandDescription by remember { mutableStateOf("") }
    
    // Date formatter for display
    val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    
    // Date Picker Dialogs
    if (showStartDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = startDate.time
        )
        
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            startDate = Date(millis)
                        }
                        showStartDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showStartDatePicker = false }
                ) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
    
    if (showEndDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = endDate.time
        )
        
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            endDate = Date(millis)
                        }
                        showEndDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showEndDatePicker = false }
                ) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Create New Event",
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 80.dp) // Add padding to avoid overlap with navbar
            ) {
                // Event Name
                OutlinedTextField(
                    value = eventName,
                    onValueChange = { eventName = it },
                    label = { Text("Event Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Location
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Date pickers
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Start date
                    OutlinedButton(
                        onClick = { showStartDatePicker = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Start Date"
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Start: ${dateFormatter.format(startDate)}",
                            fontSize = 14.sp
                        )
                    }
                    
                    // End date
                    OutlinedButton(
                        onClick = { showEndDatePicker = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "End Date"
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "End: ${dateFormatter.format(endDate)}",
                            fontSize = 14.sp
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider()
                
                // Stands section
                Text(
                    text = "Stands",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
                
                // Display added stands
                stands.forEach { stand ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Store,
                                contentDescription = null,
                                tint = Purple
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = stand.name, style = MaterialTheme.typography.titleSmall)
                                if (stand.description.isNotBlank()) {
                                    Text(
                                        text = stand.description,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                            IconButton(onClick = { 
                                stands = stands.filter { it.id != stand.id }
                            }) {
                                Icon(
                                    imageVector = Icons.Filled.Delete,
                                    contentDescription = "Remove stand",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
                
                // Add Stand button
                OutlinedButton(
                    onClick = { showAddStandDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Add Stand"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Stand")
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider()
                
                // Ticket Types section (placeholder)
                Text(
                    text = "Ticket Types",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
                
                OutlinedButton(
                    onClick = { /* TODO: Add ticket type dialog */ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Add Ticket Type"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Ticket Type")
                }
                
                // Additional padding at the bottom to ensure content is not hidden
                Spacer(modifier = Modifier.height(64.dp))
            }
            
            // Create Event button - fixed at the bottom
            Button(
                onClick = { 
                    if (eventName.isNotBlank() && description.isNotBlank() && location.isNotBlank()) {
                        // Create new event
                        val newEvent = Event(
                            id = UUID.randomUUID().toString(),
                            title = eventName,
                            description = description,
                            location = location,
                            startDate = startDate,
                            endDate = endDate,
                            stands = stands.toMutableList()
                        )
                        
                        // Add to events data
                        EventsData.addEvent(newEvent)
                        // Set as selected event
                        EventsData.setSelectedEvent(newEvent)
                        
                        // Navigate back
                        navController.popBackStack()
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Purple
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                Text("Create Event", modifier = Modifier.padding(vertical = 4.dp))
            }
        }
    }
    
    // Add Stand Dialog
    if (showAddStandDialog) {
        AlertDialog(
            onDismissRequest = { showAddStandDialog = false },
            title = { Text("Add New Stand") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newStandName,
                        onValueChange = { newStandName = it },
                        label = { Text("Stand Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = newStandDescription,
                        onValueChange = { newStandDescription = it },
                        label = { Text("Stand Description (Optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newStandName.isNotBlank()) {
                            // Create new stand
                            val newStand = Stand(
                                name = newStandName,
                                description = newStandDescription
                            )
                            
                            // Add to stands list
                            stands = stands + newStand
                            
                            // Clear fields and close dialog
                            newStandName = ""
                            newStandDescription = ""
                            showAddStandDialog = false
                        }
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddStandDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
} 