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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import pt.ua.deti.icm.awav.data.model.Product
import pt.ua.deti.icm.awav.ui.theme.AWAVStyles
import pt.ua.deti.icm.awav.ui.theme.Purple

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageStandScreen(navController: NavController) {
    // This would be loaded from a repository in a real app
    var standName by remember { mutableStateOf("Daniel's Chorizo") }
    var showAddProductDialog by remember { mutableStateOf(false) }
    var newProductName by remember { mutableStateOf("") }
    var newProductPrice by remember { mutableStateOf("") }
    var newProductDescription by remember { mutableStateOf("") }
    
    // Sample products
    val products = remember {
        mutableStateListOf(
            Product(id = "1", name = "Chorizo Standard", description = "Regular chorizo sausage", price = 5.0),
            Product(id = "2", name = "Chorizo Spicy", description = "Spicy chorizo variant", price = 5.5),
            Product(id = "3", name = "Water", description = "Bottled water 500ml", price = 1.5)
        )
    }
    
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
                )
            )
        },
        floatingActionButton = {
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
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // NFC Payment Section has been removed
            
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
                        onEdit = { /* TODO: Edit product */ },
                        onDelete = { products.remove(product) }
                    )
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
                                id = (products.size + 1).toString(),
                                name = newProductName,
                                description = newProductDescription,
                                price = price
                            )
                            products.add(newProduct)
                            
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