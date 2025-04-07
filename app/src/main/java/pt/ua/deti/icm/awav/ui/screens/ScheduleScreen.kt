package pt.ua.deti.icm.awav.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import pt.ua.deti.icm.awav.data.room.AppDatabase
import pt.ua.deti.icm.awav.data.room.entity.ScheduleItem
import pt.ua.deti.icm.awav.data.room.entity.Event
import pt.ua.deti.icm.awav.ui.theme.Purple
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(navController: NavController) {
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)

    // Collect events data as a state with explicit type declaration
    val eventData by db.eventDao().getActiveEvents().collectAsState(initial = emptyList<Event>())

    // Get the currently selected event
    val selectedEvent by remember { derivedStateOf { eventData.firstOrNull() } }
    val dateFormatter = SimpleDateFormat("E, dd MMM", Locale.getDefault())
    val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())

    // Retrieve the schedule items for the selected event
    val scheduleItems = remember(selectedEvent) {
        mutableStateOf<List<ScheduleItem>>(emptyList())
    }

    LaunchedEffect(selectedEvent) {
        if (selectedEvent != null) {
            // Fetch schedule items for the selected event
            scheduleItems.value = db.scheduleItemDao().getScheduleItemsForEvent(selectedEvent!!.id)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Schedule",
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
        if (selectedEvent == null) {
            // No event selected
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Event,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(64.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "No event selected",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Please select an event from the Home page to view schedule",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else if (scheduleItems.value.isEmpty()) {
            // Event has no schedule items
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Event,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(64.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "No schedule available",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "This event doesn't have any schedule items yet",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            // Group schedule items by date
            val groupedSchedule = scheduleItems.value
                .sortedBy { it.startTime }
                .groupBy {
                    val cal = Calendar.getInstance()
                    cal.time = SimpleDateFormat("yyyy-MM-dd HH:mm").parse(it.startTime)!!
                    cal.set(Calendar.HOUR_OF_DAY, 0)
                    cal.set(Calendar.MINUTE, 0)
                    cal.set(Calendar.SECOND, 0)
                    cal.set(Calendar.MILLISECOND, 0)
                    cal.time
                }

            // Display schedule list
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                groupedSchedule.forEach { (date, items) ->
                    item {
                        // Date header
                        Text(
                            text = dateFormatter.format(date),
                            style = MaterialTheme.typography.titleMedium,
                            color = Purple,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    items(items) { item ->
                        ScheduleCard(item = item, timeFormatter = timeFormatter)
                    }
                }
            }
        }
    }
}

@Composable
fun ScheduleCard(
    item: ScheduleItem,
    timeFormatter: SimpleDateFormat
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Time column
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.width(64.dp)
            ) {
                Text(
                    text = item.startTime,
                    style = MaterialTheme.typography.titleSmall,
                    color = Purple
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Vertical line separator
            HorizontalDivider(
                modifier = Modifier
                    .height(40.dp)
                    .width(1.dp),
                color = Purple
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Event details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}