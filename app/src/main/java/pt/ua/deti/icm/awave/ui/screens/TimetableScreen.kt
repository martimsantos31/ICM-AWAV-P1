package pt.ua.deti.icm.awave.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TimetableScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // TODO make this dynamic
        Text(
            text = "Day 1",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        EventCard(
            title = "Gate Openings",
            time = "14:00",
            location = "All Gates"
        )
        
        EventCard(
            title = "'Kid G' Dj Set",
            time = "14:30",
            location = "Second Stage"
        )
        
    }
}

@Composable
private fun EventCard(
    title: String,
    time: String,
    location: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = time,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = location,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
} 