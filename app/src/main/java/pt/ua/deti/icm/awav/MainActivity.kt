package pt.ua.deti.icm.awav

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import pt.ua.deti.icm.awav.ui.navigation.AwavNavigation
import pt.ua.deti.icm.awav.ui.theme.awavTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            awavTheme(dynamicColor = false) { // Explicitly disable dynamic color
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AwavNavigation(
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}