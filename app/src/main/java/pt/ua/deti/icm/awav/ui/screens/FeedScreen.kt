package pt.ua.deti.icm.awav.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import pt.ua.deti.icm.awav.AWAVApplication
import pt.ua.deti.icm.awav.R
import pt.ua.deti.icm.awav.data.model.FeedPost
import pt.ua.deti.icm.awav.data.model.FeedPostType
import pt.ua.deti.icm.awav.ui.components.feed.CreatePostCard
import pt.ua.deti.icm.awav.ui.components.feed.FeedPostItem
import pt.ua.deti.icm.awav.ui.theme.AWAVStyles
import pt.ua.deti.icm.awav.ui.theme.Purple
import java.io.File
import java.util.*
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(navController: NavController) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    
    // Initialize repository from app container
    val feedRepository = remember { AWAVApplication.appContainer.feedRepository }
    
    // State variables
    var searchQuery by remember { mutableStateOf("") }
    var showCreatePostDialog by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    
    // Collect feed posts from repository in real-time
    val postsState = feedRepository.feedPosts.collectAsState()
    val isLoading = feedRepository.isLoading.collectAsState()
    val error = feedRepository.error.collectAsState()
    
    // Show error messages
    LaunchedEffect(error.value) {
        error.value?.let {
            snackbarHostState.showSnackbar("Error: $it")
        }
    }
    
    // Filter posts based on search query
    val filteredPosts = remember(postsState.value, searchQuery) {
        if (searchQuery.isBlank()) {
            postsState.value
        } else {
            postsState.value.filter { post -> 
                post.content.contains(searchQuery, ignoreCase = true) || 
                post.authorName.contains(searchQuery, ignoreCase = true)
            }
        }
    }
    
    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }
    
    // UI Scaffold with snackbar
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
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
                // Create post card - show for all users for now
                item {
                    CreatePostCard(
                        userAvatarResId = R.drawable.user2,
                        onCreatePostClick = { showCreatePostDialog = true },
                        onAddImageClick = { 
                            imagePickerLauncher.launch("image/*")
                            showCreatePostDialog = true 
                        }
                    )
                }
                
                // Show loading indicator if posts are being loaded and list is empty
                if (isLoading.value && filteredPosts.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
                
                // Show filtered posts
                items(filteredPosts) { post ->
                    FeedPostItem(
                        post = post,
                        onLikeClick = { likedPost ->
                            // Handle like action using repository
                            coroutineScope.launch {
                                feedRepository.toggleLike(likedPost.id)
                                    .onFailure { error ->
                                        snackbarHostState.showSnackbar("Error: ${error.message}")
                                    }
                            }
                        },
                        onCommentClick = { commentedPost ->
                            // Show comment dialog
                            coroutineScope.launch {
                                val comment = "Great post!" // In a real app, you'd get this from user input
                                feedRepository.addComment(commentedPost.id, comment)
                                    .onSuccess { 
                                        snackbarHostState.showSnackbar("Comment added successfully")
                                    }
                                    .onFailure { error ->
                                        snackbarHostState.showSnackbar("Error: ${error.message}")
                                    }
                            }
                        }
                    )
                }
                
                // Show empty state if no posts and not loading
                if (filteredPosts.isEmpty() && !isLoading.value) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No posts found. Be the first to post something!")
                        }
                    }
                }
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
                    selectedImageUri = null
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
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Show selected image preview
                    selectedImageUri?.let { uri ->
                        AsyncImage(
                            model = uri,
                            contentDescription = "Selected image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                        
                        TextButton(
                            onClick = { selectedImageUri = null },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Remove Image")
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (postContent.isNotBlank()) {
                            isPosting = true
                            
                            coroutineScope.launch {
                                try {
                                    val result = if (selectedImageUri != null) {
                                        // Create image post
                                        val inputStream = context.contentResolver.openInputStream(selectedImageUri!!)
                                        val tempFile = File.createTempFile("post_image", ".jpg", context.cacheDir)
                                        inputStream?.use { input ->
                                            tempFile.outputStream().use { output ->
                                                input.copyTo(output)
                                            }
                                        }
                                        
                                        feedRepository.createImagePost(postContent, tempFile, FeedPostType.IMAGE)
                                    } else {
                                        // Create text post
                                        feedRepository.createTextPost(postContent, FeedPostType.TEXT)
                                    }
                                    
                                    result.onSuccess {
                                        snackbarHostState.showSnackbar("Post created successfully")
                                        showCreatePostDialog = false
                                        postContent = ""
                                        selectedImageUri = null
                                    }.onFailure { error ->
                                        snackbarHostState.showSnackbar("Error: ${error.message}")
                                    }
                                } catch (e: Exception) {
                                    snackbarHostState.showSnackbar("Error: ${e.message}")
                                } finally {
                                    isPosting = false
                                }
                            }
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
                        selectedImageUri = null
                    },
                    enabled = !isPosting
                ) {
                    Text("Cancel")
                }
            }
        )
    }
} 