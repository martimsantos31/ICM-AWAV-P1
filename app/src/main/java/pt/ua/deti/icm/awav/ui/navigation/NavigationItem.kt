package pt.ua.deti.icm.awav.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

data class NavigationItem(
    val title: String,
    val icon: ImageVector,
    val route: String
)

val navigationItems = listOf(
    NavigationItem(
        title = "Feed",
        icon = Icons.Default.List,
        route = "feed"
    ),
    NavigationItem(
        title = "Timetable",
        icon = Icons.Default.Schedule,
        route = "timetable"
    ),
    NavigationItem(
        title = "Home",
        icon = Icons.Default.Home,
        route = "home"
    ),
    NavigationItem(
        title = "Stands",
        icon = Icons.Default.Store,
        route = "stands"
    ),
    NavigationItem(
        title = "Profile",
        icon = Icons.Default.Person,
        route = "profile"
    )
) 