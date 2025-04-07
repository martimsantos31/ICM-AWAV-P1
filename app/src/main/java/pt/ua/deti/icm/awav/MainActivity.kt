package pt.ua.deti.icm.awav

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
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
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        AwavNavigation(
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        // Show a message for older Android versions
                        Text(
                            text = "This app requires Android 12 or higher",
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}