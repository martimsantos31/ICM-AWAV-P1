package pt.ua.deti.icm.awav.ui.screens.organizer

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import pt.ua.deti.icm.awav.AWAVApplication
import pt.ua.deti.icm.awav.data.repository.EventsRepository
import pt.ua.deti.icm.awav.data.repository.StandsRepository
import pt.ua.deti.icm.awav.data.room.entity.Event
import pt.ua.deti.icm.awav.data.room.entity.ScheduleItem
import pt.ua.deti.icm.awav.data.room.entity.Stand
import pt.ua.deti.icm.awav.ui.theme.Purple
import pt.ua.deti.icm.awav.ui.viewmodels.EventDetailsViewModel
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.TimeInput
import androidx.compose.material3.rememberTimePickerState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailsScreen(
    eventId: String,
    navController: NavController,
    onManageUsers: () -> Unit = {},
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
    val scheduleItemsState by viewModel.scheduleItems.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    // Log state changes
    LaunchedEffect(eventState) {
        Log.d("EventDetailsScreen", "Event state updated: ${eventState?.id} - ${eventState?.name}")
    }
    
    LaunchedEffect(standsState) {
        Log.d("EventDetailsScreen", "Stands state updated: ${standsState.size} stands loaded")
    }
    
    LaunchedEffect(scheduleItemsState) {
        Log.d("EventDetailsScreen", "Schedule items state updated: ${scheduleItemsState.size} items loaded")
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
                        },
                        onManageUsers = onManageUsers
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
                    2 -> ScheduleTab(
                        scheduleItems = scheduleItemsState,
                        eventId = eventState!!.id,
                        onScheduleItemAdded = { title, startTime, endTime, location ->
                            coroutineScope.launch {
                                viewModel.addScheduleItem(title, startTime, endTime, location, eventState!!.id)
                            }
                        },
                        onScheduleItemDeleted = { scheduleItem ->
                            coroutineScope.launch {
                                viewModel.deleteScheduleItem(scheduleItem)
                            }
                        },
                        onScheduleItemUpdated = { scheduleItem ->
                            coroutineScope.launch {
                                viewModel.updateScheduleItem(scheduleItem)
                            }
                        }
                    )
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
    onEventDeleted: () -> Unit,
    onManageUsers: () -> Unit = {}
) {
    var editMode by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    
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
            
            // Buttons row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Delete button
                Button(
                    onClick = {
                        showDeleteDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete"
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete Event")
                }
                
                // Manage Users button
                Button(
                    onClick = onManageUsers,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Purple,
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Group,
                        contentDescription = "Manage Users"
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Manage Users")
                }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleTab(
    scheduleItems: List<ScheduleItem>,
    eventId: Int,
    onScheduleItemAdded: (title: String, startTime: String, endTime: String, location: String) -> Unit,
    onScheduleItemDeleted: (ScheduleItem) -> Unit,
    onScheduleItemUpdated: (ScheduleItem) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedScheduleItem by remember { mutableStateOf<ScheduleItem?>(null) }
    
    // Display schedule list with add button
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Title and add button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Schedule",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                
                IconButton(
                    onClick = { showAddDialog = true },
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(16.dp)
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Schedule Item",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
            
            if (scheduleItems.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Event,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(64.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "No Schedule Items",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "Add schedule items to help attendees know when events are happening",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                // Schedule items list
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Group by date and sort dates chronologically
                    val groupedItems = scheduleItems.groupBy {
                        it.startTime.split(" ")[0] // Get the date part
                    }.toSortedMap() // Sort the dates in chronological order
                    
                    groupedItems.forEach { (date, itemsForDate) ->
                        item {
                            // Date header
                            Text(
                                text = formatDateHeader(date),
                                style = MaterialTheme.typography.titleMedium,
                                color = Purple,
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
                            )
                        }
                        
                        items(itemsForDate.sortedBy { it.startTime }) { item ->
                            ScheduleItemCard(
                                scheduleItem = item,
                                onEdit = { selectedScheduleItem = item },
                                onDelete = { onScheduleItemDeleted(item) }
                            )
                        }
                    }
                }
            }
        }
    }
    
    // Add/Edit dialog
    if (showAddDialog || selectedScheduleItem != null) {
        val isEdit = selectedScheduleItem != null
        val initialTitle = selectedScheduleItem?.title ?: ""
        val initialLocation = selectedScheduleItem?.location ?: ""
        val initialStartTime = selectedScheduleItem?.startTime ?: getFormattedCurrentDateTime()
        val initialEndTime = selectedScheduleItem?.endTime ?: getFormattedCurrentDateTimePlusHour()
        
        ScheduleItemDialog(
            isEdit = isEdit,
            initialTitle = initialTitle,
            initialLocation = initialLocation,
            initialStartTime = initialStartTime,
            initialEndTime = initialEndTime,
            onDismiss = { 
                showAddDialog = false
                selectedScheduleItem = null
            },
            onSave = { title, startTime, endTime, location ->
                if (isEdit) {
                    // Update existing item
                    val updatedItem = selectedScheduleItem!!.copy(
                        title = title,
                        startTime = startTime,
                        endTime = endTime,
                        location = location
                    )
                    onScheduleItemUpdated(updatedItem)
                } else {
                    // Add new item
                    onScheduleItemAdded(title, startTime, endTime, location)
                }
                showAddDialog = false
                selectedScheduleItem = null
            }
        )
    }
}

@Composable
fun ScheduleItemCard(
    scheduleItem: ScheduleItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Title and time information
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = scheduleItem.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = Purple,
                            modifier = Modifier.size(16.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(4.dp))
                        
                        val timeStr = formatScheduleTime(scheduleItem.startTime, scheduleItem.endTime)
                        Text(
                            text = timeStr,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    
                    if (scheduleItem.location.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = Purple,
                                modifier = Modifier.size(16.dp)
                            )
                            
                            Spacer(modifier = Modifier.width(4.dp))
                            
                            Text(
                                text = scheduleItem.location,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
                
                // Actions
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = Purple
                        )
                    }
                    
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color.Red
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleItemDialog(
    isEdit: Boolean,
    initialTitle: String,
    initialLocation: String,
    initialStartTime: String,
    initialEndTime: String,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String) -> Unit
) {
    var title by remember { mutableStateOf(initialTitle) }
    var location by remember { mutableStateOf(initialLocation) }
    
    // Parse initial dates and times
    val initialStartDateTime = parseDateTime(initialStartTime)
    val initialEndDateTime = parseDateTime(initialEndTime)
    
    // Separate state for tracking the current date and time values
    var selectedStartDate by remember { mutableStateOf(initialStartDateTime) }
    var selectedEndDate by remember { mutableStateOf(initialEndDateTime) }
    
    // Debugging state to see values
    var debugStartTime by remember { mutableStateOf("") }
    var debugEndTime by remember { mutableStateOf("") }
    
    // Update debug values when dates change
    LaunchedEffect(selectedStartDate, selectedEndDate) {
        debugStartTime = "${selectedStartDate.time}: ${formatDateTimeForDatabase(selectedStartDate)}"
        debugEndTime = "${selectedEndDate.time}: ${formatDateTimeForDatabase(selectedEndDate)}"
    }
    
    // Dialog visibility states
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }
    
    // Create date picker states
    val startDateState = rememberDatePickerState(initialSelectedDateMillis = selectedStartDate.time)
    val endDateState = rememberDatePickerState(initialSelectedDateMillis = selectedEndDate.time)
    
    // Create time picker states
    val startTimeState = rememberTimePickerState(
        initialHour = selectedStartDate.hours,
        initialMinute = selectedStartDate.minutes,
        is24Hour = false
    )
    
    val endTimeState = rememberTimePickerState(
        initialHour = selectedEndDate.hours,
        initialMinute = selectedEndDate.minutes,
        is24Hour = false
    )
    
    // Format validation
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = if (isEdit) "Edit Schedule Item" else "Add Schedule Item") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Title field
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Location field
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Start time section
                Text(
                    text = "Start Time",
                    style = MaterialTheme.typography.titleSmall
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Start date button
                    OutlinedButton(
                        onClick = { showStartDatePicker = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = formatDateForDisplay(selectedStartDate),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    
                    // Start time button
                    OutlinedButton(
                        onClick = { showStartTimePicker = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = formatTimeForDisplay(selectedStartDate),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // End time section
                Text(
                    text = "End Time",
                    style = MaterialTheme.typography.titleSmall
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // End date button
                    OutlinedButton(
                        onClick = { showEndDatePicker = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = formatDateForDisplay(selectedEndDate),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    
                    // End time button
                    OutlinedButton(
                        onClick = { showEndTimePicker = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = formatTimeForDisplay(selectedEndDate),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                
                // Error message
                if (showError) {
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    // Validate input
                    if (title.isBlank()) {
                        showError = true
                        errorMessage = "Title cannot be empty"
                        return@Button
                    }
                    
                    // Validate end time is after start time
                    if (selectedEndDate.time <= selectedStartDate.time) {
                        showError = true
                        errorMessage = "End time must be after start time"
                        return@Button
                    }
                    
                    // Format dates for database
                    val formattedStartTime = formatDateTimeForDatabase(selectedStartDate)
                    val formattedEndTime = formatDateTimeForDatabase(selectedEndDate)
                    
                    onSave(title, formattedStartTime, formattedEndTime, location)
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Cancel")
            }
        }
    )
    
    // Date pickers
    if (showStartDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        // Update the start date with the selected date (keeping the same time)
                        startDateState.selectedDateMillis?.let { millis ->
                            val calendar = Calendar.getInstance()
                            calendar.timeInMillis = millis
                            
                            // Extract time from current selection
                            val hourOfDay = selectedStartDate.hours
                            val minute = selectedStartDate.minutes
                            
                            // Set the time components
                            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                            calendar.set(Calendar.MINUTE, minute)
                            calendar.set(Calendar.SECOND, 0)
                            calendar.set(Calendar.MILLISECOND, 0)
                            
                            selectedStartDate = calendar.time
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
            DatePicker(state = startDateState)
        }
    }
    
    if (showEndDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        // Update the end date with the selected date (keeping the same time)
                        endDateState.selectedDateMillis?.let { millis ->
                            val calendar = Calendar.getInstance()
                            calendar.timeInMillis = millis
                            
                            // Extract time from current selection
                            val hourOfDay = selectedEndDate.hours
                            val minute = selectedEndDate.minutes
                            
                            // Set the time components
                            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                            calendar.set(Calendar.MINUTE, minute)
                            calendar.set(Calendar.SECOND, 0)
                            calendar.set(Calendar.MILLISECOND, 0)
                            
                            selectedEndDate = calendar.time
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
            DatePicker(state = endDateState)
        }
    }
    
    // Time pickers
    if (showStartTimePicker) {
        TimePickerDialog(
            onDismissRequest = { showStartTimePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        // Update the start time (keeping the same date)
                        val calendar = Calendar.getInstance()
                        calendar.time = selectedStartDate
                        
                        // Set new time
                        calendar.set(Calendar.HOUR_OF_DAY, startTimeState.hour)
                        calendar.set(Calendar.MINUTE, startTimeState.minute)
                        calendar.set(Calendar.SECOND, 0)
                        calendar.set(Calendar.MILLISECOND, 0)
                        
                        selectedStartDate = calendar.time
                        showStartTimePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showStartTimePicker = false }
                ) {
                    Text("Cancel")
                }
            }
        ) {
            TimePicker(state = startTimeState)
        }
    }
    
    if (showEndTimePicker) {
        TimePickerDialog(
            onDismissRequest = { showEndTimePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        // Update the end time (keeping the same date)
                        val calendar = Calendar.getInstance()
                        calendar.time = selectedEndDate
                        
                        // Set new time
                        calendar.set(Calendar.HOUR_OF_DAY, endTimeState.hour)
                        calendar.set(Calendar.MINUTE, endTimeState.minute)
                        calendar.set(Calendar.SECOND, 0)
                        calendar.set(Calendar.MILLISECOND, 0)
                        
                        selectedEndDate = calendar.time
                        showEndTimePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showEndTimePicker = false }
                ) {
                    Text("Cancel")
                }
            }
        ) {
            TimePicker(state = endTimeState)
        }
    }
}

@Composable
fun TimePickerDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Select Time") },
        text = { content() },
        confirmButton = confirmButton,
        dismissButton = dismissButton
    )
}

// Helper methods for date formatting
private fun formatDateHeader(dateStr: String): String {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val displayFormat = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
    
    return try {
        val date = dateFormat.parse(dateStr)
        displayFormat.format(date)
    } catch (e: Exception) {
        dateStr
    }
}

private fun formatScheduleTime(startTimeStr: String, endTimeStr: String): String {
    val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    
    return try {
        val startTime = inputFormat.parse(startTimeStr)
        val endTime = inputFormat.parse(endTimeStr)
        
        "${timeFormat.format(startTime)} - ${timeFormat.format(endTime)}"
    } catch (e: Exception) {
        "$startTimeStr - $endTimeStr"
    }
}

private fun getFormattedCurrentDateTime(): String {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    return dateFormat.format(Date())
}

private fun getFormattedCurrentDateTimePlusHour(): String {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.HOUR_OF_DAY, 1)
    return dateFormat.format(calendar.time)
}

private fun parseDateTime(dateTimeStr: String): Date {
    return try {
        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).parse(dateTimeStr) ?: Date()
    } catch (e: Exception) {
        Date()
    }
}

private fun formatDateForDisplay(date: Date): String {
    return SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault()).format(date)
}

private fun formatTimeForDisplay(date: Date): String {
    return SimpleDateFormat("h:mm a", Locale.getDefault()).format(date)
}

private fun formatDateTimeForDatabase(date: Date): String {
    return SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(date)
} 