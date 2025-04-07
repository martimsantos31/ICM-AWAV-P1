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
import pt.ua.deti.icm.awav.ui.screens.auth.RegisterScreen
import pt.ua.deti.icm.awav.ui.screens.EditProfileScreen
import pt.ua.deti.icm.awav.ui.screens.FirebaseBuyTicketScreen
import pt.ua.deti.icm.awav.ui.screens.FirebaseMyTicketsScreen
import pt.ua.deti.icm.awav.ui.viewmodels.TicketViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import android.util.Log
import pt.ua.deti.icm.awav.data.AuthRepository

sealed class Screen(val route: String, val label: String, val selectedIcon: ImageVector, val unselectedIcon: ImageVector) {
    data object Login : Screen("login", "Login", Icons.AutoMirrored.Filled.Login, Icons.AutoMirrored.Outlined.Login)
    data object Register : Screen("register", "Register", Icons.Filled.PersonAdd, Icons.Outlined.PersonAdd)
    data object Home : Screen("home", "Home", Icons.Filled.Home, Icons.Outlined.Home)
    data object Feed : Screen("feed", "Feed", Icons.Filled.List, Icons.Outlined.List)
    data object Timetable : Screen("timetable", "Timetable", Icons.Filled.Schedule, Icons.Outlined.Schedule)
    data object Stands : Screen("stands", "Stands", Icons.Filled.Store, Icons.Outlined.Store)
    data object Profile : Screen("profile", "Profile", Icons.Filled.Person, Icons.Outlined.Person)
    data object EditProfile : Screen("edit_profile", "Edit Profile", Icons.Filled.Edit, Icons.Outlined.Edit)
    
    // Ticket screens
    data object MyTickets : Screen("my_tickets", "My Tickets", Icons.Filled.ConfirmationNumber, Icons.Outlined.ConfirmationNumber)
    data object BuyTicket : Screen("buy_ticket", "Buy Ticket", Icons.Filled.ShoppingCart, Icons.Outlined.ShoppingCart)
    
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
    
    // Track whether user has active tickets
    val ticketViewModel: TicketViewModel = viewModel(factory = TicketViewModel.Factory)
    val hasActiveTickets by ticketViewModel.hasActiveTickets.collectAsState()
    
    // Get access to AuthRepository for user state
    val authRepository = remember { AuthRepository.getInstance() }
    
    // Different navigation tabs based on user role and ticket status
    val participantScreens = if (hasActiveTickets) {
        listOf(Screen.Feed, Screen.Timetable, Screen.Home, Screen.Stands, Screen.Profile)
    } else {
        listOf(Screen.Home, Screen.Profile) // Limited access without tickets
    }
    
    // Different navigation tabs based on user role
    val organizerScreens = listOf(Screen.ManageEvents, Screen.CreateEvent, Screen.Profile)
    val workerScreens = listOf(Screen.ManageStand, Screen.SalesAnalytics, Screen.Profile)
    
    // Get the appropriate screens based on role
    val screens = remember(userRole, hasActiveTickets) {
        when (userRole) {
            UserRole.ORGANIZER -> organizerScreens
            UserRole.STAND_WORKER -> workerScreens
            UserRole.PARTICIPANT -> participantScreens
            null -> participantScreens // Default to participant screens
        }
    }
    
    // When login status changes, ensure we reset the navigation state
    LaunchedEffect(isLoggedIn) {
        if (!isLoggedIn) {
            // When logging out, reset hasActiveTickets to ensure proper navigation on next login
            Log.d("AwavNavigation", "User logged out, resetting navigation state")
            ticketViewModel.resetTicketStatus()
        }
    }
    
    // Define which routes are always accessible vs requiring tickets
    val alwaysAccessibleRoutes = listOf(
        Screen.Login.route,
        Screen.Register.route,
        Screen.Home.route,
        Screen.Profile.route,
        Screen.EditProfile.route,
        Screen.BuyTicket.route,
        Screen.MyTickets.route,
        Screen.Feed.route
    )
    
