package pt.ua.deti.icm.awav.ui.components.stands

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pt.ua.deti.icm.awav.data.model.Stand

@Composable
fun AddStandDialog(
    onDismiss: () -> Unit,
    onConfirm: (Stand) -> Unit
) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Stand") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Stand Name") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(
                            Stand(
                                id = 0, // Room will ignore this value and auto-generate the ID
                                name = name
                            )
                        )
                        onDismiss()
                    }
                },
                enabled = name.isNotBlank()
            ) {
                Text("Add Stand")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
} 