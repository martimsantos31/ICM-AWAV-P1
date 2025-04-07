package pt.ua.deti.icm.awav.ui.screens.worker

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import pt.ua.deti.icm.awav.data.model.Order
import pt.ua.deti.icm.awav.data.model.OrderItem
import pt.ua.deti.icm.awav.data.model.OrderStatus
import pt.ua.deti.icm.awav.ui.viewmodels.OrdersViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersScreen(
    navController: NavController,
    viewModel: OrdersViewModel = viewModel()
) {
    val orders = viewModel.orders.collectAsState(initial = emptyList()).value
    val isLoading = viewModel.isLoading.collectAsState(initial = true).value
    val hasAssignedStands = viewModel.hasAssignedStands.collectAsState(initial = false).value
    val waitTimeMinutes = viewModel.waitTimeMinutes.collectAsState(initial = 15).value
    
    var showWaitTimeDialog by remember { mutableStateOf(false) }
    var newWaitTime by remember { mutableStateOf(waitTimeMinutes.toString()) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Orders") },
                actions = {
                    // Wait time button
                    TextButton(
                        onClick = { 
                            newWaitTime = waitTimeMinutes.toString()
                            showWaitTimeDialog = true 
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = "Set Wait Time"
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$waitTimeMinutes min wait",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                    
                    // Refresh button
                    IconButton(onClick = { viewModel.refreshOrders() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (isLoading) {
                // Loading indicator
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (!hasAssignedStands) {
                // No stands assigned message
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "No stands assigned",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "You don't have any stands assigned",
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Please contact an organizer to assign you to a stand",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else if (orders.isEmpty()) {
                // No orders message
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Restaurant,
                        contentDescription = "No orders",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "No orders yet",
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "New orders will appear here",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                // Orders list
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    items(orders) { order ->
                        OrderCard(
                            order = order,
                            onStatusChange = { newStatus ->
                                viewModel.updateOrderStatus(order.id, newStatus)
                            }
                        )
                    }
                }
            }
        }
    }
    
    // Show wait time dialog
    if (showWaitTimeDialog) {
        AlertDialog(
            onDismissRequest = { showWaitTimeDialog = false },
            title = { Text("Set Wait Time") },
            text = {
                Column {
                    Text("Current wait time: $waitTimeMinutes minutes")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newWaitTime,
                        onValueChange = { 
                            // Only allow digits
                            if (it.all { char -> char.isDigit() } || it.isEmpty()) {
                                newWaitTime = it 
                            }
                        },
                        label = { Text("Wait Time (minutes)") },
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Number
                        ),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val minutes = newWaitTime.toIntOrNull() ?: waitTimeMinutes
                        viewModel.updateWaitTime(minutes)
                        showWaitTimeDialog = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showWaitTimeDialog = false 
                        newWaitTime = waitTimeMinutes.toString()
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun OrderCard(
    order: Order,
    onStatusChange: (OrderStatus) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val dateFormat = SimpleDateFormat("HH:mm, dd MMM", Locale.getDefault())
    
    // Get color based on status
    val statusColor = when(order.status) {
        OrderStatus.PENDING -> Color(0xFFFFA000)    // Amber
        OrderStatus.PREPARING -> Color(0xFF2196F3)  // Blue
        OrderStatus.READY -> Color(0xFF4CAF50)      // Green
        OrderStatus.COMPLETED -> Color(0xFF9E9E9E)  // Grey
        OrderStatus.CANCELLED -> Color(0xFFF44336)  // Red
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { expanded = !expanded },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Order ID and customer name
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Order #${order.id.takeLast(4)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = order.userName,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Text(
                        text = dateFormat.format(order.createdAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Price and status
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "€%.2f".format(order.totalPrice),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = order.status.name,
                        style = MaterialTheme.typography.labelMedium,
                        color = statusColor,
                        modifier = Modifier
                            .border(1.dp, statusColor, RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
            
            // Expandable content
            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    // Divider
                    Divider()
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Items
                    Text(
                        text = "Items",
                        style = MaterialTheme.typography.titleSmall
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    order.items.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${item.quantity}×",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                
                                Text(
                                    text = item.productName,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            
                            Text(
                                text = "€%.2f".format(item.price * item.quantity),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        
                        // Item notes if any
                        if (item.notes.isNotEmpty()) {
                            Text(
                                text = item.notes,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = 24.dp, top = 2.dp)
                            )
                        }
                    }
                    
                    // Order notes if any
                    if (order.notes.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "Notes",
                            style = MaterialTheme.typography.titleSmall
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = order.notes,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Action buttons based on status
                    when (order.status) {
                        OrderStatus.PENDING -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { onStatusChange(OrderStatus.CANCELLED) },
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = Color(0xFFF44336)
                                    ),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Cancel")
                                }
                                
                                Button(
                                    onClick = { onStatusChange(OrderStatus.PREPARING) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF2196F3)
                                    ),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Prepare")
                                }
                            }
                        }
                        OrderStatus.PREPARING -> {
                            Button(
                                onClick = { onStatusChange(OrderStatus.READY) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF4CAF50)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Mark as Ready")
                            }
                        }
                        OrderStatus.READY -> {
                            Button(
                                onClick = { onStatusChange(OrderStatus.COMPLETED) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Complete Order")
                            }
                        }
                        else -> {
                            // No actions for completed or cancelled orders
                        }
                    }
                }
            }
        }
    }
} 