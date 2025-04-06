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

@Composable
fun LoginScreen(
    onLoginSuccess: (UserRole) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf<UserRole?>(null) }
    var showRoleDialog by remember { mutableStateOf(false) }
    
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
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = { showRoleDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(AWAVStyles.buttonHeight),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Purple
                )
            ) {
                Text("Login")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            TextButton(onClick = { /* TODO: Navigate to registration */ }) {
                Text("Don't have an account? Sign up")
            }
        }
    }
    
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
                            onClick = { selectedRole = UserRole.PARTICIPANT }
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
                            onClick = { selectedRole = UserRole.STAND_WORKER }
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
                            onClick = { selectedRole = UserRole.ORGANIZER }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Event Organizer")
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (selectedRole != null) {
                            showRoleDialog = false
                            // Call the callback with selected role
                            onLoginSuccess(selectedRole!!)
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

