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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailsScreen(
    eventId: String,
    navController: NavController
) {
    // In a real app, this would come from a repository
    val event = remember {
        Event(
            id = eventId,
            title = if (eventId == "1") "Enterro 25" else "BE NEI",
            description = "lorem ipsum lorem awqdi jsdoaija aodijajepoj ojajdoajjc ojadoj qojedj",
            imageUrl = "https://via.placeholder.com/150",
            startDate = if (eventId == "1") "26Apr" else "4May",
            endDate = if (eventId == "1") "3May" else "5May"
        )
    }

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
                0 -> EventDetailsTab(event)
                1 -> ManageStandsTab()
                2 -> ScheduleTab()
            }
        }
    }
}

@Composable
fun EventDetailsTab(event: Event) {
    var editMode by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf(event.title) }
    var description by remember { mutableStateOf(event.description) }
    var startDate by remember { mutableStateOf(event.startDate) }
    var endDate by remember { mutableStateOf(event.endDate) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Image
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.LightGray.copy(alpha = 0.5f))
        ) {
            AsyncImage(
                model = event.imageUrl,
                contentDescription = event.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            
            // Edit button
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                contentAlignment = Alignment.TopEnd
            ) {
                FloatingActionButton(
                    onClick = { editMode = !editMode },
                    containerColor = Purple,
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
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = startDate,
                    onValueChange = { startDate = it },
                    label = { Text("Start Date") },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Purple,
                        focusedLabelColor = Purple
                    )
                )
                
                OutlinedTextField(
                    value = endDate,
                    onValueChange = { endDate = it },
                    label = { Text("End Date") },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Purple,
                        focusedLabelColor = Purple
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Delete button
            Button(
                onClick = { /* TODO: Delete event and navigate back */ },
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
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = description,
                style = MaterialTheme.typography.bodyLarge
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
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
                    text = "$startDate - $endDate",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun ManageStandsTab() {
    // Sample stands data
    val stands = remember {
        listOf(
            "Daniel's Chorizo",
            "NEI Coffee",
            "NEECT Food Truck"
        )
    }
    
    var showAddStandDialog by remember { mutableStateOf(false) }
    var newStandName by remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
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
        stands.forEach { standName ->
            StandItem(
                name = standName,
                onEdit = { /* TODO: Edit stand */ },
                onDelete = { /* TODO: Delete stand */ }
            )
        }
    }
    
    // Add Stand Dialog
    if (showAddStandDialog) {
        AlertDialog(
            onDismissRequest = { showAddStandDialog = false },
            title = { Text("Add New Stand") },
            text = {
                OutlinedTextField(
                    value = newStandName,
                    onValueChange = { newStandName = it },
                    label = { Text("Stand Name") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        // TODO: Add stand logic
                        showAddStandDialog = false
                        newStandName = ""
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
    name: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
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
                imageVector = Icons.Filled.Store,
                contentDescription = "Stand",
                tint = Purple,
                modifier = Modifier.padding(end = 16.dp)
            )
            
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            
            IconButton(onClick = onEdit) {
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
}

@Composable
fun ScheduleTab() {
    // Sample schedule data
    val scheduleItems = remember {
        listOf(
            "Opening Ceremony" to "26 Apr, 10:00",
            "Workshops" to "26 Apr, 14:00",
            "Conference" to "27 Apr, 09:00",
            "Dinner" to "27 Apr, 20:00",
            "Games" to "28 Apr, 15:00",
            "Closing Party" to "3 May, 22:00"
        )
    }
    
    var showAddScheduleDialog by remember { mutableStateOf(false) }
    var newEventName by remember { mutableStateOf("") }
    var newEventTime by remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
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
        scheduleItems.forEach { (name, time) ->
            ScheduleItem(
                name = name,
                time = time,
                onEdit = { /* TODO: Edit schedule item */ },
                onDelete = { /* TODO: Delete schedule item */ }
            )
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
                    
                    OutlinedTextField(
                        value = newEventTime,
                        onValueChange = { newEventTime = it },
                        label = { Text("Date & Time (e.g., 26 Apr, 10:00)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        // TODO: Add schedule item logic
                        showAddScheduleDialog = false
                        newEventName = ""
                        newEventTime = ""
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
    name: String,
    time: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
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
                    text = name,
                    style = MaterialTheme.typography.titleMedium
                )
                
                Text(
                    text = time,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            IconButton(onClick = onEdit) {
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
} 