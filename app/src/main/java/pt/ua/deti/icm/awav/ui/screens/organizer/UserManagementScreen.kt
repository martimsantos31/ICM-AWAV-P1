package pt.ua.deti.icm.awav.ui.screens.organizer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import pt.ua.deti.icm.awav.data.model.UserProfile
import pt.ua.deti.icm.awav.data.model.UserRole
import pt.ua.deti.icm.awav.ui.viewmodels.UserManagementViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserManagementScreen(
    navController: NavController,
    viewModel: UserManagementViewModel = viewModel()
) {
    val users by viewModel.users.collectAsState()
    val stands by viewModel.stands.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    // Selected user for stand assignment
    var selectedUser by remember { mutableStateOf<UserProfile?>(null) }
    var showStandDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("User Management") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refreshUsers() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh"
                        )
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
            when {
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        items(users) { user ->
                            UserCard(
                                user = user,
                                onAssignStand = {
                                    selectedUser = user
                                    showStandDialog = true
                                }
                            )
                        }
                    }
                }
            }
            
            // Dialog for stand assignment
            if (showStandDialog && selectedUser != null) {
                AssignStandDialog(
                    user = selectedUser!!,
                    stands = stands,
                    assignedStands = selectedUser!!.managedStandIds,
                    onDismiss = { showStandDialog = false },
                    onAssign = { standId ->
                        viewModel.assignStandToUser(selectedUser!!.id, standId)
                        showStandDialog = false
                    },
                    onRemove = { standId ->
                        viewModel.removeStandFromUser(selectedUser!!.id, standId)
                        showStandDialog = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserCard(
    user: UserProfile,
    onAssignStand: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = user.name,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = user.email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Role: ${user.role.name}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    if (user.role == UserRole.STAND_WORKER && user.managedStandIds.isNotEmpty()) {
                        Text(
                            text = "Managing stands: ${user.managedStandIds.joinToString(", ")}",
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                
                // Only show assign button for STAND_WORKER role
                if (user.role == UserRole.STAND_WORKER) {
                    IconButton(onClick = onAssignStand) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Assign Stand"
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignStandDialog(
    user: UserProfile,
    stands: List<Pair<String, String>>, // Pair of (standId, standName)
    assignedStands: List<String>,
    onDismiss: () -> Unit,
    onAssign: (String) -> Unit,
    onRemove: (String) -> Unit
) {
    var selectedStandId by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Manage Stands for ${user.name}") },
        text = {
            Column {
                Text("Currently assigned stands:", style = MaterialTheme.typography.labelMedium)
                if (assignedStands.isEmpty()) {
                    Text("No stands assigned", style = MaterialTheme.typography.bodyMedium)
                } else {
                    assignedStands.forEach { standId ->
                        val standName = stands.find { it.first == standId }?.second ?: standId
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(standName, style = MaterialTheme.typography.bodyMedium)
                            IconButton(onClick = { onRemove(standId) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Remove")
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                Text("Assign new stand:", style = MaterialTheme.typography.labelMedium)
                
                val availableStands = stands.filter { it.first !in assignedStands }
                if (availableStands.isEmpty()) {
                    Text("No more stands available", style = MaterialTheme.typography.bodyMedium)
                } else {
                    ExposedDropdownMenuBox(
                        expanded = false,
                        onExpandedChange = { },
                    ) {
                        TextField(
                            value = selectedStandId.ifEmpty { "Select a stand" },
                            onValueChange = { },
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = false) },
                            modifier = Modifier
                                .fillMaxWidth()
                        )
                        
                        ExposedDropdownMenu(
                            expanded = false,
                            onDismissRequest = { },
                        ) {
                            availableStands.forEach { (id, name) ->
                                DropdownMenuItem(
                                    text = { Text(name) },
                                    onClick = {
                                        selectedStandId = id
                                    }
                                )
                            }
                        }
                    }
                    
                    // Temporary solution since the dropdown is not functional
                    // Show available stands as a list of buttons
                    Column {
                        availableStands.forEach { (id, name) ->
                            Button(
                                onClick = { selectedStandId = id },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Text(name)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (selectedStandId.isNotEmpty()) onAssign(selectedStandId) },
                enabled = selectedStandId.isNotEmpty()
            ) {
                Text("Assign")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
} 