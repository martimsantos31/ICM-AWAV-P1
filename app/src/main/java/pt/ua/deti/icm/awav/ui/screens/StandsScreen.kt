package pt.ua.deti.icm.awav.ui.screens

import pt.ua.deti.icm.awav.R
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import pt.ua.deti.icm.awav.data.repository.StandRepository
import pt.ua.deti.icm.awav.ui.components.stands.StandCard
import pt.ua.deti.icm.awav.ui.navigation.Screen
import pt.ua.deti.icm.awav.ui.navigation.createRoute

@Composable
fun StandsScreen(navController: NavController = androidx.navigation.compose.rememberNavController()) {
    val stands = StandRepository.getAllStands()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Use data from the repository instead of hardcoded values
        stands.forEach { stand ->
            StandCard(
                title = stand.name,
                imageResId = when (stand.id) {
                    "1" -> R.drawable.chorizo
                    "2" -> R.drawable.kebab
                    else -> R.drawable.ic_launcher_foreground
                },
                onDetailsClick = {
                    // Navigate to stand details screen
                    navController.navigate(
                        Screen.StandDetails.createRoute("standId" to stand.id)
                    )
                },
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
    }
}
