package pt.ua.deti.icm.awav.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import pt.ua.deti.icm.awav.R
import pt.ua.deti.icm.awav.ui.theme.Purple
import androidx.lifecycle.viewmodel.compose.viewModel
import pt.ua.deti.icm.awav.ui.screens.auth.AuthViewModel
import pt.ua.deti.icm.awav.ui.viewmodels.TicketViewModel
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.google.firebase.auth.FirebaseUser
import pt.ua.deti.icm.awav.data.model.UserRole
import coil.compose.AsyncImagePainter
import pt.ua.deti.icm.awav.utils.FCMUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController? = null,
    authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory),
    ticketViewModel: TicketViewModel = viewModel(factory = TicketViewModel.Factory)
) {
    val context = LocalContext.current
    val currentUser by authViewModel.currentUser.collectAsState()
    val userRoles by authViewModel.userRoles.collectAsState()
    val activeRole by authViewModel.activeRole.collectAsState()
    
    // Add a state to track when profile screen is recomposed
    val refreshTrigger = remember { mutableStateOf(0) }

    // Add this inside the ProfileScreen composable, before the Scaffold
    var showFcmTokenDialog by remember { mutableStateOf(false) }
    var fcmToken by remember { mutableStateOf<String?>(null) }
    var requestFCMToken by remember { mutableStateOf(false) }
    
    // Handle FCM token requests
    LaunchedEffect(requestFCMToken) {
        if (requestFCMToken) {
            FCMUtils.getFCMToken { token ->
                fcmToken = token
                showFcmTokenDialog = true
                requestFCMToken = false
            }
        }
    }
    
    // Helper function to copy text to clipboard (called within composable context)
    val copyToClipboard = { text: String ->
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("FCM Token", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Token copied to clipboard", Toast.LENGTH_SHORT).show()
    }
    
    if (showFcmTokenDialog) {
        AlertDialog(
            onDismissRequest = { showFcmTokenDialog = false },
            title = { Text("FCM Token") },
            text = {
                Column {
                    if (fcmToken == null) {
                        CircularProgressIndicator()
                    } else {
                        Text(
                            text = fcmToken ?: "Failed to get token",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        fcmToken?.let { copyToClipboard(it) }
                        showFcmTokenDialog = false
                    }
                ) {
                    Text("Copy & Close")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showFcmTokenDialog = false }
                ) {
                    Text("Close")
                }
            }
        )
    }
    
    // Refresh user state and tickets when ProfileScreen is mounted
    LaunchedEffect(true) {
        Log.d("ProfileScreen", "LaunchedEffect triggered, refreshing user state and tickets")
        
        // Force multiple refreshes to make sure we get the latest data
        authViewModel.refreshUserState()
        
        // Force refresh ticket status to update navbar
        ticketViewModel.refreshTicketStatus(forceFirebaseCheck = true)
        
        // Force refresh the profile image
        refreshTrigger.value = System.currentTimeMillis().toInt()
        
        // Add a slight delay and refresh again to make sure Firestore data is loaded
        kotlinx.coroutines.delay(500)
        authViewModel.refreshUserState()
        Log.d("ProfileScreen", "Second refresh triggered after delay")
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // App Logo
        Text(
            text = "AWAV.",
            modifier = Modifier.padding(top = 24.dp, bottom = 16.dp),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Black
        )
        
        // Profile Image
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            val photoUrl = currentUser?.photoUrl?.toString()
            val userDoc by authViewModel.userDoc.collectAsState()
            val firestorePhotoUrl = userDoc?.getString("photoUrl")
            
            // Debug logs to see what URLs we have
            Log.d("ProfileScreen", "Firebase Auth photoUrl: $photoUrl")
            Log.d("ProfileScreen", "Firestore photoUrl: $firestorePhotoUrl")
            
            // Use Firestore photoUrl if available, otherwise fall back to Firebase Auth photoUrl
            val effectivePhotoUrl = firestorePhotoUrl ?: photoUrl
            Log.d("ProfileScreen", "Using effectivePhotoUrl: $effectivePhotoUrl")
            
            if (!effectivePhotoUrl.isNullOrEmpty()) {
                // User has a profile picture - with aggressive cache busting
                val cacheBustUrl = "$effectivePhotoUrl?v=${System.currentTimeMillis()}"
                Log.d("ProfileScreen", "Loading image with URL: $cacheBustUrl")
                
                // Try to create a direct Firebase Storage URL if it's a Firebase Storage reference
                val directFirebaseUrl = if (effectivePhotoUrl.contains("firebasestorage.googleapis.com")) {
                    effectivePhotoUrl
                } else if (currentUser?.uid != null) {
                    // As a fallback, try to construct a direct URL to user's profile image
                    "https://firebasestorage.googleapis.com/v0/b/icm-awav.appspot.com/o/profile_images%2F${currentUser?.uid}.jpg?alt=media&token=1"
                } else {
                    null
                }
                
                Log.d("ProfileScreen", "Trying direct Firebase URL as fallback: $directFirebaseUrl")
                
                // Try loading both URLs in different composables
                Box(modifier = Modifier.fillMaxSize()) {
                    // First try with the effective URL
                    val painter = rememberAsyncImagePainter(
                        ImageRequest.Builder(context)
                            .data(cacheBustUrl)
                            .crossfade(true)
                            .diskCachePolicy(coil.request.CachePolicy.DISABLED)
                            .memoryCachePolicy(coil.request.CachePolicy.DISABLED)
                            .build()
                    )
                    
                    // If the first image is loading or error, try the direct URL
                    if (painter.state is AsyncImagePainter.State.Loading || 
                        painter.state is AsyncImagePainter.State.Error) {
                        
                        if (directFirebaseUrl != null) {
                            Image(
                                painter = rememberAsyncImagePainter(
                                    ImageRequest.Builder(context)
                                        .data(directFirebaseUrl)
                                        .crossfade(true)
                                        .diskCachePolicy(coil.request.CachePolicy.DISABLED)
                                        .memoryCachePolicy(coil.request.CachePolicy.DISABLED)
                                        .build()
                                ),
                                contentDescription = "Profile picture (direct)",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    } else {
                        // Show the first image if it loaded successfully
                        Image(
                            painter = painter,
                            contentDescription = "Profile picture",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            } else {
                Log.d("ProfileScreen", "No profile image URL available, showing default icon")
                // Default profile icon
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Default profile picture",
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // User Name
        Text(
            text = currentUser?.displayName ?: "User",
            modifier = Modifier.padding(top = 16.dp),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        // User Email & Role with optional switch button
        Text(
            text = currentUser?.email ?: "",
            modifier = Modifier.padding(top = 4.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
        )
        
        // Display only the active role without switching option
        Text(
            text = activeRole?.name?.lowercase()?.replaceFirstChar { it.uppercase() } ?: 
                   userRoles.firstOrNull()?.name?.lowercase()?.replaceFirstChar { it.uppercase() } ?: 
                   "Guest",
            modifier = Modifier.padding(top = 4.dp, bottom = 24.dp),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )
        
        // Menu Options
        ProfileMenuItem(
            title = "Edit Profile",
            icon = Icons.Default.Edit,
            onClick = { 
                navController?.navigate("edit_profile")
            }
        )
        
        ProfileMenuItem(
            title = "My Tickets",
            icon = Icons.Default.ConfirmationNumber,
            onClick = { navController?.navigate("my_tickets") }
        )
        
        ProfileMenuItem(
            title = "My Spents",
            icon = Icons.Default.Euro,
            onClick = { /* TODO: Navigate to spending history screen */ }
        )
        
        ProfileMenuItem(
            title = "Settings",
            icon = Icons.Default.Settings,
            onClick = { /* TODO: Navigate to settings screen */ }
        )
        
        // Add FCM Token menu item
        ProfileMenuItem(
            title = "Show FCM Token",
            icon = Icons.Default.Notifications,
            onClick = { 
                requestFCMToken = true
            }
        )
        
        ProfileMenuItem(
            title = "Logout",
            icon = Icons.Default.Logout,
            onClick = { 
                // Sign out user
                authViewModel.signOut()
                
                // Navigate back to login screen
                navController?.navigate("login") {
                    popUpTo(0) { inclusive = true }
                }
            }
        )
    }
}

@Composable
fun ProfileMenuItem(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = Purple
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(24.dp)
            )
            
            // Title
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
            )
            
            // Arrow icon
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "Navigate",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
} 