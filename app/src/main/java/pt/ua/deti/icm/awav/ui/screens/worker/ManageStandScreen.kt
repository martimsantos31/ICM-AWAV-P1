package pt.ua.deti.icm.awav.ui.screens.worker

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import pt.ua.deti.icm.awav.data.model.Product
import pt.ua.deti.icm.awav.data.repository.StandRepository
import pt.ua.deti.icm.awav.ui.theme.AWAVStyles
import pt.ua.deti.icm.awav.ui.theme.Purple
import pt.ua.deti.icm.awav.ui.viewmodels.ManageStandViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageStandScreen(
    navController: NavController,
    viewModel: ManageStandViewModel = viewModel()
) {
    val coroutineScope = rememberCoroutineScope()
    
    // UI state
    var showAddProductDialog by remember { mutableStateOf(false) }
    var showEditProductDialog by remember { mutableStateOf(false) }
    var newProductName by remember { mutableStateOf("") }
    var newProductPrice by remember { mutableStateOf("") }
    var newProductDescription by remember { mutableStateOf("") }
    
    // State for product being edited
    var editingProduct by remember { mutableStateOf<Product?>(null) }
    
    // ViewModel state
    val standName = viewModel.standName.collectAsState().value
    val products = viewModel.products.collectAsState().value
    val hasAssignedStands = viewModel.hasAssignedStands.collectAsState().value
    val assignedStands = viewModel.assignedStands.collectAsState().value
    val isLoading = viewModel.isLoading.collectAsState().value
    
    // Get first stand ID if available (used for operations)
    val currentStandId = if (assignedStands.isNotEmpty()) assignedStands.first() else ""
    val currentStandIdInt = currentStandId.toIntOrNull() ?: 0
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = standName,
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            if (hasAssignedStands) {
                FloatingActionButton(
                    onClick = { showAddProductDialog = true },
                    containerColor = Purple
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Product",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            if (isLoading) {
                // Show loading indicator
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (!hasAssignedStands) {
                // Show no stands assigned message
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
            } else {
                // Show products section when user has stands assigned
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Products Section
                    Text(
                        text = "Products",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    LazyColumn(
                        modifier = Modifier.weight(1f)
                    ) {
                        items(products) { product ->
                            ProductItem(
                                product = product,
                                onEdit = { 
                                    // Set the product being edited and show the edit dialog
                                    editingProduct = product
                                    newProductName = product.name
                                    newProductPrice = product.price.toString()
                                    newProductDescription = product.description
                                    showEditProductDialog = true
                                },
                                onDelete = { 
                                    coroutineScope.launch {
                                        viewModel.deleteProduct(product, currentStandIdInt)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
    
    // Add Product Dialog
    if (showAddProductDialog) {
        AlertDialog(
            onDismissRequest = { showAddProductDialog = false },
            title = { Text("Add New Product") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newProductName,
                        onValueChange = { newProductName = it },
                        label = { Text("Product Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = newProductPrice,
                        onValueChange = { newProductPrice = it },
                        label = { Text("Price (€)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = newProductDescription,
                        onValueChange = { newProductDescription = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        // Add the new product
                        try {
                            val price = newProductPrice.toDoubleOrNull() ?: 0.0
                            val newProduct = Product(
                                id = "", // Empty ID will be generated in the repository
                                name = newProductName,
                                description = newProductDescription,
                                price = price
                            )
                            
                            // Save the product to the database via ViewModel
                            coroutineScope.launch {
                                viewModel.saveProduct(newProduct, currentStandIdInt)
                            }
                            
                            // Reset fields
                            newProductName = ""
                            newProductPrice = ""
                            newProductDescription = ""
                            showAddProductDialog = false
                        } catch (e: Exception) {
                            // Handle error
                        }
                    },
                    enabled = newProductName.isNotBlank() && newProductPrice.isNotBlank()
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddProductDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Edit Product Dialog
    if (showEditProductDialog && editingProduct != null) {
        AlertDialog(
            onDismissRequest = { 
                showEditProductDialog = false
                editingProduct = null
            },
            title = { Text("Edit Product") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newProductName,
                        onValueChange = { newProductName = it },
                        label = { Text("Product Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = newProductPrice,
                        onValueChange = { newProductPrice = it },
                        label = { Text("Price (€)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = newProductDescription,
                        onValueChange = { newProductDescription = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        // Update the existing product
                        try {
                            val price = newProductPrice.toDoubleOrNull() ?: 0.0
                            val updatedProduct = editingProduct!!.copy(
                                name = newProductName,
                                description = newProductDescription,
                                price = price
                            )
                            
                            // Update the product in the database via ViewModel
                            coroutineScope.launch {
                                viewModel.updateProduct(updatedProduct, currentStandIdInt)
                            }
                            
                            // Reset fields
                            editingProduct = null
                            newProductName = ""
                            newProductPrice = ""
                            newProductDescription = ""
                            showEditProductDialog = false
                        } catch (e: Exception) {
                            // Handle error
                        }
                    },
                    enabled = newProductName.isNotBlank() && newProductPrice.isNotBlank()
                ) {
                    Text("Update")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showEditProductDialog = false
                    editingProduct = null
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ProductItem(
    product: Product,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium
                )
                
                Text(
                    text = product.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Text(
                    text = "€${String.format("%.2f", product.price)}",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            IconButton(onClick = onEdit) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Product"
                )
            }
            
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Product",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
} 