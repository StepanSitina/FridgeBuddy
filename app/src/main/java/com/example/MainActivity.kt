package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.Manifest
import com.example.ui.screens.*
import com.example.ui.viewmodel.FridgeBuddyViewModel
import com.example.ui.theme.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
                    permissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.CAMERA,
                            Manifest.permission.RECORD_AUDIO
                        )
                    )
                }

                // Initialize Central ViewModel
                val viewModel: FridgeBuddyViewModel = viewModel()
                val isSlovak by viewModel.isSlovak.collectAsState()
                val isOnboardingCompleted by viewModel.isOnboardingCompleted.collectAsState()

                if (!isOnboardingCompleted) {
                    OnboardingScreen(
                        viewModel = viewModel
                    )
                } else {
                    var currentTab by remember { mutableStateOf("home") }

                    val homeLabel = if (isSlovak) "Domov" else "Domů"
                val pantryLabel = if (isSlovak) "Spíž" else "Spižírna"
                val recipeLabel = if (isSlovak) "Recepty" else "Recepty"
                val shoppingLabel = if (isSlovak) "Nákupy" else "Nákupy"
                val scannerLabel = if (isSlovak) "Skener" else "Skener"

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
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

                            NavigationBarItem(
                                selected = currentTab == "scanner",
                                onClick = { currentTab = "scanner" },
                                icon = { Icon(Icons.Default.DocumentScanner, contentDescription = scannerLabel, tint = if (currentTab == "scanner") FreshGreenPrimary else SaffronGoldSecondary) },
                                label = { Text(scannerLabel, fontSize = 10.sp, color = if (currentTab == "scanner") FreshGreenPrimary else SaffronGoldSecondary) },
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = SoftGreenGlow,
                                    selectedIconColor = FreshGreenPrimary,
                                    unselectedIconColor = SaffronGoldSecondary
                                ),
                                modifier = Modifier.testTag("nav_scanner")
                            )
                        }
                    }
                ) { innerPadding ->
                    when (currentTab) {
                        "home" -> DashboardScreen(
                            viewModel = viewModel,
                            onNavigateToPantry = { currentTab = "pantry" },
                            modifier = Modifier.padding(innerPadding)
                        )
                        "pantry" -> PantryScreen(
                            viewModel = viewModel,
                            modifier = Modifier.padding(innerPadding),
                            onNavigateToTab = { currentTab = it }
                        )
                        "recipes" -> RecipeScreen(
                            viewModel = viewModel,
                            modifier = Modifier.padding(innerPadding)
                        )
                        "shopping" -> ShoppingListScreen(
                            viewModel = viewModel,
                            modifier = Modifier.padding(innerPadding)
                        )
                        "scanner" -> CentralScannerScreen(
                            viewModel = viewModel,
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }
}
}
