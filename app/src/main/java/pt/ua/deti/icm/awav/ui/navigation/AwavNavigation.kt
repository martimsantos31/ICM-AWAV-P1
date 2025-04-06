package pt.ua.deti.icm.awav.ui.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.automirrored.outlined.Login
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import pt.ua.deti.icm.awav.data.model.UserRole
import pt.ua.deti.icm.awav.ui.screens.*
import pt.ua.deti.icm.awav.ui.screens.organizer.CreateEventScreen
import pt.ua.deti.icm.awav.ui.screens.organizer.EventDetailsScreen
import pt.ua.deti.icm.awav.ui.screens.organizer.ManageEventsScreen
import pt.ua.deti.icm.awav.ui.screens.stand.*
import pt.ua.deti.icm.awav.ui.screens.worker.ManageStandScreen
import pt.ua.deti.icm.awav.ui.screens.worker.SalesAnalyticsScreen
import pt.ua.deti.icm.awav.ui.theme.AWAVStyles
import androidx.lifecycle.viewmodel.compose.viewModel
import pt.ua.deti.icm.awav.ui.screens.auth.RegisterScreen
import pt.ua.deti.icm.awav.ui.screens.auth.AuthViewModel

sealed class Screen(val route: String, val label: String, val selectedIcon: ImageVector, val unselectedIcon: ImageVector) {
    data object Login : Screen("login", "Login", Icons.AutoMirrored.Filled.Login, Icons.AutoMirrored.Outlined.Login)
    data object Register : Screen("register", "Register", Icons.Filled.PersonAdd, Icons.Outlined.PersonAdd)
    data object Home : Screen("home", "Home", Icons.Filled.Home, Icons.Outlined.Home)
    data object Chat : Screen("chat", "Chat", Icons.AutoMirrored.Filled.Chat, Icons.AutoMirrored.Outlined.Chat)
    data object LiveChat : Screen("live_chat", "Live Chat", Icons.AutoMirrored.Filled.Chat, Icons.AutoMirrored.Outlined.Chat)
    data object Timetable : Screen("timetable", "Timetable", Icons.Filled.Schedule, Icons.Outlined.Schedule)
    data object Stands : Screen("stands", "Stands", Icons.Filled.Store, Icons.Outlined.Store)
    data object Profile : Screen("profile", "Profile", Icons.Filled.Person, Icons.Outlined.Person)
    
    // Organizer-specific screens
    data object CreateEvent : Screen("create_event", "Create Event", Icons.Filled.Add, Icons.Outlined.Add)
    data object ManageEvents : Screen("manage_events", "Manage Events", Icons.Filled.Event, Icons.Outlined.Event)
    data object EventDetails : Screen("event_details/{eventId}", "Event Details", Icons.Filled.Event, Icons.Outlined.Event)
    
    // Stand Worker screens
    data object ManageStand : Screen("manage_stand", "Manage Stand", Icons.Filled.Store, Icons.Outlined.Store)
    data object SalesAnalytics : Screen("sales_analytics", "Sales Analytics", Icons.Filled.BarChart, Icons.Outlined.BarChart)
    
    // Stand detail screens for participants
    data object StandDetails : Screen("stand_details/{standId}", "Stand Details", Icons.Filled.Store, Icons.Outlined.Store)
    data object StandMenu : Screen("stand_menu/{standId}", "Menu", Icons.Filled.MenuBook, Icons.Outlined.MenuBook)
    data object StandOrder : Screen("stand_order/{standId}", "Order", Icons.Filled.ShoppingCart, Icons.Outlined.ShoppingCart)
    data object StandCart : Screen("stand_cart/{standId}", "Cart", Icons.Filled.ShoppingCart, Icons.Outlined.ShoppingCart)
}

