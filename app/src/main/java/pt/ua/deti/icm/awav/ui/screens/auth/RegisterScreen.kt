package pt.ua.deti.icm.awav.ui.screens.auth

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.res.painterResource
import pt.ua.deti.icm.awav.R
import pt.ua.deti.icm.awav.data.model.UserRole
import android.util.Log

@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit,
    onRegisterSuccess: (UserRole) -> Unit,
    viewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory)
) {
    val email by viewModel.email.collectAsState()
    val password by viewModel.password.collectAsState()
    val selectedRole by viewModel.selectedRole.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val context = LocalContext.current
    
    // Local state for dialog control
    var showRoleDialog by remember { mutableStateOf(false) }
    
    // Check if user is already registered and logged in
    val currentUser by viewModel.currentUser.collectAsState()
    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            // Show success message
            Toast.makeText(
                context,
                "Successfully registered as ${selectedRole?.name?.lowercase() ?: "user"}!",
                Toast.LENGTH_LONG
            ).show()
            
            // Pass the selected role to the callback
            selectedRole?.let { onRegisterSuccess(it) }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Create Account",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        OutlinedTextField(
            value = email,
            onValueChange = { viewModel.updateEmail(it) },
            label = { Text("Email") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            )
        )
        
        OutlinedTextField(
            value = password,
            onValueChange = { viewModel.updatePassword(it) },
            label = { Text("Password") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            )
        )
        
        // Role selection button
        Button(
            onClick = { showRoleDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            )
        ) {
            Text("Select Role: ${selectedRole?.name ?: "Choose your role"}")
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = {
                viewModel.signUp { success ->
                    if (!success) {
                        Toast.makeText(
                            context,
                            "Registration failed. Please try again.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    // The LaunchedEffect will handle navigation on success
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = !loading && email.isNotEmpty() && password.length >= 6 && selectedRole != null
        ) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Register")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Google Sign-up button
        val googleLoading by viewModel.repositoryLoading.collectAsState()
        OutlinedButton(
            onClick = {
                if (selectedRole != null) {
                    Log.d("RegisterScreen", "Starting Google sign-up process")
                    viewModel.signUpWithGoogle { success, errorMessage ->
                        Log.d("RegisterScreen", "Google sign-up result: $success, error: $errorMessage")
                        if (!success) {
                            Toast.makeText(
                                context,
                                errorMessage ?: "Google sign-up failed. Please try again.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        // The LaunchedEffect with currentUser will handle navigation on success
                    }
                } else {
                    Toast.makeText(
                        context,
                        "Please select a role first",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, Color(0xFF4285F4)),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color(0xFF4285F4)
            ),
            enabled = !googleLoading && selectedRole != null
        ) {
            if (googleLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color(0xFF4285F4)
                )
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_google),
                        contentDescription = "Google Logo",
                        tint = Color.Unspecified
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sign up with Google")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        TextButton(onClick = onNavigateToLogin) {
            Text("Already have an account? Login")
        }
    }
    
    // Role selection dialog
    if (showRoleDialog) {
        AlertDialog(
            onDismissRequest = { showRoleDialog = false },
            title = { Text("Select Your Role") },
            text = { 
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        RadioButton(
                            selected = selectedRole == UserRole.PARTICIPANT,
                            onClick = { viewModel.updateSelectedRole(UserRole.PARTICIPANT) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Event Participant")
                    }
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        RadioButton(
                            selected = selectedRole == UserRole.STAND_WORKER,
                            onClick = { viewModel.updateSelectedRole(UserRole.STAND_WORKER) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Stand Worker")
                    }
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        RadioButton(
                            selected = selectedRole == UserRole.ORGANIZER,
                            onClick = { viewModel.updateSelectedRole(UserRole.ORGANIZER) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Event Organizer")
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showRoleDialog = false }) {
                    Text("Confirm")
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