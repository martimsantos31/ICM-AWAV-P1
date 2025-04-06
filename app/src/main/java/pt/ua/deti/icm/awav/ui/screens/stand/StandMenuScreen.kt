package pt.ua.deti.icm.awav.ui.screens.stand

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import pt.ua.deti.icm.awav.R
import pt.ua.deti.icm.awav.data.model.MenuItem
import pt.ua.deti.icm.awav.data.repository.StandRepository
import pt.ua.deti.icm.awav.ui.navigation.Screen
import pt.ua.deti.icm.awav.ui.navigation.createRoute
import pt.ua.deti.icm.awav.ui.theme.Purple

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StandMenuScreen(
    standId: String,
    navController: NavController
) {
    val stand = remember { StandRepository.getStandById(standId) }
    val menuItems = remember { StandRepository.getMenuItems(standId) }
    
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
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = null,
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Menu",
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
                actions = {
                    IconButton(
                        onClick = {
                            navController.navigate(
                                Screen.StandCart.createRoute("standId" to standId)
                            )
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = "Cart",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Purple,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Banner Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                // In a real app, you would load the image from a URL
                Image(
                    painter = painterResource(id = R.drawable.chorizo),
                    contentDescription = stand.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            
            // Menu Items
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                items(menuItems) { menuItem ->
                    MenuItemCard(
                        menuItem = menuItem,
                        onBuyClick = {
                            // Add to cart and navigate to cart
                            val cart = StandRepository.getCart(standId)
                            cart.addItem(menuItem)
                            
                            // Navigate to cart screen
                            navController.navigate(
                                Screen.StandCart.createRoute("standId" to standId)
                            )
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun MenuItemCard(
    menuItem: MenuItem,
    onBuyClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
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
                // In a real app, you would load the image from a URL
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
                
                if (menuItem.description.isNotEmpty()) {
                    Text(
                        text = menuItem.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.7f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                Text(
                    text = String.format("%.1f€", menuItem.price),
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )
            }
            
            // Buy button or Out of Stock label
            if (menuItem.isAvailable) {
                Button(
                    onClick = onBuyClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Purple
                    )
                ) {
                    Text("Buy Now")
                }
            } else {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color.LightGray
                    )
                ) {
                    Text(
                        text = "Out of Stock",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        color = Color.DarkGray
                    )
                }
            }
        }
    }
} 