// Extension function to create routes with parameters
fun Screen.createRoute(vararg params: Pair<String, String>): String {
    return when (this) {
        is Screen.StandDetails, 
        is Screen.StandMenu, 
        is Screen.StandOrder,
        is Screen.StandCart,
        is Screen.EventDetails -> {
            var route = this.route
            params.forEach { (key, value) ->
                route = route.replace("{$key}", value)
            }
            route
        }
        else -> this.route
    }
}

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@Composable
fun AwavNavigation(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    // For simplicity, we'll use a state to track user login and role
    var isLoggedIn by remember { mutableStateOf(false) }
    var userRole by remember { mutableStateOf<UserRole?>(null) }
    
    // Different navigation tabs based on user role
    val participantScreens = listOf(Screen.Chat, Screen.Timetable, Screen.Home, Screen.Stands, Screen.Profile)
    val organizerScreens = listOf(Screen.ManageEvents, Screen.CreateEvent, Screen.Profile)
    val workerScreens = listOf(Screen.ManageStand, Screen.SalesAnalytics, Screen.Profile)
    
    // Get the appropriate screens based on role
    val screens = when (userRole) {
        UserRole.ORGANIZER -> organizerScreens
        UserRole.STAND_WORKER -> workerScreens
        UserRole.PARTICIPANT -> participantScreens
        null -> participantScreens // Default to participant screens
    }
    
    Scaffold(
        bottomBar = {
            if (isLoggedIn && currentRoute != Screen.Login.route) {
                Surface(
                    tonalElevation = 4.dp,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(AWAVStyles.navBarHeight)
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth().fillMaxHeight(),
                        contentAlignment = Alignment.Center,
                    ) {
                        NavigationBar(
                            contentColor = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            screens.forEach { screen ->
                                NavigationBarItem(
                                    icon = {
                                        Icon(
                                            imageVector = if (currentRoute == screen.route || 
                                                (screen == Screen.Chat && currentRoute == Screen.LiveChat.route && userRole != UserRole.STAND_WORKER) ||
                                                (screen == Screen.ManageStand && currentRoute == Screen.ManageStand.route)
                                            ) screen.selectedIcon else screen.unselectedIcon,
                                            contentDescription = screen.label
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = screen.label,
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                    },
                                    selected = currentRoute == screen.route || 
                                        (screen == Screen.Chat && currentRoute == Screen.LiveChat.route && userRole != UserRole.STAND_WORKER) ||
                                        (screen == Screen.ManageStand && currentRoute == Screen.ManageStand.route),
                                    onClick = {
                                        navController.navigate(screen.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.primary,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        indicatorColor = Color.Transparent,
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Login.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Auth screens
            composable(Screen.Login.route) { 
                LoginScreen(
                    onLoginSuccess = { role ->
                        isLoggedIn = true
                        userRole = role
                        
                        // Navigate to appropriate starting screen based on role
                        val startRoute = when (role) {
                            UserRole.ORGANIZER -> Screen.ManageEvents.route
                            UserRole.STAND_WORKER -> Screen.ManageStand.route
                            UserRole.PARTICIPANT -> Screen.Home.route
                        }
                        
                        navController.navigate(startRoute) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    navController = navController
                )
            }
            
            composable(Screen.Register.route) {
                // Don't change login state here
                RegisterScreen(
                    onNavigateToLogin = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Register.route) { inclusive = true }
                        }
                    },
                    onRegisterSuccess = { role ->
                        // Set login state and user role
                        isLoggedIn = true
                        userRole = role
                        
                        // Navigate to appropriate starting screen based on role
                        val startRoute = when (role) {
                            UserRole.ORGANIZER -> Screen.ManageEvents.route
                            UserRole.STAND_WORKER -> Screen.ManageStand.route
                            UserRole.PARTICIPANT -> Screen.Home.route
                        }
                        
                        // Navigate to home screen after successful registration
                        navController.navigate(startRoute) {
                            popUpTo(Screen.Register.route) { inclusive = true }
                        }
                    }
                )
            }
            
            // Common screens
            composable(Screen.Home.route) { HomeScreen() }
            composable(Screen.Chat.route) { 
                if (userRole != UserRole.STAND_WORKER) {
                    ChatScreen(navController) 
                } else {
                    // Redirect workers to Home screen if they somehow access Chat
                    LaunchedEffect(Unit) {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Chat.route) { inclusive = true }
                        }
                    }
                    Box(modifier = Modifier.fillMaxSize())
                }
            }
            composable(Screen.LiveChat.route) { 
                if (userRole != UserRole.STAND_WORKER) {
                    LiveChatScreen(navController)
                } else {
                    // Redirect workers to Home screen if they somehow access LiveChat
                    LaunchedEffect(Unit) {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.LiveChat.route) { inclusive = true }
                        }
                    }
                    Box(modifier = Modifier.fillMaxSize())
                }
            }
            composable(Screen.Timetable.route) { TimetableScreen() }
            composable(Screen.Stands.route) { StandsScreen(navController) }
            composable(Screen.Profile.route) { ProfileScreen(navController) }
            
            // Organizer screens
            composable(Screen.CreateEvent.route) { 
                CreateEventScreen(navController)
            }
            composable(Screen.ManageEvents.route) { 
                ManageEventsScreen(navController)
            }
            composable(
                route = Screen.EventDetails.route,
                arguments = listOf(navArgument("eventId") { type = NavType.StringType })
            ) { backStackEntry ->
                val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
                EventDetailsScreen(
                    eventId = eventId,
                    navController = navController
                )
            }
            
            // Stand Worker screens
            composable(Screen.ManageStand.route) { 
                ManageStandScreen(navController)
            }
            composable(Screen.SalesAnalytics.route) { 
                SalesAnalyticsScreen()
            }
            
            // Stand detail screens for participants
            composable(
                route = Screen.StandDetails.route,
                arguments = listOf(navArgument("standId") { type = NavType.StringType })
            ) { backStackEntry ->
                val standId = backStackEntry.arguments?.getString("standId") ?: ""
                StandDetailsScreen(
                    standId = standId,
                    navController = navController
                )
            }
            
            composable(
                route = Screen.StandMenu.route,
                arguments = listOf(navArgument("standId") { type = NavType.StringType })
            ) { backStackEntry ->
                val standId = backStackEntry.arguments?.getString("standId") ?: ""
                StandMenuScreen(
                    standId = standId,
                    navController = navController
                )
            }
            
            composable(
                route = Screen.StandOrder.route,
                arguments = listOf(navArgument("standId") { type = NavType.StringType })
            ) { backStackEntry ->
                val standId = backStackEntry.arguments?.getString("standId") ?: ""
                StandOrderScreen(
                    standId = standId,
                    navController = navController
                )
            }
            
            composable(
                route = Screen.StandCart.route,
                arguments = listOf(navArgument("standId") { type = NavType.StringType })
            ) { backStackEntry ->
                val standId = backStackEntry.arguments?.getString("standId") ?: ""
                StandCartScreen(
                    standId = standId,
                    navController = navController
                )
            }
        }
    }
}