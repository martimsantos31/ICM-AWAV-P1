package pt.ua.deti.icm.awav.ui.screens.worker

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
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
    var newProductName by remember { mutableStateOf("") }
    var newProductPrice by remember { mutableStateOf("") }
    var newProductDescription by remember { mutableStateOf("") }
    
    // State from ViewModel
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val standName by viewModel.standName.collectAsState()
    val stands by viewModel.stands.collectAsState()
    val selectedStand by viewModel.selectedStand.collectAsState()
    val products by viewModel.products.collectAsState()
    
    // Selected stand state
    var showStandSelector by remember { mutableStateOf(false) }
    
    // Load assigned stands when screen is created
    LaunchedEffect(Unit) {
        viewModel.loadAssignedStands()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = if (stands.size > 1) "$standName (Tap to change)" else standName,
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = stands.size > 1) { 
                                if (stands.size > 1) showStandSelector = true 
                            }
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
            if (selectedStand != null) {
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
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Purple
                    )
                }
                error != null -> {
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
                        Button(onClick = { viewModel.loadAssignedStands() }) {
                            Text("Retry")
                        }
                    }
                }
                selectedStand == null -> {
                    // No stand selected or available
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (stands.isEmpty()) 
                                "You don't have any stands assigned" 
                            else 
                                "Select a stand to manage",
                            textAlign = TextAlign.Center
                        )
                        
                        if (stands.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { showStandSelector = true }) {
                                Text("Select Stand")
                            }
                        }
                    }
                }
                else -> {
                    // Show products for selected stand
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        // Products Section
                        Text(
                            text = "Products",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        if (products.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No products available. Add some!",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.weight(1f)
                            ) {
                                items(products) { product ->
                                    ProductItem(
                                        product = product,
                                        onEdit = { /* TODO: Edit product */ },
                                        onDelete = { 
                                            coroutineScope.launch {
                                                selectedStand?.id?.let { standId ->
                                                    viewModel.deleteProduct(product, standId)
                                                }
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Add Product Dialog
    if (showAddProductDialog && selectedStand != null) {
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
                                selectedStand?.id?.let { standId ->
                                    viewModel.saveProduct(newProduct, standId)
                                }
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
    
    // Stand Selector Dialog
    if (showStandSelector && stands.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { showStandSelector = false },
            title = { Text("Select Stand") },
            text = {
                LazyColumn {
                    items(stands) { stand ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { 
                                    viewModel.selectStand(stand.id)
                                    showStandSelector = false
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = if (selectedStand?.id == stand.id)
                                    MaterialTheme.colorScheme.primaryContainer
                                else
                                    MaterialTheme.colorScheme.surfaceVariant
                            )
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
                                        text = stand.name,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    
                                    if (stand.description.isNotBlank()) {
                                        Text(
                                            text = stand.description,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                
                                if (selectedStand?.id == stand.id) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected Stand",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showStandSelector = false }) {
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