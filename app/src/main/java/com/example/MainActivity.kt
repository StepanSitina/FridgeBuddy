package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.Manifest
import com.example.ui.screens.*
import com.example.ui.viewmodel.FridgeBuddyViewModel
import com.example.ui.theme.*

import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {

    private fun createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channelId = "fridgebuddy_notifications"
            val name = "FridgeBuddy Expirations"
            val descriptionText = "Alerts for expiring pantry items within 48 hours"
            val importance = android.app.NotificationManager.IMPORTANCE_DEFAULT
            val channel = android.app.NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: android.app.NotificationManager =
                getSystemService(android.content.Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showSystemNotification(title: String, text: String) {
        val channelId = "fridgebuddy_notifications"
        val notificationId = 101
        
        val builder = androidx.core.app.NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            
        with(androidx.core.app.NotificationManagerCompat.from(this)) {
            val isPermissionGranted = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                androidx.core.content.ContextCompat.checkSelfPermission(
                    this@MainActivity,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            } else true
            
            if (isPermissionGranted) {
                try {
                    notify(notificationId, builder.build())
                } catch (e: SecurityException) {
                    // Ignore gracefully
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel()
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                // Request permissions on startup
                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions()
                ) { permissions ->
                    // Handle permissions callback if needed
                }

                LaunchedEffect(Unit) {
                    val permissions = mutableListOf(
                        Manifest.permission.CAMERA,
                        Manifest.permission.RECORD_AUDIO
                    )
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                        permissions.add(Manifest.permission.POST_NOTIFICATIONS)
                    }
                    permissionLauncher.launch(permissions.toTypedArray())
                }

                // Initialize Central ViewModel
                val viewModel: FridgeBuddyViewModel = viewModel()
                val isSlovak by viewModel.isSlovak.collectAsState()
                val isOnboardingCompleted by viewModel.isOnboardingCompleted.collectAsState()
                val pantryItems by viewModel.pantryItems.collectAsState()
                val currentHouseholdId by viewModel.householdId.collectAsState()

                // State directly controlling custom browser notification alert visible state
                var showBrowserToast by remember { mutableStateOf(true) }
                
                // Identify items expiring within 48 hours
                val expiringItems = remember(pantryItems) {
                    pantryItems.filter {
                        val hoursLeft = (it.expirationTimestamp - System.currentTimeMillis()) / (1000L * 60 * 60)
                        hoursLeft in 0..48
                    }
                }

                // Whenever expiring items list changes, trigger a system-level notification
                LaunchedEffect(expiringItems) {
                    if (expiringItems.isNotEmpty()) {
                        val title = if (isSlovak) "Upozornenie prehliadača: Expirácia!" else "Upozornění prohlížeče: Expirace!"
                        val text = if (isSlovak) {
                            "Máte ${expiringItems.size} položiek blízko expirácie (do 48 hodín)."
                        } else {
                            "Máte ${expiringItems.size} položek blízko expirace (do 48 hodin)."
                        }
                        showSystemNotification(title, text)
                    }
                }

                val isTutorialCompleted by viewModel.isTutorialCompleted.collectAsState()

                if (!isOnboardingCompleted) {
                    OnboardingScreen(
                        viewModel = viewModel
                    )
                } else if (!isTutorialCompleted) {
                    TutorialScreen(
                        onFinish = { viewModel.completeTutorial() },
                        isSlovak = isSlovak
                    )
                } else {
                    var currentTab by remember { mutableStateOf("home") }
                    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                    val scope = rememberCoroutineScope()

                    var currentOverlayScreen by remember { mutableStateOf<String?>(null) }
                    val householdManager = remember { com.example.data.HouseholdManager() }

                    val homeLabel = if (isSlovak) "Domov" else "Domů"
                    val pantryLabel = if (isSlovak) "Spíž" else "Spižírna"
                    val recipeLabel = if (isSlovak) "Recepty" else "Recepty"
                    val shoppingLabel = if (isSlovak) "Nákupy" else "Nákupy"
                    val scannerLabel = if (isSlovak) "Skener" else "Skener"

                    ModalNavigationDrawer(
                        drawerState = drawerState,
                        drawerContent = {
                            ModalDrawerSheet(
                                drawerContainerColor = DarkSurface,
                                modifier = Modifier.width(300.dp)
                            ) {
                                Spacer(Modifier.height(24.dp))
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Dashboard, contentDescription = null, tint = SoftGreenGlow, modifier = Modifier.size(32.dp))
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Text("Menu", color = CreamText, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                                }
                                Divider(color = BorderNatural)
                                NavigationDrawerItem(
                                    icon = { Icon(Icons.Default.Person, contentDescription = "Profil", tint = FreshGreenPrimary) },
                                    label = { Text(if (isSlovak) "Môj Profil" else "Můj Profil", color = CreamText) },
                                    selected = currentOverlayScreen == "profile",
                                    onClick = { 
                                        currentOverlayScreen = "profile"
                                        scope.launch { drawerState.close() }
                                    },
                                    colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent, selectedContainerColor = SoftGreenGlow.copy(alpha = 0.2f))
                                )
                                NavigationDrawerItem(
                                    icon = { Icon(Icons.Default.Settings, contentDescription = "Nastavení", tint = FreshGreenPrimary) },
                                    label = { Text("Nastavení & Studio", color = CreamText) },
                                    selected = currentOverlayScreen == "settings",
                                    onClick = { 
                                        currentOverlayScreen = "settings"
                                        scope.launch { drawerState.close() }
                                    },
                                    colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent, selectedContainerColor = SoftGreenGlow.copy(alpha = 0.2f))
                                )
                                NavigationDrawerItem(
                                    icon = { Icon(Icons.Default.FamilyRestroom, contentDescription = "Rodina", tint = FreshGreenPrimary) },
                                    label = { Text("Rodina (Párování)", color = CreamText) },
                                    selected = currentOverlayScreen == "family",
                                    onClick = { 
                                        currentOverlayScreen = "family"
                                        scope.launch { drawerState.close() }
                                    },
                                    colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent, selectedContainerColor = SoftGreenGlow.copy(alpha = 0.2f))
                                )
                                NavigationDrawerItem(
                                    icon = { Icon(Icons.Default.MonitorWeight, contentDescription = "BMI", tint = FreshGreenPrimary) },
                                    label = { Text("BMI Kalkulačka", color = CreamText) },
                                    selected = currentOverlayScreen == "bmi",
                                    onClick = { 
                                        currentOverlayScreen = "bmi"
                                        scope.launch { drawerState.close() }
                                    },
                                    colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent, selectedContainerColor = SoftGreenGlow.copy(alpha = 0.2f))
                                )
                                NavigationDrawerItem(
                                    icon = { Icon(Icons.Default.History, contentDescription = "Historie aktivit", tint = FreshGreenPrimary) },
                                    label = { Text("Historie aktivit", color = CreamText) },
                                    selected = currentOverlayScreen == "history",
                                    onClick = { 
                                        currentOverlayScreen = "history"
                                        scope.launch { drawerState.close() }
                                    },
                                    colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent, selectedContainerColor = SoftGreenGlow.copy(alpha = 0.2f))
                                )
                            }
                        }
                    ) {
                        Scaffold(
                            modifier = Modifier.fillMaxSize(),
                            topBar = {
                                CenterAlignedTopAppBar(
                                    title = { Text(if (isSlovak) "NutriKalk" else "NutriKalk", fontWeight = FontWeight.Bold, color = CreamText) },
                                    navigationIcon = {
                                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = FreshGreenPrimary, modifier = Modifier.size(32.dp))
                                        }
                                    },
                                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = DarkBg)
                                )
                            },
                            bottomBar = {
                            NavigationBar(
                                containerColor = NavBgNatural,
                                contentColor = FreshGreenPrimary,
                                modifier = Modifier.testTag("bottom_nav_bar")
                            ) {
                                NavigationBarItem(
                                    selected = currentTab == "home",
                                    onClick = { currentTab = "home" },
                                    icon = { Icon(Icons.Default.Home, contentDescription = homeLabel, tint = if (currentTab == "home") FreshGreenPrimary else SaffronGoldSecondary) },
                                    label = { Text(homeLabel, fontSize = 10.sp, color = if (currentTab == "home") FreshGreenPrimary else SaffronGoldSecondary) },
                                    colors = NavigationBarItemDefaults.colors(
                                        indicatorColor = SoftGreenGlow,
                                        selectedIconColor = FreshGreenPrimary,
                                        unselectedIconColor = SaffronGoldSecondary
                                    ),
                                    modifier = Modifier.testTag("nav_home")
                                )

                                NavigationBarItem(
                                    selected = currentTab == "pantry",
                                    onClick = { currentTab = "pantry" },
                                    icon = { Icon(Icons.Default.Kitchen, contentDescription = pantryLabel, tint = if (currentTab == "pantry") FreshGreenPrimary else SaffronGoldSecondary) },
                                    label = { Text(pantryLabel, fontSize = 10.sp, color = if (currentTab == "pantry") FreshGreenPrimary else SaffronGoldSecondary) },
                                    colors = NavigationBarItemDefaults.colors(
                                        indicatorColor = SoftGreenGlow,
                                        selectedIconColor = FreshGreenPrimary,
                                        unselectedIconColor = SaffronGoldSecondary
                                    ),
                                    modifier = Modifier.testTag("nav_pantry")
                                )

                                NavigationBarItem(
                                    selected = currentTab == "recipes",
                                    onClick = { currentTab = "recipes" },
                                    icon = { Icon(Icons.Default.MenuBook, contentDescription = recipeLabel, tint = if (currentTab == "recipes") FreshGreenPrimary else SaffronGoldSecondary) },
                                    label = { Text(recipeLabel, fontSize = 10.sp, color = if (currentTab == "recipes") FreshGreenPrimary else SaffronGoldSecondary) },
                                    colors = NavigationBarItemDefaults.colors(
                                        indicatorColor = SoftGreenGlow,
                                        selectedIconColor = FreshGreenPrimary,
                                        unselectedIconColor = SaffronGoldSecondary
                                    ),
                                    modifier = Modifier.testTag("nav_recipes")
                                )

                                NavigationBarItem(
                                    selected = currentTab == "shopping",
                                    onClick = { currentTab = "shopping" },
                                    icon = { Icon(Icons.Default.ShoppingCart, contentDescription = shoppingLabel, tint = if (currentTab == "shopping") FreshGreenPrimary else SaffronGoldSecondary) },
                                    label = { Text(shoppingLabel, fontSize = 10.sp, color = if (currentTab == "shopping") FreshGreenPrimary else SaffronGoldSecondary) },
                                    colors = NavigationBarItemDefaults.colors(
                                        indicatorColor = SoftGreenGlow,
                                        selectedIconColor = FreshGreenPrimary,
                                        unselectedIconColor = SaffronGoldSecondary
                                    ),
                                    modifier = Modifier.testTag("nav_shopping")
                                )
                            }
                        }
                    ) { innerPadding ->
                        Box(modifier = Modifier.fillMaxSize()) {
                            // Render primary application screens under padding limits
                            Box(modifier = Modifier.padding(innerPadding)) {
                                when (currentTab) {
                                    "home" -> DashboardScreen(
                                        viewModel = viewModel,
                                        onNavigateToPantry = { currentTab = "pantry" },
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    "pantry" -> PantryScreen(
                                        viewModel = viewModel,
                                        modifier = Modifier.fillMaxSize(),
                                        onNavigateToTab = { currentTab = it }
                                    )
                                    "recipes" -> RecipeScreen(
                                        viewModel = viewModel,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    "shopping" -> ShoppingListScreen(
                                        viewModel = viewModel,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    "scanner" -> CentralScannerScreen(
                                        viewModel = viewModel,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                
                                // Overlay Screens
                                when (currentOverlayScreen) {
                                    "profile" -> {
                                        ProfileScreen(
                                            viewModel = viewModel,
                                            onNavigateToFamily = { currentOverlayScreen = "family" },
                                            onClose = { currentOverlayScreen = null }
                                        )
                                    }
                                    "settings" -> {
                                        com.example.ui.screens.SettingsOverlayScreen(
                                            onClose = { currentOverlayScreen = null },
                                            isSlovak = isSlovak
                                        )
                                    }
                                    "family" -> {
                                        Surface(color = DarkBg, modifier = Modifier.fillMaxSize()) {
                                            FamilyPairingScreen(
                                                viewModel = viewModel,
                                                householdManager = householdManager,
                                                onPairingSuccess = { _ -> currentOverlayScreen = null },
                                                onCancel = { currentOverlayScreen = null }
                                            )
                                        }
                                    }
                                    "bmi" -> {
                                        com.example.ui.screens.BmiCalculatorScreen(
                                            onClose = { currentOverlayScreen = null },
                                            isSlovak = isSlovak
                                        )
                                    }
                                    "history" -> {
                                        com.example.ui.screens.ActivityHistoryScreen(
                                            householdManager = householdManager,
                                            onClose = { currentOverlayScreen = null },
                                            isSlovak = isSlovak,
                                            householdId = currentHouseholdId
                                        )
                                    }
                                }
                            }

                            // --- Chrome-style web browser push notification box (Overlay) ---
                            if (showBrowserToast && expiringItems.isNotEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopCenter)
                                        .padding(top = 90.dp) // Float cleanly below typical custom headers
                                        .padding(horizontal = 16.dp)
                                        .fillMaxWidth()
                                        .testTag("browser_toast_overlay")
                                ) {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = LightSurface),
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(1.dp, SaffronGoldSecondary),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(Color(0xFF2D312E))
                                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = Icons.Default.Language,
                                                        contentDescription = "Chrome Push",
                                                        tint = SaffronGoldSecondary,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = "chrome-notification://fridgebuddy",
                                                        fontSize = 11.sp,
                                                        color = CaptionTextNatural,
                                                        fontWeight = FontWeight.Light,
                                                        maxLines = 1
                                                    )
                                                }
                                                IconButton(
                                                    onClick = { showBrowserToast = false },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Close,
                                                        contentDescription = "Close",
                                                        tint = CaptionTextNatural,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                }
                                            }

                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(10.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(34.dp)
                                                        .background(Color(0xFFFFB300).copy(alpha = 0.15f), CircleShape),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Warning,
                                                        contentDescription = "Warning",
                                                        tint = Color(0xFFFFB300),
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = if (isSlovak) "Expirácia v spíži (do 48h)" else "Expirace ve spižírně (do 48h)",
                                                        color = CreamText,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 13.sp
                                                    )
                                                    Text(
                                                        text = if (isSlovak) {
                                                            "Nájdených ${expiringItems.size} položiek s končiacou trvanlivosťou!"
                                                        } else {
                                                            "Nalezeno ${expiringItems.size} položek blížících se k expiraci!"
                                                        },
                                                        color = CaptionTextNatural,
                                                        fontSize = 11.sp
                                                    )
                                                }
                                            }

                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 10.dp)
                                                    .background(DarkBg, RoundedCornerShape(6.dp))
                                                    .padding(6.dp)
                                            ) {
                                                expiringItems.take(3).forEach { collapsingItem ->
                                                    val hours = ((collapsingItem.expirationTimestamp - System.currentTimeMillis()) / (1000L * 60 * 60)).coerceAtLeast(0)
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp),
                                                        horizontalArrangement = Arrangement.SpaceBetween
                                                    ) {
                                                        Text(
                                                            text = "• ${collapsingItem.name}",
                                                            color = CreamText,
                                                            fontSize = 10.sp,
                                                            maxLines = 1,
                                                            modifier = Modifier.weight(1f)
                                                        )
                                                        Text(
                                                            text = "${hours}h",
                                                            color = SaffronGoldSecondary,
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                }
                                                if (expiringItems.size > 3) {
                                                    Text(
                                                        text = if (isSlovak) "... a ${expiringItems.size - 3} ďalších" else "... a ${expiringItems.size - 3} dalších",
                                                        color = CaptionTextNatural,
                                                        fontSize = 9.sp,
                                                        modifier = Modifier.padding(start = 6.dp)
                                                    )
                                                }
                                            }

                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(8.dp),
                                                horizontalArrangement = Arrangement.End,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                TextButton(
                                                    modifier = Modifier.height(32.dp),
                                                    onClick = { showBrowserToast = false }
                                                ) {
                                                    Text(
                                                        text = if (isSlovak) "Ignorovať" else "Ignorovat",
                                                        color = CaptionTextNatural,
                                                        fontSize = 11.sp
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Button(
                                                    onClick = {
                                                        showBrowserToast = false
                                                        currentTab = "pantry"
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = SaffronGoldSecondary),
                                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                                                    modifier = Modifier.height(30.dp)
                                                ) {
                                                    Text(
                                                        text = if (isSlovak) "Otvorit spíž" else "Otevřít spižírnu",
                                                        color = Color.Black,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
}
