package pt.ua.deti.icm.awav.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import pt.ua.deti.icm.awav.R
import pt.ua.deti.icm.awav.data.model.FeedPost
import pt.ua.deti.icm.awav.data.model.FeedPostType
import pt.ua.deti.icm.awav.ui.components.feed.CreatePostCard
import pt.ua.deti.icm.awav.ui.components.feed.FeedPostItem
import pt.ua.deti.icm.awav.ui.theme.AWAVStyles
import pt.ua.deti.icm.awav.ui.theme.Purple
import java.util.*
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(navController: NavController) {
    var searchQuery by remember { mutableStateOf("") }
    var showCreatePostDialog by remember { mutableStateOf(false) }
    
    // Sample feed data
    val feedPosts = remember {
        mutableStateListOf(
            FeedPost(
                id = "1",
                authorId = "user1",
                authorName = "Event Organizer",
                authorAvatarResId = R.drawable.user1,
                content = "Welcome to the event! We're excited to have you all here. Don't forget to check out the schedule for today's activities.",
                type = FeedPostType.ANNOUNCEMENT,
                timestamp = Date(System.currentTimeMillis() - 3600000), // 1 hour ago
                likes = 15,
                comments = 3
            ),
            FeedPost(
                id = "2",
                authorId = "user2",
                authorName = "Jane Doe",
                authorAvatarResId = R.drawable.user3,
                content = "The main stage performance starts in 30 minutes! Don't miss it!",
                type = FeedPostType.TEXT,
                timestamp = Date(System.currentTimeMillis() - 1800000), // 30 minutes ago
                likes = 8,
                comments = 1
            ),
            FeedPost(
                id = "3",
                authorId = "user3",
                authorName = "John Smith",
                authorAvatarResId = R.drawable.user2,
                content = "Check out this amazing view from the event!",
                imageUrl = "event_image.jpg", // This would be a URL in a real app
                type = FeedPostType.IMAGE,
                timestamp = Date(System.currentTimeMillis() - 900000), // 15 minutes ago
                likes = 24,
                comments = 5
            ),
            FeedPost(
                id = "4",
                authorId = "user1",
                authorName = "Event Organizer",
                authorAvatarResId = R.drawable.user1,
                content = "IMPORTANT: Due to the weather forecast, the outdoor activities scheduled for tomorrow will be moved to the main hall.",
                type = FeedPostType.EVENT_UPDATE,
                timestamp = Date(System.currentTimeMillis() - 600000), // 10 minutes ago
                likes = 7,
                comments = 12
            ),
            FeedPost(
                id = "5",
                authorId = "user4",
                authorName = "Chris Bumstead",
                authorAvatarResId = R.drawable.user1,
                content = "The food stand near the entrance has amazing snacks! Highly recommend trying their local specialties.",
                type = FeedPostType.TEXT,
                timestamp = Date(System.currentTimeMillis() - 300000), // 5 minutes ago
                likes = 3,
                comments = 0
            )
        )
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // App logo
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_awav),
                contentDescription = "AWAV Logo",
                modifier = Modifier.height(32.dp)
            )
        }
        
        // Search bar
        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(AWAVStyles.searchBarHeight)
                .clip(RoundedCornerShape(AWAVStyles.searchBarCornerRadius)),
            placeholder = { Text("Search in feed") },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search"
                )
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.LightGray.copy(alpha = 0.5f),
                unfocusedContainerColor = Color.LightGray.copy(alpha = 0.5f),
                disabledContainerColor = Color.LightGray.copy(alpha = 0.5f),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Feed content
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // Create post card
                CreatePostCard(
                    userAvatarResId = R.drawable.user2, // Current user avatar
                    onCreatePostClick = { showCreatePostDialog = true },
                    onAddImageClick = { showCreatePostDialog = true }
                )
            }
            
            items(feedPosts) { post ->
                FeedPostItem(
                    post = post,
                    onLikeClick = { likedPost ->
                        // Handle like action
                        val index = feedPosts.indexOfFirst { it.id == likedPost.id }
                        if (index >= 0) {
                            val updatedPost = feedPosts[index].copy(
                                isLikedByCurrentUser = !feedPosts[index].isLikedByCurrentUser,
                                likes = if (feedPosts[index].isLikedByCurrentUser) 
                                    feedPosts[index].likes - 1 else feedPosts[index].likes + 1
                            )
                            feedPosts[index] = updatedPost
                        }
                    },
                    onCommentClick = { commentedPost ->
                        // Navigate to comment screen/dialog
                        // For now, we'll just show a snackbar
                    }
                )
            }
        }
    }
    
    // Create post dialog
    if (showCreatePostDialog) {
        var postContent by remember { mutableStateOf("") }
        var isPosting by remember { mutableStateOf(false) }
        
        AlertDialog(
            onDismissRequest = { 
                if (!isPosting) {
                    showCreatePostDialog = false
                    postContent = ""
                }
            },
            title = { Text("Create a Post") },
            text = {
                Column {
                    OutlinedTextField(
                        value = postContent,
                        onValueChange = { postContent = it },
                        placeholder = { Text("What's happening at the event?") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (postContent.isNotBlank()) {
                            isPosting = true
                            
                            // Create new post
                            val newPost = FeedPost(
                                id = UUID.randomUUID().toString(),
                                authorId = "currentUser",
                                authorName = "You", // In a real app, get the current user's name
                                authorAvatarResId = R.drawable.user2, // Current user avatar
                                content = postContent,
                                type = FeedPostType.TEXT,
                                timestamp = Date(),
                                likes = 0,
                                comments = 0
                            )
                            
                            // Add to feed
                            feedPosts.add(0, newPost)
                            
                            // Close dialog
                            showCreatePostDialog = false
                            postContent = ""
                            isPosting = false
                        }
                    },
                    enabled = postContent.isNotBlank() && !isPosting
                ) {
                    Text(if (isPosting) "Posting..." else "Post")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showCreatePostDialog = false
                        postContent = ""
                    },
                    enabled = !isPosting
                ) {
                    Text("Cancel")
                }
            }
        )
    }
} 