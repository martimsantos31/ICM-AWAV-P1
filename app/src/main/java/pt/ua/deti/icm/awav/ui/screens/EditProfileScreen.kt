package pt.ua.deti.icm.awav.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import pt.ua.deti.icm.awav.R
import pt.ua.deti.icm.awav.ui.screens.auth.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory)
) {
    val context = LocalContext.current
    val currentUser by authViewModel.currentUser.collectAsState()
    val loading by authViewModel.loading.collectAsState()
    val selectedRole by authViewModel.userRoles.collectAsState()
    
    // Initialize edit fields with current user data
    LaunchedEffect(Unit) {
        authViewModel.initializeEditFields()
    }
    
    // Local state for UI
    val displayName by authViewModel.displayName.collectAsState()
    var showSaveConfirmation by remember { mutableStateOf(false) }
    
    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { authViewModel.updateProfilePicUri(it) }
    }
    
    val profilePicUri by authViewModel.profilePicUri.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    } else {
                        IconButton(
                            onClick = { showSaveConfirmation = true }
                        ) {
                            Icon(Icons.Default.Save, contentDescription = "Save")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Image
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    .clickable { imagePickerLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (profilePicUri != null) {
                    // Selected new image
                    Image(
                        painter = rememberAsyncImagePainter(profilePicUri),
                        contentDescription = "Profile picture",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else if (currentUser?.photoUrl != null) {
                    // Existing image
                    Image(
                        painter = rememberAsyncImagePainter(
                            ImageRequest.Builder(context)
                                .data(currentUser?.photoUrl.toString() + "?t=${System.currentTimeMillis()}")
                                .crossfade(true)
                                .diskCachePolicy(coil.request.CachePolicy.DISABLED)
                                .memoryCachePolicy(coil.request.CachePolicy.DISABLED)
                                .placeholder(R.drawable.ic_account_circle)
                                .error(R.drawable.ic_account_circle)
                                .build()
                        ),
                        contentDescription = "Profile picture",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Default icon
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Default profile picture",
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Overlay camera icon to indicate editable
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .align(Alignment.BottomEnd)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoCamera,
                        contentDescription = "Change Photo",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Display Name Field
            OutlinedTextField(
                value = displayName,
                onValueChange = { authViewModel.updateDisplayName(it) },
                label = { Text("Display Name") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                singleLine = true,
                leadingIcon = {
                    Icon(Icons.Default.Person, contentDescription = "Name")
                }
            )
            
            // Email field (non-editable)
            OutlinedTextField(
                value = currentUser?.email ?: "",
                onValueChange = { },
                label = { Text("Email") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                enabled = false,
                leadingIcon = {
                    Icon(Icons.Default.Email, contentDescription = "Email")
                }
            )
            
            // Role field (non-editable)
            val roleText = if (selectedRole.isNotEmpty()) {
                selectedRole.first().name.lowercase().replaceFirstChar { it.uppercase() }
            } else {
                "User"
            }
            
            OutlinedTextField(
                value = roleText,
                onValueChange = { },
                label = { Text("Role") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                enabled = false,
                leadingIcon = {
                    Icon(Icons.Default.Badge, contentDescription = "Role")
                }
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            Button(
                onClick = { showSaveConfirmation = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.typography.titleMedium.color
                    )
                } else {
                    Text("Save Changes", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
        
        // Confirmation Dialog
        if (showSaveConfirmation) {
            AlertDialog(
                onDismissRequest = { showSaveConfirmation = false },
                title = { Text("Save Changes?") },
                text = { Text("Are you sure you want to update your profile information?") },
                confirmButton = {
                    Button(
                        onClick = {
                            showSaveConfirmation = false
                            
                            // Update profile with ViewModel state
                            authViewModel.updateUserProfile(
                                displayName = displayName,
                                profilePicUri = profilePicUri
                            ) { success ->
                                if (success) {
                                    // Force reload user data
                                    authViewModel.checkAuthState()
                                    
                                    Toast.makeText(
                                        context,
                                        "Profile updated successfully",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    navController.navigateUp()
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Failed to update profile",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    ) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showSaveConfirmation = false }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
} 