package pt.ua.deti.icm.awav.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import pt.ua.deti.icm.awav.R
import pt.ua.deti.icm.awav.data.model.Message
import pt.ua.deti.icm.awav.data.model.MessageType
import pt.ua.deti.icm.awav.ui.components.messages.ChatMessage
import pt.ua.deti.icm.awav.ui.theme.Purple

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveChatScreen(navController: NavController) {
    val messages = remember {
        mutableStateListOf(
            Message(
                id = "1",
                senderId = "user1",
                senderName = "Chris Bumstead",
                content = "The main stage is going crazy",
                type = MessageType.TEXT
            ),
            Message(
                id = "2",
                senderId = "currentUser",
                senderName = "Current User",
                content = "The main stage is going crazy",
                type = MessageType.TEXT
            ),
            Message(
                id = "3",
                senderId = "user2",
                senderName = "Cristiano Ronaldo",
                content = "SIUUUUUUUUUUUUUUUUU",
                type = MessageType.TEXT
            ),
            Message(
                id = "4",
                senderId = "user3",
                senderName = "Jane Doe",
                content = "Quem você pensa que é",
                type = MessageType.TEXT
            ),
            Message(
                id = "5",
                senderId = "currentUser",
                senderName = "Current User",
                content = "Chris Bumstead",
                type = MessageType.TEXT
            ),
            Message(
                id = "6",
                senderId = "currentUser",
                senderName = "Current User",
                content = "O CBUM",
                type = MessageType.TEXT
            ),
            Message(
                id = "7",
                senderId = "currentUser",
                senderName = "Current User",
                mediaUrl = "concert_image.jpg",
                type = MessageType.IMAGE
            ),
            Message(
                id = "8",
                senderId = "currentUser",
                senderName = "Current User",
                content = "Where you @t",
                type = MessageType.TEXT
            ),
            Message(
                id = "9",
                senderId = "user1",
                senderName = "Jane Doe",
                location = null,
                type = MessageType.LOCATION
            )
        )
    }

    var messageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // State for the popup menu
    var showOptionsMenu by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Bar
        TopAppBar(
            title = {
                Text(
                    text = "Live Chat",
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background
            )
        )

        // Messages
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(messages) { message ->
                val isFromCurrentUser = message.senderId == "currentUser"
                val avatarResId = when (message.senderId) {
                    "user1" -> R.drawable.user2
                    "user2" -> R.drawable.user1
                    "user3" -> R.drawable.user2
                    "currentUser" -> R.drawable.user2
                    else -> R.drawable.user1
                }

                ChatMessage(
                    message = message,
                    isFromCurrentUser = isFromCurrentUser,
                    avatarResId = avatarResId
                )
            }
        }

        // Message Input
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box {
                // Plus button
                IconButton(
                    onClick = { showOptionsMenu = !showOptionsMenu },
                    modifier = Modifier
                        .size(40.dp)
                        .background(Purple, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "More options",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }

                // Popup menu for options
                if (showOptionsMenu) {
                    Box(
                        modifier = Modifier
                            .width(200.dp)
                            .padding(top = 48.dp) // Position below the button
                    ) {
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp)
                            ) {
                                // Location option
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            // Handle location
                                            showOptionsMenu = false
                                        }
                                        .padding(vertical = 12.dp, horizontal = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_location),
                                        contentDescription = "Location",
                                        tint = Purple,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Text(
                                        text = "Location",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }

                                Divider()

                                // Photo option
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            // Handle photo
                                            showOptionsMenu = false
                                        }
                                        .padding(vertical = 12.dp, horizontal = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_photo),
                                        contentDescription = "Photo",
                                        tint = Purple,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Text(
                                        text = "Photo",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            TextField(
                value = messageText,
                onValueChange = { messageText = it },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                placeholder = { Text("Type a message") },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.LightGray.copy(alpha = 0.5f),
                    unfocusedContainerColor = Color.LightGray.copy(alpha = 0.5f),
                    disabledContainerColor = Color.LightGray.copy(alpha = 0.5f),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(24.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = {
                    if (messageText.isNotBlank()) {
                        val newMessage = Message(
                            id = (messages.size + 1).toString(),
                            senderId = "currentUser",
                            senderName = "Current User",
                            content = messageText,
                            type = MessageType.TEXT
                        )
                        messages.add(newMessage)
                        messageText = ""
                        coroutineScope.launch {
                            listState.animateScrollToItem(messages.size - 1)
                        }
                    }
                },
                modifier = Modifier
                    .size(40.dp)
                    .background(Purple, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send message",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }

    // Click outside to dismiss the popup
    if (showOptionsMenu) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    showOptionsMenu = false
                }
        )
    }
}