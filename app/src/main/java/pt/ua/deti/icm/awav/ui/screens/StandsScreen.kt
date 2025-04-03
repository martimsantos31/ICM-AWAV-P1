package pt.ua.deti.icm.awav.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
fun StandsScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        StandCard(
            title = "Daniel's Chorizo",
            imageUrl = "https://saltofportugal.files.wordpress.com/2012/01/pc3a3o-com-chouric3a7o.jpg?w=1312"
        )
        
        StandCard(
            title = "Mohamed's Kebab",
            imageUrl = "https://www.expomaquinaria.es/wiki/wp-content/uploads/2021/07/doner-kebab-1024x609.jpg"
        )
        
        // TODO: Add more stands
    }
}

@Composable
private fun StandCard(
    title: String,
    imageUrl: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column {
            AsyncImage(
                model = imageUrl,
                contentDescription = title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentScale = ContentScale.Crop
            )
            
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge
                )
                
                TextButton(
                    onClick = { /* TODO */ }
                ) {
                    Text("see details")
                }
            }
        }
    }
} 