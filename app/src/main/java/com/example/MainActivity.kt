package com.example

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import androidx.room.Room
import com.example.data.AppConfigRepository
import com.example.data.AppDatabase
import com.example.data.AppRepository
import com.example.ui.breathe.BreathingExerciseScreen
import com.example.ui.home.AppSelectionScreen
import com.example.ui.home.DashboardScreen
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.BoxWithConstraints
import com.example.ui.profile.ProfileScreen
import com.example.ui.history.HistoryScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.FocusViewModel
import com.example.viewmodel.FocusViewModelFactory

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class MainActivity : ComponentActivity() {
    
    internal val _newIntents = MutableSharedFlow<Intent>(extraBufferCapacity = 1)
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        _newIntents.tryEmit(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val database = AppDatabase.getDatabase(applicationContext)
        val appRepo = AppRepository(database.intentDao())
        val configRepo = AppConfigRepository(applicationContext)
        val factory = FocusViewModelFactory(appRepo, configRepo, applicationContext)

        setContent {
            MyApplicationTheme {
                Surface(
                    color = MaterialTheme.colorScheme.background,
                    modifier = Modifier.fillMaxSize()
                ) {
                    val navController = rememberNavController()
                    val focusViewModel: FocusViewModel = viewModel(factory = factory)
                    
                    var startDest by remember { mutableStateOf(if (intent.getStringExtra("intercept_package") != null) "breathe?appName=${Uri.encode(intent.getStringExtra("intercept_package"))}" else "dashboard") }

                    val activity = androidx.compose.ui.platform.LocalContext.current as? MainActivity
                    LaunchedEffect(activity) {
                        activity?._newIntents?.collect { newIntent ->
                            val pkg = newIntent.getStringExtra("intercept_package")
                            if (pkg != null) {
                                val route = "breathe?appName=${Uri.encode(pkg)}"
                                navController.navigate(route) {
                                    launchSingleTop = true
                                }
                            }
                        }
                    }

                    val currentBackStack by navController.currentBackStackEntryAsState()
                    val currentRoute = currentBackStack?.destination?.route ?: startDest

                    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val isTablet = maxWidth > 600.dp
                    
                    val showNavigation = currentRoute in listOf("dashboard", "apps", "history", "profile")

                    Scaffold(
                        bottomBar = {
                            if (!isTablet && showNavigation) {
                                NavigationBar {
                                    NavigationBarItem(
                                        selected = currentRoute == "dashboard",
                                        onClick = { navController.navigate("dashboard") },
                                        icon = { Icon(Icons.Default.Home, contentDescription = "Dashboard") },
                                        label = { Text("Dashboard") }
                                    )
                                    NavigationBarItem(
                                        selected = currentRoute == "history",
                                        onClick = { navController.navigate("history") },
                                        icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "History") },
                                        label = { Text("History") }
                                    )
                                    NavigationBarItem(
                                        selected = currentRoute == "apps",
                                        onClick = { navController.navigate("apps") },
                                        icon = { Icon(Icons.Default.Settings, contentDescription = "Apps") },
                                        label = { Text("Apps") }
                                    )
                                    NavigationBarItem(
                                        selected = currentRoute == "profile",
                                        onClick = { navController.navigate("profile") },
                                        icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                                        label = { Text("Profile") }
                                    )
                                }
                            }
                        }
                    ) { paddingVals ->
                        Row(modifier = Modifier.fillMaxSize().padding(paddingVals)) {
                            if (isTablet && showNavigation) {
                                NavigationRail {
                                    NavigationRailItem(
                                        selected = currentRoute == "dashboard",
                                        onClick = { navController.navigate("dashboard") },
                                        icon = { Icon(Icons.Default.Home, contentDescription = "Dashboard") },
                                        label = { Text("Dashboard") }
                                    )
                                    NavigationRailItem(
                                        selected = currentRoute == "history",
                                        onClick = { navController.navigate("history") },
                                        icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "History") },
                                        label = { Text("History") }
                                    )
                                    NavigationRailItem(
                                        selected = currentRoute == "apps",
                                        onClick = { navController.navigate("apps") },
                                        icon = { Icon(Icons.Default.Settings, contentDescription = "Apps") },
                                        label = { Text("Apps") }
                                    )
                                    NavigationRailItem(
                                        selected = currentRoute == "profile",
                                        onClick = { navController.navigate("profile") },
                                        icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                                        label = { Text("Profile") }
                                    )
                                }
                            }
                            
                            androidx.compose.foundation.layout.Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                NavHost(
                                    navController = navController, 
                                    startDestination = startDest,
                                    modifier = Modifier.fillMaxSize().widthIn(max = 840.dp),
                                    enterTransition = {
                                    slideInHorizontally(
                                        initialOffsetX = { it },
                                        animationSpec = tween(300)
                                    ) + fadeIn(animationSpec = tween(300))
                                },
                                exitTransition = {
                                    slideOutHorizontally(
                                        targetOffsetX = { -it },
                                        animationSpec = tween(300)
                                    ) + fadeOut(animationSpec = tween(300))
                                },
                                popEnterTransition = {
                                    slideInHorizontally(
                                        initialOffsetX = { -it },
                                        animationSpec = tween(300)
                                    ) + fadeIn(animationSpec = tween(300))
                                },
                                popExitTransition = {
                                    slideOutHorizontally(
                                        targetOffsetX = { it },
                                        animationSpec = tween(300)
                                    ) + fadeOut(animationSpec = tween(300))
                                }
                            ) {
                                composable("dashboard") {
                                    val intercepts by focusViewModel.totalIntercepts.collectAsState()
                                    val prevented by focusViewModel.preventedLaunches.collectAsState()
                                    val interceptsWeek by focusViewModel.totalInterceptsWeek.collectAsState()
                                    val preventedWeek by focusViewModel.preventedLaunchesWeek.collectAsState()
                                    val apps by focusViewModel.blockedApps.collectAsState()
                                    val context = androidx.compose.ui.platform.LocalContext.current.applicationContext
                                    
                                    LaunchedEffect(intercepts, prevented) {
                                        com.example.widget.updateFocusStatsWidget(context, intercepts, prevented)
                                    }
                                    
                                    DashboardScreen(
                                        totalIntercepts = intercepts,
                                        preventedLaunches = prevented,
                                        totalInterceptsWeek = interceptsWeek,
                                        preventedLaunchesWeek = preventedWeek,
                                        blockedApps = apps,
                                        onManageBlockedApps = { navController.navigate("apps") },
                                        onTestIntercept = { appName -> 
                                            navController.navigate("breathe?appName=${Uri.encode(appName)}") 
                                        },
                                        onOpenProfile = { navController.navigate("profile") }
                                    )
                                }
                                composable("history") {
                                    val history by focusViewModel.historicalSessions.collectAsState()
                                    HistoryScreen(historicalSessions = history)
                                }
                                composable("apps") {
                                    val apps by focusViewModel.blockedApps.collectAsState()
                                    AppSelectionScreen(
                                        blockedApps = apps,
                                        onToggleApp = focusViewModel::toggleAppBlocked,
                                        onBack = { navController.popBackStack() }
                                    )
                                }
                                composable("profile") {
                                    val interceptsWeek by focusViewModel.totalInterceptsWeek.collectAsState()
                                    val preventedWeek by focusViewModel.preventedLaunchesWeek.collectAsState()
                                    val history by focusViewModel.historicalSessions.collectAsState()
                                    ProfileScreen(
                                        totalInterceptsWeek = interceptsWeek,
                                        preventedWeek = preventedWeek,
                                        historicalSessions = history
                                    )
                                }
                                composable(
                                    route = "breathe?appName={appName}",
                                    arguments = listOf(navArgument("appName") { type = NavType.StringType; defaultValue = "Unknown App" })
                                ) { backStackEntry ->
                                    val packageName = backStackEntry.arguments?.getString("appName") ?: "Unknown App"
                                    val localContext = androidx.compose.ui.platform.LocalContext.current
                                    val displayAppName = remember(packageName) {
                                        try {
                                            localContext.packageManager.getApplicationLabel(
                                                localContext.packageManager.getApplicationInfo(packageName, 0)
                                            ).toString()
                                        } catch (e: Exception) {
                                            packageName
                                        }
                                    }
                                    BreathingExerciseScreen(
                                        appName = displayAppName,
                                        onFinish = { didContinue ->
                                            focusViewModel.recordSession(displayAppName, packageName, 10, didContinue)
                                            if (didContinue) {
                                                // Try to launch the app
                                                val launchIntent = localContext.packageManager.getLaunchIntentForPackage(packageName)
                                                if (launchIntent != null) {
                                                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                                    localContext.startActivity(launchIntent)
                                                }
                                            } else {
                                                val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                                                    addCategory(Intent.CATEGORY_HOME)
                                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                                }
                                                localContext.startActivity(homeIntent)
                                            }
                                            
                                            if (!navController.popBackStack()) {
                                                navController.navigate("dashboard") {
                                                    popUpTo(0) { inclusive = true }
                                                }
                                                (localContext as? android.app.Activity)?.finishAndRemoveTask()
                                            }
                                        }
                                    )
                                }
                            }
                            } // Close Box
                        }
                    }
                }
                }
            }
        }
    }
}