    // Observe current destination and redirect if needed - this needs to be at Composable level
    LaunchedEffect(currentRoute, hasActiveTickets, userRole) {
        // IMPORTANT: Force a refresh of ticket status when navigation changes to ensure accurate restrictions
        ticketViewModel.refreshTicketStatus()
        
        // Debug all relevant states when route changes
        Log.d("AwavNavigation", "Route: $currentRoute, hasTickets: $hasActiveTickets, userRole: $userRole, isLoggedIn: $isLoggedIn")
        
        // Make sure login state is correct - if we have a user role but not logged in, fix it
        if (userRole != null && !isLoggedIn) {
            isLoggedIn = true
            Log.d("AwavNavigation", "Fixed login state - set to logged in")
        }
        
        if (currentRoute != null && 
            !alwaysAccessibleRoutes.contains(currentRoute) && 
            userRole == UserRole.PARTICIPANT && 
            !hasActiveTickets) {
            // Redirect to Home if trying to access a restricted route
            Log.d("AwavNavigation", "Restricting access to $currentRoute - redirecting to home")
            navController.navigate(Screen.Home.route) {
                popUpTo(currentRoute) { inclusive = true }
            }
        }
    }
    
    Scaffold(
        bottomBar = {
            if (isLoggedIn && currentRoute != Screen.Login.route && currentRoute != Screen.Register.route) {
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
                            // Use a direct check of hasActiveTickets here to determine which tabs to show
                            val currentHasActiveTickets by ticketViewModel.hasActiveTickets.collectAsState()
                            
                            // Collect current user ID first, outside the remember block
                            val currentUserId by authRepository.currentUser.collectAsState()
                            val userUid = currentUserId?.uid ?: "none"
                            
                            // Generate a unique key for this user session to force refresh when user changes
                            val userSessionKey = remember(userRole, userUid) {
                                "${userRole}_$userUid"
                            }
                            
                            Log.d("NavBar", "Current user session key: $userSessionKey, hasTickets: $currentHasActiveTickets")
                            
                            // Only show the screens the user should have access to
                            // Important: Use userSessionKey as part of the filter to force recomposition when user changes
                            val filteredScreens = screens.filter { screen ->
                                Log.d("NavBar", "Filtering screen: ${screen.label}, userRole=$userRole, hasTickets=$currentHasActiveTickets, userKey=$userSessionKey")
                                
                                // Always show Home and Profile
                                if (screen == Screen.Home || screen == Screen.Profile || screen == Screen.BuyTicket || screen == Screen.MyTickets) {
                                    return@filter true
                                }
                                
                                // For participants, only show other tabs if they have active tickets
                                if (userRole == UserRole.PARTICIPANT) {
                                    val hasAccess = currentHasActiveTickets
                                    Log.d("NavBar", "Participant access to ${screen.label}: $hasAccess")
                                    return@filter hasAccess
                                }
                                
                                // For other roles (organizer, worker), show their tabs
                                return@filter true
                            }
                            
                            filteredScreens.forEach { screen ->
                                NavigationBarItem(
                                    icon = {
                                        Icon(
                                            imageVector = if (currentRoute == screen.route || 
                                                (screen == Screen.Feed && currentRoute == Screen.Feed.route) ||
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
                                        (screen == Screen.Feed && currentRoute == Screen.Feed.route) ||
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
            // Auth screen
            composable(Screen.Login.route) {
                LoginScreen(
                    onLoginSuccess = { role ->
                        // Reset ticket status first to ensure we don't carry over previous state
                        ticketViewModel.resetTicketStatus()
                        
                        isLoggedIn = true
                        userRole = role

                        // IMPORTANT: For participants, ALWAYS start with restricted access
                        // Only unlock after explicitly checking Firebase for tickets
                        if (role == UserRole.PARTICIPANT) {
                            // Force immediate check for tickets, start with restrictions
                            ticketViewModel.forceRestrictedStart()
                        }

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

            // Register screen
            composable(Screen.Register.route) {
                pt.ua.deti.icm.awav.ui.screens.auth.RegisterScreen(
                    onNavigateToLogin = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Register.route) { inclusive = true }
                        }
                    },
                    onRegisterSuccess = { role ->
                        // Reset ticket status first to ensure we don't carry over previous state
                        ticketViewModel.resetTicketStatus()
                        
                        isLoggedIn = true
                        userRole = role
                        
                        // IMPORTANT: For participants, ALWAYS start with restricted access
                        // Only unlock after explicitly checking Firebase for tickets
                        if (role == UserRole.PARTICIPANT) {
                            // Force immediate check for tickets, start with restrictions
                            ticketViewModel.forceRestrictedStart()
                        }
                        
                        // Navigate to appropriate starting screen based on role
                        val startRoute = when (role) {
                            UserRole.ORGANIZER -> Screen.ManageEvents.route
                            UserRole.STAND_WORKER -> Screen.ManageStand.route
                            UserRole.PARTICIPANT -> Screen.Home.route
                        }
                        
                        navController.navigate(startRoute) {
                            popUpTo(Screen.Register.route) { inclusive = true }
                        }
                    }
                )
            }

            // Home and common screens
            composable(Screen.Home.route) {
                HomeScreen(hasActiveTickets = hasActiveTickets, navController = navController)
            }
            
            // Profile screen - common for all roles
            composable(Screen.Profile.route) {
                ProfileScreen(navController = navController)
            }
            
            // Edit Profile Screen
            composable(Screen.EditProfile.route) {
                EditProfileScreen(navController = navController)
            }
            
            // Ticket screens
            composable(Screen.MyTickets.route) {
                FirebaseMyTicketsScreen(navController = navController)
            }
            
            composable(Screen.BuyTicket.route) {
                FirebaseBuyTicketScreen(navController = navController)
            }
            
            composable(Screen.Feed.route) { 
                if (userRole != UserRole.STAND_WORKER) {
                    FeedScreen(navController) // Use FeedScreen composable
                } else {
                    // Redirect workers to Home screen if they somehow access Feed
                    LaunchedEffect(Unit) {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Feed.route) { inclusive = true }
                        }
                    }
                    Box(modifier = Modifier.fillMaxSize())
                }
            }
            
            composable(Screen.Timetable.route) { 
                // Use recomposition-based approach to check ticket status
                val currentHasActiveTickets by ticketViewModel.hasActiveTickets.collectAsState()
                
                if (currentHasActiveTickets || userRole != UserRole.PARTICIPANT) {
                    ScheduleScreen(navController) 
                } else {
                    // Redirect to home if trying to access without a ticket
                    LaunchedEffect(Unit) {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Timetable.route) { inclusive = true }
                        }
                    }
                    Box(modifier = Modifier.fillMaxSize())
                }
            }
            
            composable(Screen.Stands.route) { 
                // Use recomposition-based approach to check ticket status
                val currentHasActiveTickets by ticketViewModel.hasActiveTickets.collectAsState()
                
                if (currentHasActiveTickets || userRole != UserRole.PARTICIPANT) {
                    StandsScreen(navController)
                } else {
                    // Redirect to home if trying to access without a ticket
                    LaunchedEffect(Unit) {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Stands.route) { inclusive = true }
                        }
                    }
                    Box(modifier = Modifier.fillMaxSize())
                }
            }
            
            // Organizer screens
            composable(Screen.CreateEvent.route) { 
                if (userRole == UserRole.ORGANIZER) {
                    CreateEventScreen(navController)
                } else {
                    LaunchedEffect(Unit) {
                        navController.navigate(Screen.Home.route)
                    }
                    Box(modifier = Modifier.fillMaxSize())
                }
            }
            
            composable(Screen.ManageEvents.route) { 
                if (userRole == UserRole.ORGANIZER) {
                    ManageEventsScreen(navController)
                } else {
                    LaunchedEffect(Unit) {
                        navController.navigate(Screen.Home.route)
                    }
                    Box(modifier = Modifier.fillMaxSize())
                }
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
                if (userRole == UserRole.STAND_WORKER) {
                    ManageStandScreen(navController)
                } else {
                    LaunchedEffect(Unit) {
                        navController.navigate(Screen.Home.route)
                    }
                    Box(modifier = Modifier.fillMaxSize())
                }
            }
            
            composable(Screen.SalesAnalytics.route) { 
                if (userRole == UserRole.STAND_WORKER) {
                    SalesAnalyticsScreen()
                } else {
                    LaunchedEffect(Unit) {
                        navController.navigate(Screen.Home.route)
                    }
                    Box(modifier = Modifier.fillMaxSize())
                }
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