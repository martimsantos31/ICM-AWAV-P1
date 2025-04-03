package pt.ua.deti.icm.awav.ui.screens

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
import pt.ua.deti.icm.awav.ui.components.EventCard

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
            location = "All Gates",
            isPurple = false
        )
        
        EventCard(
            title = "'Kid G' Dj Set",
            time = "14:30",
            location = "Second Stage"
        )
        
    }
}
