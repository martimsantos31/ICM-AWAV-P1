package pt.ua.deti.icm.awav.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import pt.ua.deti.icm.awav.ui.theme.AWAVStyles
import pt.ua.deti.icm.awav.ui.theme.Purple
import pt.ua.deti.icm.awav.ui.theme.White

@Composable
fun EventCard(
    modifier: Modifier = Modifier,
    title: String,
    location: String,
    time: String,
    isPurple: Boolean = true
) {
    Card(
        modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .height(AWAVStyles.eventCardHeight),
        shape = RoundedCornerShape(AWAVStyles.eventCardCornerRadius),
        colors = CardDefaults.cardColors(
            containerColor = if (isPurple) Purple else MaterialTheme.colorScheme.onSecondary
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isPurple) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = location,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isPurple) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f) else MaterialTheme.colorScheme.tertiary
                )
            }
            Text(
                text = time,
                style = MaterialTheme.typography.titleMedium,
                color = if (isPurple) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.End
            )
        }
    }
}