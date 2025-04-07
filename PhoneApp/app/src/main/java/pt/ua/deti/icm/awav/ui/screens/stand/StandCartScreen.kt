package pt.ua.deti.icm.awav.ui.screens.stand

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import pt.ua.deti.icm.awav.R
import pt.ua.deti.icm.awav.data.model.CartItem
import pt.ua.deti.icm.awav.data.repository.StandRepository
import pt.ua.deti.icm.awav.ui.theme.Purple

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StandCartScreen(
    standId: String,
    navController: NavController
) {
    val stand by remember { mutableStateOf(StandRepository.getStandById(standId)) }
    val cart by remember { mutableStateOf(StandRepository.getCart(standId)) }
    val cartItems by remember { mutableStateOf(cart.getItems()) }
    val totalPrice by remember { derivedStateOf { cart.getTotalPrice() } }
    
    if (stand == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Stand not found")
        }
        return
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = null,
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Cart",
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Center,
                            color = Color.White
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Purple,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (cart.isEmpty()) {
                // Show empty cart message
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Your cart is empty",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            } else {
                // Show item count
                Text(
                    text = "${cart.getTotalItems()} items",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(16.dp)
                )
                
                // Cart items
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    items(cartItems) { cartItem ->
                        CartItemCard(
                            cartItem = cartItem,
                            onIncreaseQuantity = {
                                cart.addItem(cartItem.menuItem)
                            },
                            onDecreaseQuantity = {
                                cart.removeItem(cartItem.menuItem)
                            }
                        )
                    }
                }
                
                // Total and checkout button
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
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
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Subtotal",
                                style = MaterialTheme.typography.titleLarge
                            )
                            
                            Text(
                                text = String.format("%.1f€", totalPrice),
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Button(
                            onClick = {
                                // Handle checkout
                                // For now, just show a fake success message
                                cart.clear()
                                navController.popBackStack()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Purple
                            )
                        ) {
                            Text("Check Out")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CartItemCard(
    cartItem: CartItem,
    onIncreaseQuantity: () -> Unit,
    onDecreaseQuantity: () -> Unit,
    modifier: Modifier = Modifier
) {
    val menuItem = cartItem.menuItem
    val itemPrice = menuItem.price * cartItem.quantity
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Purple
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Item image
            Card(
                modifier = Modifier
                    .size(80.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                // Use a placeholder image for now
                Image(
                    painter = painterResource(
                        id = when (menuItem.id) {
                            "1-1" -> R.drawable.ic_launcher_foreground
                            "1-2" -> R.drawable.ic_launcher_foreground
                            "1-3" -> R.drawable.ic_launcher_foreground
                            else -> R.drawable.ic_launcher_foreground
                        }
                    ),
                    contentDescription = menuItem.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            
            // Item details
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = menuItem.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
                
                Text(
                    text = String.format("%.1f€", itemPrice),
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )
            }
            
            // Quantity controls
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.padding(4.dp)
            ) {
                IconButton(
                    onClick = onDecreaseQuantity,
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            color = Color.White,
                            shape = RoundedCornerShape(4.dp)
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Decrease quantity",
                        tint = Purple
                    )
                }
                
                Text(
                    text = "${cartItem.quantity}",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                
                IconButton(
                    onClick = onIncreaseQuantity,
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            color = Color.White,
                            shape = RoundedCornerShape(4.dp)
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Increase quantity",
                        tint = Purple
                    )
                }
            }
        }
    }
} 