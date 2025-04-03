package pt.ua.deti.icm.awav.ui.screens

import pt.ua.deti.icm.awav.R
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pt.ua.deti.icm.awav.ui.components.StandCard

@Composable
fun StandsScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        StandCard(
            title = "Daniel's Chorizo",
            imageResId = R.drawable.chorizo,
            onDetailsClick = { /* TODO: Handle click */ }
        )
        StandCard(
            title = "Mohamed's Kebab",
            imageResId = R.drawable.kebab,
            onDetailsClick = { /* TODO: Handle click */ },
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // TODO: Add more stands
    }
}
