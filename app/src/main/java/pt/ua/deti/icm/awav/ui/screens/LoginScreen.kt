package pt.ua.deti.icm.awav.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import pt.ua.deti.icm.awav.R
import pt.ua.deti.icm.awav.data.model.UserRole
import pt.ua.deti.icm.awav.ui.theme.AWAVStyles
import pt.ua.deti.icm.awav.ui.theme.Purple
import android.util.Log
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import pt.ua.deti.icm.awav.data.AuthRepository
import pt.ua.deti.icm.awav.ui.navigation.Screen
import pt.ua.deti.icm.awav.ui.screens.auth.AuthViewModel
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext

@Composable
fun LoginScreen(
    onLoginSuccess: (UserRole) -> Unit,
    navController: NavController? = null,
    viewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory)
) {
    val email by viewModel.email.collectAsState()
    val password by viewModel.password.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val selectedRole by viewModel.selectedRole.collectAsState()
    val userRoles by viewModel.userRoles.collectAsState()
    
    var showRoleDialog by remember { mutableStateOf(false) }
    var showSignOutDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    
    // Firebase auth instance
    val auth = remember { Firebase.auth }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "App Logo",
                modifier = Modifier.size(120.dp)
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            Text(
                text = "Welcome to AWAV",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            OutlinedTextField(
                value = email,
                onValueChange = { viewModel.updateEmail(it) },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = password,
                onValueChange = { viewModel.updatePassword(it) },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Error message
            errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = { 
                    errorMessage = null
                    
                    // First authenticate with Firebase
                    viewModel.signIn { success ->
                        if (success) {
                            Log.d("LoginScreen", "signInWithEmail:success")
                            // If user has only one role, log them in directly
                            if (userRoles.size == 1) {
                                viewModel.updateSelectedRole(userRoles.first())
                                Toast.makeText(
                                    context,
                                    "Successfully logged in as ${userRoles.first().name.lowercase()}!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                onLoginSuccess(userRoles.first())
                            } else if (userRoles.isNotEmpty()) {
                                // Show role selection dialog if user has multiple roles
                                showRoleDialog = true
                            } else {
                                errorMessage = "No roles found for this account. Please register."
                            }
                        } else {
                            errorMessage = "Authentication failed. Please check your credentials."
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(AWAVStyles.buttonHeight),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Purple
                ),
                enabled = !loading && email.isNotEmpty() && password.isNotEmpty()
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Login")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            TextButton(onClick = { 
                // Check if user is already signed in
                if (auth.currentUser != null) {
                    // Show confirmation dialog
                    showSignOutDialog = true
                } else {
                    // Navigate directly to register screen if no user signed in
                    navController?.navigate(Screen.Register.route)
                }
            }) {
                Text("Don't have an account? Sign up")
            }
        }
    }
    
    // Sign out confirmation dialog
    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            title = { Text("Sign Out Required") },
            text = { Text("To register a new account, you need to sign out from your current account first. Do you want to continue?") },
            confirmButton = {
                Button(
                    onClick = {
                        showSignOutDialog = false
                        // Sign out and navigate to register
                        viewModel.signOut()
                        navController?.navigate(Screen.Register.route)
                    }
                ) {
                    Text("Sign Out & Continue")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Role selection dialog - only shows available roles
    if (showRoleDialog) {
        AlertDialog(
            onDismissRequest = { showRoleDialog = false },
            title = { Text("Select Your Role") },
            text = { 
                Column {
                    // Only show roles the user has registered for
                    userRoles.forEach { role ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            RadioButton(
                                selected = selectedRole == role,
                                onClick = { viewModel.updateSelectedRole(role) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                when (role) {
                                    UserRole.PARTICIPANT -> "Event Participant"
                                    UserRole.STAND_WORKER -> "Stand Worker" 
                                    UserRole.ORGANIZER -> "Event Organizer"
                                }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (selectedRole != null) {
                            showRoleDialog = false
                            // Show success message
                            val currentRole = selectedRole // Local copy for safe use
                            if (currentRole != null) {
                                Toast.makeText(
                                    context,
                                    "Successfully logged in as ${currentRole.name.lowercase()}!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                // Call the callback with selected role
                                onLoginSuccess(currentRole)
                            }
                        }
                    },
                    enabled = selectedRole != null
                ) {
                    Text("Continue")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRoleDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

