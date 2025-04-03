package pt.ua.deti.icm.awav.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import pt.ua.deti.icm.awav.R
import pt.ua.deti.icm.awav.ui.components.ChatMessage

@Composable
fun ChatScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Live Chat",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        Button(
            onClick = { /* TODO */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_stream),
                contentDescription = "Chat Icon",
                modifier = Modifier.padding(end = 8.dp)
            )
            Text("Live Chat")
            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_right),
                contentDescription = "Chat Icon",
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        
        Text(
            text = "Recent Chats",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(vertical = 16.dp)
        )
        Modifier.padding(bottom = 8.dp).ChatMessage(
            message = "Hello, how are you?",
            isFromCurrentUser = false,
            avatarResId = R.drawable.user1
        )
        Modifier.padding(bottom = 8.dp).ChatMessage(
            message = "I'm good, thanks! How about you?",
            isFromCurrentUser = true,
            avatarResId = R.drawable.user2
        )
        
    }
} 