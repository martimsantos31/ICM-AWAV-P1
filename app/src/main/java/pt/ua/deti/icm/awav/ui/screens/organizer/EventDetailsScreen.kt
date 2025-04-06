package pt.ua.deti.icm.awav.ui.screens.organizer

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
import androidx.navigation.NavController
import coil.compose.AsyncImage
import pt.ua.deti.icm.awav.ui.theme.Purple
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailsScreen(
    eventId: String,
    navController: NavController
) {
    // Get event from the EventsData
    val event = EventsData.getEvent(eventId) ?: return
    
    val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Event Details", "Manage Stands", "Schedule")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = event.title,
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
                    event = event,
                    dateFormatter = dateFormatter,
                    navController = navController
                )
                1 -> ManageStandsTab(event = event)
                2 -> ScheduleTab(event = event)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailsTab(
    event: Event,
    dateFormatter: SimpleDateFormat,
    navController: NavController
) {
    var editMode by remember { mutableStateOf(false) }
    
    // State for edited fields
    var title by remember { mutableStateOf(event.title) }
    var description by remember { mutableStateOf(event.description) }
    var location by remember { mutableStateOf(event.location) }
    var startDate by remember { mutableStateOf(event.startDate) }
    var endDate by remember { mutableStateOf(event.endDate) }
    
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
                    imageVector = event.icon,
                    contentDescription = event.title,
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
                            val updatedEvent = event.copy(
                                title = title,
                                description = description,
                                location = location,
                                startDate = startDate,
                                endDate = endDate
                            )
                            EventsData.updateEvent(updatedEvent)
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
                value = title,
                onValueChange = { title = it },
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
                onClick = { 
                    EventsData.deleteEvent(event.id)
                    navController.popBackStack()
                },
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
                text = event.title,
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
                    text = "${dateFormatter.format(event.startDate)} - ${dateFormatter.format(event.endDate)}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun ManageStandsTab(event: Event) {
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
        if (event.stands.isEmpty()) {
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
            event.stands.forEach { stand ->
                StandItem(
                    stand = stand,
                    onEdit = { /* Edit dialog will be implemented next */ },
                    onDelete = { 
                        event.stands.remove(stand)
                        EventsData.updateEvent(event)
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
                            val newStand = Stand(
                                name = newStandName,
                                description = newStandDescription
                            )
                            event.stands.add(newStand)
                            // Update event with new stand
                            EventsData.updateEvent(event)
                            
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
    onEdit: () -> Unit,
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
                imageVector = stand.icon,
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
                            // Get the event that contains this stand
                            val events = EventsData.events
                            val event = events.find { it.stands.any { s -> s.id == stand.id } }
                            
                            if (event != null) {
                                // Find and update the stand in the event
                                val index = event.stands.indexOfFirst { it.id == stand.id }
                                if (index != -1) {
                                    event.stands[index] = stand.copy(
                                        name = editedName,
                                        description = editedDescription
                                    )
                                    
                                    // Update event
                                    EventsData.updateEvent(event)
                                }
                            }
                            
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleTab(event: Event) {
    var showAddScheduleDialog by remember { mutableStateOf(false) }
    var newEventName by remember { mutableStateOf("") }
    var newEventTime by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(event.startDate) }
    var showDatePicker by remember { mutableStateOf(false) }
    
    val dateFormatter = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault())
    val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    
    // Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate.time
        )
        
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            selectedDate = Date(millis)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDatePicker = false }
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
        // Add Schedule Item button
        Button(
            onClick = { showAddScheduleDialog = true },
            colors = ButtonDefaults.buttonColors(
                containerColor = Purple
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "Add Schedule Item",
                modifier = Modifier.padding(end = 8.dp)
            )
            Text("Add Schedule Item")
        }
        
        // Schedule list
        if (event.scheduleItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No schedule items added yet",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray
                )
            }
        } else {
            // Sort schedule items by date and time
            val sortedItems = event.scheduleItems.sortedBy { it.date }
            
            sortedItems.forEach { item ->
                ScheduleItem(
                    scheduleItem = item,
                    dateFormatter = dateFormatter,
                    onEdit = { /* Edit dialog will be implemented next */ },
                    onDelete = { 
                        event.scheduleItems.remove(item)
                        EventsData.updateEvent(event)
                    }
                )
            }
        }
    }
    
    // Add Schedule Item Dialog
    if (showAddScheduleDialog) {
        AlertDialog(
            onDismissRequest = { showAddScheduleDialog = false },
            title = { Text("Add Schedule Item") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newEventName,
                        onValueChange = { newEventName = it },
                        label = { Text("Event Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Date selection
                    OutlinedButton(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Select Date"
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Date: ${dateFormatter.format(selectedDate)}")
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = newEventTime,
                        onValueChange = { newEventTime = it },
                        label = { Text("Time (e.g., 10:00)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newEventName.isNotBlank() && newEventTime.isNotBlank()) {
                            try {
                                // Create calendar from selected date
                                val calendar = Calendar.getInstance()
                                calendar.time = selectedDate
                                
                                // Parse time
                                val timeParts = newEventTime.split(":")
                                if (timeParts.size == 2) {
                                    val hour = timeParts[0].toInt()
                                    val minute = timeParts[1].toInt()
                                    
                                    // Set time on calendar
                                    calendar.set(Calendar.HOUR_OF_DAY, hour)
                                    calendar.set(Calendar.MINUTE, minute)
                                    
                                    // Create new schedule item
                                    val newItem = ScheduleItem(
                                        name = newEventName,
                                        time = newEventTime,
                                        date = calendar.time
                                    )
                                    
                                    // Add to event
                                    event.scheduleItems.add(newItem)
                                    EventsData.updateEvent(event)
                                    
                                    showAddScheduleDialog = false
                                    newEventName = ""
                                    newEventTime = ""
                                }
                            } catch (e: Exception) {
                                // Handle invalid time format
                            }
                        }
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddScheduleDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ScheduleItem(
    scheduleItem: ScheduleItem,
    dateFormatter: SimpleDateFormat,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showEditDialog by remember { mutableStateOf(false) }
    var editedName by remember { mutableStateOf(scheduleItem.name) }
    var editedTime by remember { mutableStateOf(scheduleItem.time) }
    
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
                imageVector = Icons.Filled.Event,
                contentDescription = "Schedule Item",
                tint = Purple,
                modifier = Modifier.padding(end = 16.dp)
            )
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = scheduleItem.name,
                    style = MaterialTheme.typography.titleMedium
                )
                
                Row {
                    Text(
                        text = dateFormatter.format(scheduleItem.date),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = scheduleItem.time,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            IconButton(onClick = { showEditDialog = true }) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = "Edit Schedule Item"
                )
            }
            
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Delete Schedule Item",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
    
    // Edit Schedule Item Dialog
    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit Schedule Item") },
            text = {
                Column {
                    OutlinedTextField(
                        value = editedName,
                        onValueChange = { editedName = it },
                        label = { Text("Event Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = editedTime,
                        onValueChange = { editedTime = it },
                        label = { Text("Time (e.g., 10:00)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editedName.isNotBlank() && editedTime.isNotBlank()) {
                            // Get the event that contains this schedule item
                            val events = EventsData.events
                            val event = events.find { it.scheduleItems.any { item -> item.id == scheduleItem.id } }
                            
                            if (event != null) {
                                // Find and update the schedule item in the event
                                val index = event.scheduleItems.indexOfFirst { it.id == scheduleItem.id }
                                if (index != -1) {
                                    // Parse the time if it changed
                                    val calendar = Calendar.getInstance()
                                    calendar.time = scheduleItem.date
                                    
                                    if (editedTime != scheduleItem.time) {
                                        try {
                                            val timeParts = editedTime.split(":")
                                            if (timeParts.size == 2) {
                                                val hour = timeParts[0].toInt()
                                                val minute = timeParts[1].toInt()
                                                
                                                calendar.set(Calendar.HOUR_OF_DAY, hour)
                                                calendar.set(Calendar.MINUTE, minute)
                                            }
                                        } catch (e: Exception) {
                                            // Handle invalid time format
                                        }
                                    }
                                    
                                    event.scheduleItems[index] = scheduleItem.copy(
                                        name = editedName,
                                        time = editedTime,
                                        date = calendar.time
                                    )
                                    
                                    // Update event
                                    EventsData.updateEvent(event)
                                }
                            }
                            
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