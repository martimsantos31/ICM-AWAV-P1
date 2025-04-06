package pt.ua.deti.icm.awav.ui.screens.organizer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import pt.ua.deti.icm.awav.ui.theme.AWAVStyles
import pt.ua.deti.icm.awav.ui.theme.Purple
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventScreen(navController: NavController) {
    var eventName by remember { mutableStateOf("") }
    var eventDescription by remember { mutableStateOf("") }
    var eventLocation by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf(Date()) }
    var endDate by remember { mutableStateOf(Date()) }
    
    val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            
            // Event Name
            OutlinedTextField(
                value = eventName,
                onValueChange = { eventName = it },
                label = { Text("Event Name") },
                modifier = Modifier.fillMaxWidth()
            )
            
            // Event Description
            OutlinedTextField(
                value = eventDescription,
                onValueChange = { eventDescription = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
            
            // Event Location
            OutlinedTextField(
                value = eventLocation,
                onValueChange = { eventLocation = it },
                label = { Text("Location") },
                modifier = Modifier.fillMaxWidth()
            )
            
            // Date Pickers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { /* TODO: Show date picker */ },
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
                    onClick = { /* TODO: Show date picker */ },
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
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            // Stand Management Section
            Text(
                text = "Stands",
                style = MaterialTheme.typography.titleMedium
            )
            
            // Add stand button
            OutlinedButton(
                onClick = { /* TODO: Add stand dialog */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Stand"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Stand")
            }
            
            // List of stands would go here
            // ...
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            // Ticket Types Section
            Text(
                text = "Ticket Types",
                style = MaterialTheme.typography.titleMedium
            )
            
            // Add ticket type button
            OutlinedButton(
                onClick = { /* TODO: Add ticket type dialog */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Ticket Type"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Ticket Type")
            }
            
            // List of ticket types would go here
            // ...
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Create Event Button
            Button(
                onClick = { /* TODO: Create event and save to database */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(AWAVStyles.buttonHeight),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Purple
                )
            ) {
                Text("Create Event")
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
} 