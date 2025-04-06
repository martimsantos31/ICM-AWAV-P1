package pt.ua.deti.icm.awav.ui.screens

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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import pt.ua.deti.icm.awav.R
import pt.ua.deti.icm.awav.ui.theme.Purple

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController? = null) {
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
                .background(MaterialTheme.colorScheme.surface)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "Profile picture",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        
        // User Name
        Text(
            text = "Tiago Pedrosa",
            modifier = Modifier.padding(top = 16.dp),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        // User Role
        Text(
            text = "Participant",
            modifier = Modifier.padding(top = 4.dp, bottom = 24.dp),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )
        
        // Menu Options
        ProfileMenuItem(
            title = "Edit Profile",
            icon = Icons.Default.Edit,
            onClick = { /* TODO: Navigate to edit profile screen */ }
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