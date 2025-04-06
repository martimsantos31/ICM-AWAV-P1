package pt.ua.deti.icm.awav.ui.screens.stand

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowForward
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import pt.ua.deti.icm.awav.R
import pt.ua.deti.icm.awav.data.repository.StandRepository
import pt.ua.deti.icm.awav.ui.navigation.Screen
import pt.ua.deti.icm.awav.ui.navigation.createRoute
import pt.ua.deti.icm.awav.ui.theme.Purple

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StandDetailsScreen(
    standId: String,
    navController: NavController
) {
    val stand = remember { StandRepository.getStandById(standId) }
    val waitTime = remember { StandRepository.getWaitTime(standId) }
    
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
                    Text(
                        text = stand.name,
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
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
            // Banner Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                // In a real app, you would load the image from a URL
                // For now, we'll use R.drawable.chorizo
                Image(
                    painter = painterResource(id = R.drawable.chorizo),
                    contentDescription = stand.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Menu Button
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp),
                onClick = {
                    navController.navigate(
                        Screen.StandMenu.createRoute("standId" to standId)
                    )
                },
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
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = "Menu",
                        tint = Color.White
                    )
                    
                    Text(
                        text = "Menu",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp)
                    )
                    
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Go to Menu",
                        tint = Color.White
                    )
                }
            }
            
            // Wait Time
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp),
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
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = "Wait Time",
                        tint = Color.White
                    )
                    
                    Text(
                        text = "Wait Time",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp)
                    )
                    
                    Text(
                        text = "$waitTime min",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White
                    )
                }
            }
            
            // Order Button
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp),
                onClick = {
                    navController.navigate(
                        Screen.StandOrder.createRoute("standId" to standId)
                    )
                },
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
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = "Order",
                        tint = Color.White
                    )
                    
                    Text(
                        text = "Order",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp)
                    )
                    
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Go to Order",
                        tint = Color.White
                    )
                }
            }
        }
    }
} 