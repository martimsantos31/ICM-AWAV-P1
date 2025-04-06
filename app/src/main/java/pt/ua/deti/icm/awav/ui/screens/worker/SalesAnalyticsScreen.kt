package pt.ua.deti.icm.awav.ui.screens.worker

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.ua.deti.icm.awav.ui.theme.Purple

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesAnalyticsScreen() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Analytics",
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // First row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Clients card
                AnalyticsCard(
                    title = "142",
                    subtitle = "Clients",
                    icon = Icons.Filled.People,
                    modifier = Modifier.weight(1f)
                )
                
                // Money card
                AnalyticsCard(
                    title = "3000€",
                    subtitle = "Money",
                    icon = Icons.Filled.Euro,
                    modifier = Modifier.weight(1f)
                )
            }
            
            // Second row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Most Popular Item
                AnalyticsCard(
                    title = "Chorizo",
                    subtitle = "Most Popular",
                    icon = Icons.Filled.Star,
                    modifier = Modifier.weight(1f)
                )
                
                // Average Order Value
                AnalyticsCard(
                    title = "12.50€",
                    subtitle = "Avg Order",
                    icon = Icons.Filled.ShoppingCart,
                    modifier = Modifier.weight(1f)
                )
            }
            
            // Third row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Peak Hour
                AnalyticsCard(
                    title = "13-14h",
                    subtitle = "Peak Time",
                    icon = Icons.Filled.Schedule,
                    modifier = Modifier.weight(1f)
                )
                
                // Items Per Order
                AnalyticsCard(
                    title = "2.3",
                    subtitle = "Items/Order",
                    icon = Icons.Filled.List,
                    modifier = Modifier.weight(1f)
                )
            }
            
            // Fourth row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Processing Time
                AnalyticsCard(
                    title = "4.5 min",
                    subtitle = "Avg Wait",
                    icon = Icons.Filled.Timer,
                    modifier = Modifier.weight(1f)
                )
                
                // "..." placeholder
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "...",
                        style = MaterialTheme.typography.titleLarge,
                        fontSize = 24.sp
                    )
                }
            }
        }
    }
}

@Composable
fun AnalyticsCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.aspectRatio(1f),
        colors = CardDefaults.cardColors(
            containerColor = Purple
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            // Icon
            Icon(
                imageVector = icon,
                contentDescription = subtitle,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .size(36.dp)
                    .padding(bottom = 8.dp)
            )
            
            // Title
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            
            // Subtitle
            if (subtitle.isNotEmpty()) {
                Text(
                    text = subtitle,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 18.sp
                )
            }
        }
    }
} 