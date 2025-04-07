package pt.ua.deti.icm.awav.ui.screens.organizer

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import pt.ua.deti.icm.awav.awavApplication
import pt.ua.deti.icm.awav.data.repository.EventsRepository
import pt.ua.deti.icm.awav.data.repository.StandsRepository
import pt.ua.deti.icm.awav.data.room.entity.Event
import pt.ua.deti.icm.awav.data.room.entity.Stand
import pt.ua.deti.icm.awav.ui.theme.Purple
import pt.ua.deti.icm.awav.ui.viewmodels.EventDetailsViewModel
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailsScreen(
    eventId: String,
    navController: NavController,
    viewModel: EventDetailsViewModel = viewModel()
) {
    // Log the received eventId
    Log.d("EventDetailsScreen", "Received eventId parameter: '$eventId'")
    
    // State to track parsing errors
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Load event data when the screen is first created
    LaunchedEffect(eventId) {
        try {
            // Try to convert the eventId to Int
            Log.d("EventDetailsScreen", "Attempting to convert eventId '$eventId' to Int")
            val eventIdInt = eventId.toInt()
            Log.d("EventDetailsScreen", "Successfully converted eventId to Int: $eventIdInt")
            
            // Load the event using the ViewModel
            Log.d("EventDetailsScreen", "Loading event with ID: $eventIdInt")
            viewModel.loadEvent(eventIdInt)
        } catch (e: NumberFormatException) {
            // Handle conversion error
            errorMessage = "Invalid event ID format: $eventId"
            Log.e("EventDetailsScreen", "Error parsing event ID: $eventId", e)
        } catch (e: Exception) {
            // Handle any other errors
            errorMessage = "Error loading event: ${e.message}"
            Log.e("EventDetailsScreen", "Error in EventDetailsScreen: ${e.message}", e)
        }
    }
    
    // Create coroutine scope for database operations
    val coroutineScope = rememberCoroutineScope()
    
    // Collect state
    val eventState by viewModel.event.collectAsState()
    val standsState by viewModel.stands.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    // Log state changes
    LaunchedEffect(eventState) {
        Log.d("EventDetailsScreen", "Event state updated: ${eventState?.id} - ${eventState?.name}")
    }
    
    LaunchedEffect(standsState) {
        Log.d("EventDetailsScreen", "Stands state updated: ${standsState.size} stands loaded")
    }
    
    LaunchedEffect(isLoading) {
        Log.d("EventDetailsScreen", "Loading state updated: $isLoading")
    }
    
    val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Event Details", "Manage Stands", "Schedule")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = eventState?.name ?: "Loading...",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Purple)
            }
        } else if (eventState == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Event not found")
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Tab row
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = Purple
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title) }
                        )
                    }
                }
                
                // Tab content
                when (selectedTab) {
                    0 -> EventDetailsTab(
                        event = eventState!!,
                        dateFormatter = dateFormatter,
                        navController = navController,
                        onEventUpdated = { updatedEvent ->
                            coroutineScope.launch {
                                viewModel.updateEvent(updatedEvent)
                            }
                        },
                        onEventDeleted = {
                            coroutineScope.launch {
                                viewModel.deleteEvent(eventState!!)
                                navController.popBackStack()
                            }
                        }
                    )
                    1 -> ManageStandsTab(
                        stands = standsState,
                        eventId = eventState!!.id,
                        onStandAdded = { name, description ->
                            coroutineScope.launch {
                                viewModel.addStand(name, description, eventState!!.id)
                            }
                        },
                        onStandDeleted = { stand ->
                            coroutineScope.launch {
                                viewModel.deleteStand(stand)
                            }
                        },
                        onStandUpdated = { stand ->
                            coroutineScope.launch {
                                viewModel.updateStand(stand)
                            }
                        }
                    )
                    2 -> Text("Schedule tab - To be implemented")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailsTab(
    event: Event,
    dateFormatter: SimpleDateFormat,
    navController: NavController,
    onEventUpdated: (Event) -> Unit,
    onEventDeleted: () -> Unit
) {
    var editMode by remember { mutableStateOf(false) }
    
    // State for edited fields
    var name by remember { mutableStateOf(event.name) }
    var description by remember { mutableStateOf(event.description) }
    var location by remember { mutableStateOf(event.location) }
    
    // Convert String dates to Date objects
    val startDateStr = event.startDate
    val endDateStr = event.endDate
    var startDate by remember { 
        mutableStateOf(
            try {
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(startDateStr) ?: Date()
            } catch (e: Exception) {
                Date()
            }
        ) 
    }
    var endDate by remember { 
        mutableStateOf(
            try {
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(endDateStr) ?: Date()
            } catch (e: Exception) {
                Date()
            }
        ) 
    }
    
    // Date picker dialog states
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    
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
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Image/Icon
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Purple.copy(alpha = 0.7f))
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Event,
                    contentDescription = event.name,
                    tint = Color.White,
                    modifier = Modifier.size(80.dp)
                )
            }
            
            // Edit button
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                contentAlignment = Alignment.TopEnd
            ) {
                FloatingActionButton(
                    onClick = { 
                        if (editMode) {
                            // Save changes
                            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            val updatedEvent = event.copy(
                                name = name,
                                description = description,
                                location = location,
                                startDate = dateFormat.format(startDate),
                                endDate = dateFormat.format(endDate)
                            )
                            onEventUpdated(updatedEvent)
                        }
                        editMode = !editMode 
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(
                        imageVector = if (editMode) Icons.Filled.Save else Icons.Filled.Edit,
                        contentDescription = if (editMode) "Save" else "Edit"
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Event details
        if (editMode) {
            // Editable fields
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Event Title") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Purple,
                    focusedLabelColor = Purple
                )
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Purple,
                    focusedLabelColor = Purple
                )
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Location") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Purple,
                    focusedLabelColor = Purple
                )
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { showStartDatePicker = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Start Date"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Start: ${dateFormatter.format(startDate)}")
                }
                
                OutlinedButton(
                    onClick = { showEndDatePicker = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "End Date"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("End: ${dateFormatter.format(endDate)}")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Delete button
            Button(
                onClick = onEventDeleted,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Delete Event",
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("Delete Event")
            }
        } else {
            // Display-only view
            Text(
                text = event.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = event.description,
                style = MaterialTheme.typography.bodyLarge
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.LocationOn,
                    contentDescription = "Location",
                    tint = Purple
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = event.location,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.CalendarMonth,
                    contentDescription = "Date Range",
                    tint = Purple
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = "${dateFormatter.format(startDate)} - ${dateFormatter.format(endDate)}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun ManageStandsTab(
    stands: List<Stand>,
    eventId: Int,
    onStandAdded: (name: String, description: String) -> Unit,
    onStandDeleted: (Stand) -> Unit,
    onStandUpdated: (Stand) -> Unit
) {
    var showAddStandDialog by remember { mutableStateOf(false) }
    var newStandName by remember { mutableStateOf("") }
    var newStandDescription by remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Add Stand button
        Button(
            onClick = { showAddStandDialog = true },
            colors = ButtonDefaults.buttonColors(
                containerColor = Purple
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "Add Stand",
                modifier = Modifier.padding(end = 8.dp)
            )
            Text("Add New Stand")
        }
        
        // Stands list
        if (stands.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No stands added yet",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray
                )
            }
        } else {
            stands.forEach { stand ->
                StandItem(
                    stand = stand,
                    onEdit = { updatedStand -> 
                        onStandUpdated(updatedStand)
                    },
                    onDelete = { 
                        onStandDeleted(stand)
                    }
                )
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
                            // Add new stand
                            onStandAdded(newStandName, newStandDescription)
                            
                            showAddStandDialog = false
                            newStandName = ""
                            newStandDescription = ""
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

@Composable
fun StandItem(
    stand: Stand,
    onEdit: (Stand) -> Unit,
    onDelete: () -> Unit
) {
    var showEditDialog by remember { mutableStateOf(false) }
    var editedName by remember { mutableStateOf(stand.name) }
    var editedDescription by remember { mutableStateOf(stand.description) }
    
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
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Store,
                contentDescription = "Stand",
                tint = Purple,
                modifier = Modifier.padding(end = 16.dp)
            )
            
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
            
            IconButton(onClick = { showEditDialog = true }) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = "Edit Stand"
                )
            }
            
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Delete Stand",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
    
    // Edit Stand Dialog
    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit Stand") },
            text = {
                Column {
                    OutlinedTextField(
                        value = editedName,
                        onValueChange = { editedName = it },
                        label = { Text("Stand Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = editedDescription,
                        onValueChange = { editedDescription = it },
                        label = { Text("Stand Description (Optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editedName.isNotBlank()) {
                            val updatedStand = stand.copy(
                                name = editedName,
                                description = editedDescription
                            )
                            
                            onEdit(updatedStand)
                            showEditDialog = false
                        }
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
} 