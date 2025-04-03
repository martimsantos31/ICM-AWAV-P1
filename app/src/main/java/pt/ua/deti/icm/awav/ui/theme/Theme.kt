package pt.ua.deti.icm.awav.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import pt.ua.deti.icm.e.ui.theme.AWAVTypography

// Light Theme
private val LightColorScheme = lightColorScheme(
    primary = Purple,
    primaryContainer = LightPurple,
    onPrimary = White,
    secondary = Orange,
    onSecondary = White,
    background = White,
    surface = White,
    onBackground = Black,
    onSurface = Black,
    surfaceVariant = LightGray,
    onSurfaceVariant = DarkGray
)

// Dark Theme
private val DarkColorScheme = darkColorScheme(
    primary = Purple,
    primaryContainer = DarkPurple,
    onPrimary = White,
    secondary = Orange,
    onSecondary = White,
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    onBackground = White,
    onSurface = White,
    surfaceVariant = DarkGray,
    onSurfaceVariant = LightGray
)

@Composable
fun awavTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AWAVTypography,
        shapes = AWAVShapes,
        content = content
    )
}