package pt.ua.deti.icm.awav.ui.screens

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
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.google.firebase.auth.FirebaseUser
import pt.ua.deti.icm.awav.data.model.UserRole

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController? = null,
    authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory)
) {
    val context = LocalContext.current
    val currentUser by authViewModel.currentUser.collectAsState()
    val userRoles by authViewModel.userRoles.collectAsState()
    val activeRole by authViewModel.activeRole.collectAsState()
    
    // Add a state to track when profile screen is recomposed
    val refreshTrigger = remember { mutableStateOf(0) }

    // Refresh user state when ProfileScreen is mounted
    LaunchedEffect(true) {
        Log.d("ProfileScreen", "LaunchedEffect triggered, refreshing user state")
        authViewModel.refreshUserState()
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
            if (currentUser?.photoUrl != null) {
                // User has a profile picture
                Image(
                    painter = rememberAsyncImagePainter(
                        ImageRequest.Builder(context)
                            .data(currentUser?.photoUrl.toString() + "?v=${refreshTrigger.value}")
                            .crossfade(true)
                            .diskCachePolicy(coil.request.CachePolicy.DISABLED)  // Disable disk cache
                            .memoryCachePolicy(coil.request.CachePolicy.DISABLED)  // Disable memory cache  
                            .build()
                    ),
                    contentDescription = "Profile picture",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
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
            onClick = { /* TODO: Navigate to tickets screen */ }
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