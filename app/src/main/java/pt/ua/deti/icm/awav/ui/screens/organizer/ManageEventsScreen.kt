package pt.ua.deti.icm.awav.ui.screens.organizer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import pt.ua.deti.icm.awav.ui.theme.Purple
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.MusicNote

data class Event(
    val id: String,
    val title: String,
    val description: String,
    val imageUrl: String,
    val startDate: String,
    val endDate: String,
    val icon: ImageVector = Icons.Filled.Event
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageEventsScreen(navController: NavController) {
    // Sample events data
    val events = remember {
        listOf(
            Event(
                id = "1",
                title = "Enterro 25",
                description = "lorem ipsum lorem awqdi jsdoaija aodijajepoj ojajdoajjc ojadoj qojedj",
                imageUrl = "https://via.placeholder.com/150",
                startDate = "26Apr",
                endDate = "3May",
                icon = Icons.Filled.Celebration
            ),
            Event(
                id = "2",
                title = "BE NEI",
                description = "lorem ipsum lorem awqdi jsdoaija aodijajepoj ojajdoajjc ojadoj qojedj",
                imageUrl = "https://via.placeholder.com/150",
                startDate = "4May",
                endDate = "5May",
                icon = Icons.Filled.MusicNote
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Manage Events",
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(events) { event ->
                EventCard(event = event, onEventClick = {
                    // Navigate to event details screen
                    navController.navigate("event_details/${event.id}")
                })
            }
        }
    }
}

@Composable
fun EventCard(
    event: Event,
    onEventClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth(),
        onClick = onEventClick,
        colors = CardDefaults.cardColors(
            containerColor = Purple
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
        ) {
            // Event Icon Container
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(Color.LightGray.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = event.icon,
                    contentDescription = event.title,
                    tint = Color.White,
                    modifier = Modifier.size(64.dp)
                )
            }
            
            // Event Details
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Title
                Text(
                    text = event.title,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                
                // Description
                Text(
                    text = event.description,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 14.sp,
                    maxLines = 3
                )
                
                // Date
                Text(
                    text = "${event.startDate}-${event.endDate}",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 14.sp
                )
            }
        }
    }
} 