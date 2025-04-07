package pt.ua.deti.icm.awav.ui.screens.organizer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import pt.ua.deti.icm.awav.data.model.UserProfile
import pt.ua.deti.icm.awav.data.model.UserRole
import pt.ua.deti.icm.awav.data.room.entity.Stand
import pt.ua.deti.icm.awav.data.room.entity.Worker
import pt.ua.deti.icm.awav.ui.theme.Purple
import pt.ua.deti.icm.awav.ui.viewmodels.UserManagementViewModel
import android.util.Log

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageUsersScreen(
    eventId: String,
    navController: NavController,
    viewModel: UserManagementViewModel = viewModel()
) {
    // State
    val users by viewModel.users.collectAsState()
    val stands by viewModel.stands.collectAsState()
    val workers by viewModel.workers.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    // Selected state for dialogs
    var selectedUser by remember { mutableStateOf<UserProfile?>(null) }
    var selectedStand by remember { mutableStateOf<Stand?>(null) }
    
    // Manage which section is expanded
    var expandUsersSection by remember { mutableStateOf(true) }
    var expandWorkersSection by remember { mutableStateOf(true) }
    
    // Debug info
    val eventIdInt = eventId.toIntOrNull()
    
    // Load data when screen is created
    LaunchedEffect(eventId) {
        val parsedEventId = eventId.toIntOrNull()
        if (parsedEventId != null) {
            Log.d("ManageUsersScreen", "Loading stands for event ID: $parsedEventId")
            viewModel.loadStandsForEvent(parsedEventId)
        } else {
            Log.e("ManageUsersScreen", "Invalid event ID: $eventId")
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Manage Users (Event #$eventId)",
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Purple
                )
            } else if (error != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = error ?: "An unknown error occurred",
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { 
                        viewModel.loadUsers()
                        eventIdInt?.let { viewModel.loadStandsForEvent(it) }
                    }) {
                        Text("Retry")
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Debug info for stands
                    Text(
                        text = "Event ID: $eventId, Found ${stands.size} stands, ${workers.size} workers",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Section: Users
                        item {
                            SectionHeader(
                                title = "Users",
                                isExpanded = expandUsersSection,
                                onToggle = { expandUsersSection = !expandUsersSection }
                            )
                        }
                        
                        if (expandUsersSection) {
                            items(users.filter { it.role != UserRole.ORGANIZER }) { user ->
                                UserItem(
                                    user = user,
                                    onClick = { 
                                        Log.d("ManageUsersScreen", "User selected: ${user.name}, Email: '${user.email}', ID: '${user.id}', role: ${user.role}, stands available: ${stands.size}")
                                        selectedUser = user 
                                    }
                                )
                            }
                            
                            if (users.isEmpty() || users.all { it.role == UserRole.ORGANIZER }) {
                                item {
                                    EmptyState(message = "No users found")
                                }
                            }
                        }
                        
                        // Section: Current Workers
                        item {
                            Spacer(modifier = Modifier.height(24.dp))
                            SectionHeader(
                                title = "Assigned Workers",
                                isExpanded = expandWorkersSection,
                                onToggle = { expandWorkersSection = !expandWorkersSection }
                            )
                        }
                        
                        if (expandWorkersSection) {
                            items(workers) { workerWithUser ->
                                WorkerItem(
                                    workerWithUser = workerWithUser,
                                    onDelete = {
                                        viewModel.removeWorker(workerWithUser.worker)
                                    }
                                )
                            }
                            
                            if (workers.isEmpty()) {
                                item {
                                    EmptyState(message = "No workers assigned")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Dialog to select a stand when a user is clicked
    if (selectedUser != null) {
        SelectStandDialog(
            user = selectedUser!!,
            stands = stands,
            onDismiss = { selectedUser = null },
            onAssign = { stand ->
                Log.d("ManageUsersScreen", "Assigning ${selectedUser!!.name} to stand ${stand.name}")
                Log.d("ID", selectedUser!!.id)
                viewModel.assignWorkerToStand(
                    userId = selectedUser!!.id,
                    userName = selectedUser!!.name,
                    standId = stand.id
                )
                selectedUser = null
            }
        )
    }
}

@Composable
fun SectionHeader(
    title: String,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onToggle() },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Purple
        )
        
        Icon(
            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
            contentDescription = if (isExpanded) "Collapse" else "Expand",
            tint = Purple
        )
    }
    
    HorizontalDivider(
        color = Purple.copy(alpha = 0.3f),
        thickness = 2.dp,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
fun UserItem(
    user: UserProfile,
    onClick: () -> Unit
) {
    // Check if user has a valid email
    val hasValidEmail = user.email.isNotBlank()
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (hasValidEmail) 
                MaterialTheme.colorScheme.surfaceVariant 
            else 
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // User avatar
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Purple.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                if (user.photoUrl != null) {
                    AsyncImage(
                        model = user.photoUrl,
                        contentDescription = user.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = user.name.take(1).uppercase(),
                        style = MaterialTheme.typography.titleLarge,
                        color = Purple
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // User info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = user.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = user.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = user.role.name.lowercase().replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.bodySmall,
                        color = Purple
                    )
                    
                    if (!hasValidEmail) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Missing Email",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "No Email",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            
            // Icon
            Icon(
                imageVector = if (hasValidEmail) Icons.Default.Add else Icons.Default.Block,
                contentDescription = if (hasValidEmail) "Assign to Stand" else "Cannot Assign",
                tint = if (hasValidEmail) Purple else MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
fun WorkerItem(
    workerWithUser: UserManagementViewModel.WorkerWithUser,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // User avatar
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Purple.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                if (workerWithUser.userProfile.photoUrl != null) {
                    AsyncImage(
                        model = workerWithUser.userProfile.photoUrl,
                        contentDescription = workerWithUser.userProfile.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = workerWithUser.userProfile.name.take(1).uppercase(),
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Worker info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = workerWithUser.userProfile.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = workerWithUser.userProfile.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                Text(
                    text = "Assigned to: ${workerWithUser.standName}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
            
            // Delete icon
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Remove Worker",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun EmptyState(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun SelectStandDialog(
    user: UserProfile,
    stands: List<Stand>,
    onDismiss: () -> Unit,
    onAssign: (Stand) -> Unit
) {
    // Use email as the primary identifier
    val userIdentifier = user.email
    val hasValidIdentifier = userIdentifier.isNotBlank()
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Assign ${user.name} to Stand") },
        text = {
            Column {
                // Show warning for missing identifier
                if (!hasValidIdentifier) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                            .background(
                                color = MaterialTheme.colorScheme.errorContainer,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Warning",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "This user has no email address and cannot be assigned to a stand. Please ensure the user's email is properly set up.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                
                // Show available stands or empty message
                if (stands.isEmpty()) {
                    Text("No stands available for this event.")
                } else if (!hasValidIdentifier) {
                    Text("Cannot assign user without an email address.")
                } else {
                    LazyColumn {
                        items(stands) { stand ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable { onAssign(stand) },
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
                                        imageVector = Icons.Default.Store,
                                        contentDescription = null,
                                        tint = Purple,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    
                                    Spacer(modifier = Modifier.width(12.dp))
                                    
                                    Column {
                                        Text(
                                            text = stand.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        
                                        if (stand.description.isNotBlank()) {
                                            Text(
                                                text = stand.description,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
} 