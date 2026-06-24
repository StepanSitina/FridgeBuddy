package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.*
import com.example.ui.viewmodel.FridgeBuddyViewModel
import com.example.data.PantryItem
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(
    viewModel: FridgeBuddyViewModel,
    onNavigateToPantry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isSlovak by viewModel.isSlovak.collectAsState()
    val calorieGoal by viewModel.calorieGoal.collectAsState()
    val caloriesToday by viewModel.caloriesConsumedToday.collectAsState()
    val carbsToday by viewModel.carbsToday.collectAsState()
    val proteinToday by viewModel.proteinToday.collectAsState()
    val fatToday by viewModel.fatToday.collectAsState()
    val pantryItems by viewModel.pantryItems.collectAsState()
    val backupStatus by viewModel.backupStatus.collectAsState()
    val healthConnectSynced by viewModel.isHealthConnectSynced.collectAsState()
    
    // Nové proměnné pro rozpad kalorií z hodinek (Mock data pro UI demonstraci)
    val activeWatchCalories = 450 // Mock (Aktivita: +450 kcal z hodinek)
    val baseCalorieGoal = calorieGoal
    val adjustedCalorieGoal = baseCalorieGoal + activeWatchCalories

    var showGoalDialog by remember { mutableStateOf(false) }
    var fastMealInput by remember { mutableStateOf("") }
    var showFastMealDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showFamilyPairingDialog by remember { mutableStateOf(false) }
    val householdManager = remember { com.example.data.HouseholdManager() }

    val currency = if (isSlovak) "EUR" else "Kč"

    val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val suggestedType = when {
        currentHour < 11 -> "Snídaně"
        currentHour < 15 -> "Oběd"
        currentHour < 18 -> "Svačina"
        else -> "Večeře"
    }

    val localizedType = if (isSlovak) {
        when (suggestedType) {
            "Snídaně" -> "Raňajky"
            "Oběd" -> "Obed"
            "Svačina" -> "Desiata"
            else -> "Večera"
        }
    } else {
        when (suggestedType) {
            "Snídaně" -> "Snídaně"
            "Oběd" -> "Oběd"
            "Svačina" -> "Svačina"
            else -> "Večeře"
        }
    }

    // Multi-language strings
    val greetingText = if (isSlovak) {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when {
            hour < 9 -> "Dobré ráno, kulinár!"
            hour < 18 -> "Pekný deň s FridgeBuddy!"
            else -> "Dobrý večer, čo uvaríme?"
        }
    } else {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when {
            hour < 9 -> "Dobré ráno, kuchaři!"
            hour < 18 -> "Hezký den s FridgeBuddy!"
            else -> "Dobrý večer, co uvaříme?"
        }
    }

    val subtitleText = if (isSlovak) "Prehľad vašej chladničky a denný kalorický plán." else "Přehled vaší chladničky a denní kalorický plán."

    // Compute status variables
    val expiringSoonCount = pantryItems.count {
        val daysLeft = ((it.expirationTimestamp - System.currentTimeMillis()) / (24 * 60 * 60 * 1000L))
        daysLeft <= 2
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBg)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .windowInsetsPadding(WindowInsets.safeDrawing),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 1. Header with App Title, Language Indicator / Logo
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(45.dp)
                        .background(
                            Brush.radialGradient(listOf(SoftGreenGlow, FreshGreenPrimary)),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Eco,
                        contentDescription = "Logo",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "FridgeBuddy",
                        color = FreshGreenPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(SafeGreen, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isSlovak) "Lokál: Slovensko (EUR)" else "Lokál: Česko (CZK)",
                            color = CreamText.copy(alpha = 0.6f),
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Czech / Slovak fast language switcher pill
            Button(
                onClick = { viewModel.toggleLanguage() },
                colors = ButtonDefaults.buttonColors(containerColor = SoftGreenGlow, contentColor = FreshGreenPrimary),
                shape = RoundedCornerShape(20.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                modifier = Modifier.testTag("lang_toggle_button")
            ) {
                Text(
                    text = if (isSlovak) "🇸🇰 SK" else "🇨🇿 CZ",
                    color = FreshGreenPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }

        // 2. Localized Greeting and Quick Status Box
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = greetingText,
                color = CreamText,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp
            )
            Text(
                text = subtitleText,
                color = CreamText.copy(alpha = 0.7f),
                fontSize = 14.sp
            )
        }

        // Expiration Warning Banner if items are expiring
        if (expiringSoonCount > 0) {
            Card(
                colors = CardDefaults.cardColors(containerColor = AlertDangerBg),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, AlertDangerBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToPantry() }
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = "Warning",
                        tint = AlertDangerText,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isSlovak) "Suroviny expirujú!" else "Suroviny expirují!",
                            color = AlertDangerText,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = if (isSlovak) {
                                "Máte $expiringSoonCount položky, ktoré treba spotrebovať do 48 hodín. Pozrieť v spíži."
                            } else {
                                "Máte $expiringSoonCount položky, které je třeba spotřebovat do 48 hodin. Podívat do spíže."
                            },
                            color = AlertDangerText.copy(alpha = 0.8f),
                            fontSize = 13.sp
                        )
                    }
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = "Arrow",
                        tint = AlertDangerText
                    )
                }
            }
        }

        // 3. Dual Ring Custom Canvas Macro Chart & Calories Tracker
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, BorderNatural),
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Responsive Canvas drawing of nutrition rings
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.35f)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val progressRatio = if (adjustedCalorieGoal > 0) caloriesToday.toFloat() / adjustedCalorieGoal else 0f
                    val animatedProgress = Math.min(progressRatio, 1f)

                    Canvas(modifier = Modifier.fillMaxSize().aspectRatio(1f)) {
                        // Drawing circles
                        val strokeWidth = 10.dp.toPx()
                        
                        // Calorie Background
                        drawCircle(
                            color = BorderNatural.copy(alpha = 0.6f),
                            radius = size.width / 2,
                            style = Stroke(width = strokeWidth)
                        )
                        // Calorie Highlight Ring
                        drawArc(
                            color = FreshGreenPrimary,
                            startAngle = -90f,
                            sweepAngle = 360f * animatedProgress,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )

                        // Outer thin Macro carb/protein ring
                        val subStrokeWidth = 4.dp.toPx()
                        drawArc(
                            color = SaffronGoldSecondary,
                            startAngle = 180f,
                            sweepAngle = 160f,
                            useCenter = false,
                            style = Stroke(width = subStrokeWidth, cap = StrokeCap.Round)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$caloriesToday",
                            color = CreamText,
                            fontSize = 25.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "kcal",
                            color = CreamText.copy(alpha = 0.6f),
                            fontSize = 12.sp
                        )
                        Text(
                            text = "/ $adjustedCalorieGoal",
                            color = SaffronGoldSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Watch, contentDescription = "Watch Sync", tint = CreamText.copy(alpha = 0.5f), modifier = Modifier.size(10.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Box(modifier = Modifier.size(4.dp).background(Color.Green, CircleShape))
                        }
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Breakdown of Macros (responsive nutrient logs)
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (isSlovak) "Zvyšný rozpočet" else "Zbývající rozpočet",
                        color = CreamText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    
                    // Rozpad - Základ: 2000 | Aktivita: 450
                    Text(
                        text = "Základ: $baseCalorieGoal kcal | Aktivita: +$activeWatchCalories kcal",
                        color = CreamText.copy(alpha = 0.5f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    MacroIndicator(
                        label = if (isSlovak) "Bielkoviny" else "Bílkoviny",
                        value = "${proteinToday}g",
                        color = SoftGreenGlow,
                        percentage = if (proteinToday > 0) proteinToday.toFloat() / 120f else 0f
                    )

                    MacroIndicator(
                        label = if (isSlovak) "Sacharidy" else "Sacharidy",
                        value = "${carbsToday}g",
                        color = SaffronGoldSecondary,
                        percentage = if (carbsToday > 0) carbsToday.toFloat() / 250f else 0f
                    )

                    MacroIndicator(
                        label = if (isSlovak) "Tuky" else "Tuky",
                        value = "${fatToday}g",
                        color = TomatoRedTertiary,
                        percentage = if (fatToday > 0) fatToday.toFloat() / 80f else 0f
                    )

                    Button(
                        onClick = { showGoalDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = FreshGreenPrimary.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(34.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = "Edit Goals", tint = CreamText, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            if (isSlovak) "Nastaviť cieľ" else "Nastavit cíl",
                            color = CreamText,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // 4. Recent Meals Habits Carousel (Predictive Meals logging based on history)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isSlovak) "Často konzumované (Habit)" else "Často konzumované (Habit)",
                    color = CreamText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = if (isSlovak) "Podľa času dňa ⚡" else "Podle času dne ⚡",
                    color = SaffronGoldSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Quick predictive Logging Carousel based on time-of-day
            Text(
                text = if (isSlovak) "Navrhované habit jedlá k času ($localizedType):" else "Navrhovaná habit jídla k času ($localizedType):",
                color = CreamText.copy(alpha = 0.6f),
                fontSize = 12.sp
            )

            // Horizontal Carousel mapping localized past meals patterns
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val mockRecentMeals = getMockHabits(suggestedType, isSlovak)
                mockRecentMeals.forEach { meal ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, BorderNatural),
                        modifier = Modifier
                            .width(170.dp)
                            .clickable {
                                viewModel.logMealDirect(
                                    meal.name,
                                    meal.calories,
                                    meal.carbs,
                                    meal.protein,
                                    meal.fat,
                                    suggestedType
                                )
                            }
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .background(
                                            FreshGreenPrimary.copy(alpha = 0.2f),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.RestaurantMenu,
                                        contentDescription = "Meal",
                                        tint = SaffronGoldSecondary,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                                Text(
                                    text = "${meal.calories} kcal",
                                    color = SaffronGoldSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = meal.name,
                                color = CreamText,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 2,
                                modifier = Modifier.height(36.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "B:${meal.protein}g S:${meal.carbs}g T:${meal.fat}g",
                                color = CreamText.copy(alpha = 0.5f),
                                fontSize = 10.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(FreshGreenPrimary, RoundedCornerShape(6.dp))
                                    .padding(vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (isSlovak) "+ Zapísať" else "+ Zapsat",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Add Custom manual meal log button
                Card(
                    colors = CardDefaults.cardColors(containerColor = FreshGreenPrimary.copy(alpha = 0.08f)),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, BorderNatural),
                    modifier = Modifier
                        .width(120.dp)
                        .fillMaxHeight()
                        .clickable { showFastMealDialog = true }
                ) {
                    Column(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add ready meal",
                            tint = SoftGreenGlow,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isSlovak) "Hotové jedlo" else "Hotové jídlo",
                            color = CreamText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // 5. Family Pairing / Rodinná lednice
        Card(
            colors = CardDefaults.cardColors(containerColor = SoftGreenGlow.copy(alpha = 0.2f)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showFamilyPairingDialog = true }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Group,
                        contentDescription = "Family Pairing",
                        tint = FreshGreenPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (isSlovak) "Rodinná chladnička" else "Rodinná lednice",
                            color = CreamText,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = if (isSlovak) "Pripojte rodinu a zdieľajte zásoby" else "Propojte rodinu a sdílejte zásoby",
                            color = CreamText.copy(alpha = 0.6f),
                            fontSize = 12.sp
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Default.QrCodeScanner,
                    contentDescription = "Scan QR",
                    tint = FreshGreenPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // 6. Syncing Metadata Panel: Android Health Connect + Google Cloud Backup
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurface.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Sync,
                            contentDescription = "Sync",
                            tint = SoftGreenGlow,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isSlovak) "Synchronizácia a záloha" else "Synchronizace a záloha",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                    Text(
                        text = if (healthConnectSynced) "Aktivní / Aktívne" else "Nepřipojeno / Nepripojené",
                        color = SafeGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Divider(color = Color.White.copy(alpha = 0.08f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Health Connect (HealthKit sync):",
                            color = CreamText.copy(alpha = 0.6f),
                            fontSize = 11.sp
                        )
                        Text(
                            text = if (isSlovak) "Aplikácia odovzdáva kalorický príjem." else "Aplikace odesílá kalorický příjem.",
                            color = CreamText,
                            fontSize = 12.sp
                        )
                    }
                    Button(
                        onClick = { viewModel.syncWithHealthConnect() },
                        colors = ButtonDefaults.buttonColors(containerColor = FreshGreenPrimary),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text(if (isSlovak) "Zosynchronizovať" else "Synchronizovat", fontSize = 10.sp)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Cloud Backup Status:",
                        color = CreamText.copy(alpha = 0.6f),
                        fontSize = 11.sp
                    )
                    Text(
                        text = backupStatus,
                        color = SaffronGoldSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Divider(color = Color.White.copy(alpha = 0.08f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                        Text(
                            text = if (isSlovak) "Resetovať aplikáciu a začať od nuly:" else "Resetovat aplikaci a začít od nuly:",
                            color = CreamText.copy(alpha = 0.6f),
                            fontSize = 11.sp
                        )
                        Text(
                            text = if (isSlovak) "Vymaže všetky zásoby a vráti vás na registráciu." else "Vymaže veškeré zásoby a vrátí vás na registraci.",
                            color = CreamText,
                            fontSize = 12.sp
                        )
                    }
                    Button(
                        onClick = { viewModel.logoutUser() },
                        colors = ButtonDefaults.buttonColors(containerColor = TomatoRedTertiary),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text(if (isSlovak) "Začať od nuly" else "Začít od nuly", fontSize = 10.sp, color = Color.White)
                    }
                }
            }
        }

        // --- Goal Setting Dialog ---
        if (showGoalDialog) {
            Dialog(onDismissRequest = { showGoalDialog = false }) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, BorderNatural),
                    modifier = Modifier.padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (isSlovak) "Kalorický Cieľ" else "Kalorický Cíl",
                            color = CreamText,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isSlovak) "Nastavte si limit pre automatického FridgeBuddy strážcu." else "Nastavte si limit pro automatického FridgeBuddy hlídače.",
                            color = CreamText.copy(alpha = 0.7f),
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )

                        var sliderVal by remember { mutableFloatStateOf(calorieGoal.toFloat()) }
                        Text(
                            text = "${sliderVal.toInt()} kcal",
                            color = SaffronGoldSecondary,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Slider(
                            value = sliderVal,
                            onValueChange = { sliderVal = it },
                            valueRange = 1200f..4000f,
                            colors = SliderDefaults.colors(
                                thumbColor = SaffronGoldSecondary,
                                activeTrackColor = FreshGreenPrimary
                            )
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { showGoalDialog = false },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Zrušit", color = CreamText)
                            }
                            Button(
                                onClick = {
                                    viewModel.setDailyCalorieGoal(sliderVal.toInt())
                                    showGoalDialog = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = FreshGreenPrimary),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Uložit")
                            }
                        }
                    }
                }
            }
        }

        // --- Custom Fast Meal Intake Dialog ---
        if (showFastMealDialog) {
            Dialog(onDismissRequest = { showFastMealDialog = false }) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, BorderNatural),
                    modifier = Modifier.padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = if (isSlovak) "Zapísať vlastnú stravu" else "Zapsat vlastní stravu",
                            color = CreamText,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )

                        OutlinedTextField(
                            value = fastMealInput,
                            onValueChange = { fastMealInput = it },
                            label = { Text(if (isSlovak) "Názov jedla (napr. Svíčková)" else "Název jídla (např. Svíčková)") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = FreshGreenPrimary,
                                focusedTextColor = CreamText,
                                unfocusedTextColor = CreamText
                            )
                        )

                        var tempCal by remember { mutableStateOf("350") }
                        var tempCarbs by remember { mutableStateOf("40") }
                        var tempProt by remember { mutableStateOf("15") }
                        var tempFat by remember { mutableStateOf("10") }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = tempCal,
                                onValueChange = { tempCal = it },
                                label = { Text("kcal") },
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = CreamText)
                            )
                            OutlinedTextField(
                                value = tempProt,
                                onValueChange = { tempProt = it },
                                label = { Text("B (g)") },
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = CreamText)
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = tempCarbs,
                                onValueChange = { tempCarbs = it },
                                label = { Text("S (g)") },
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = CreamText)
                            )
                            OutlinedTextField(
                                value = tempFat,
                                onValueChange = { tempFat = it },
                                label = { Text("T (g)") },
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = CreamText)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { showFastMealDialog = false },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Storno", color = CreamText)
                            }
                            Button(
                                onClick = {
                                    if (fastMealInput.isNotBlank()) {
                                        viewModel.logMealDirect(
                                            fastMealInput,
                                            tempCal.toIntOrNull() ?: 0,
                                            tempCarbs.toIntOrNull() ?: 0,
                                            tempProt.toIntOrNull() ?: 0,
                                            tempFat.toIntOrNull() ?: 0,
                                            suggestedType
                                        )
                                        fastMealInput = ""
                                        showFastMealDialog = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = FreshGreenPrimary),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Přidat")
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Divider(color = BorderNatural, thickness = 1.dp)
            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "NutriKalk v1.0",
                    color = CreamText.copy(alpha = 0.4f),
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { showAboutDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = SoftGreenGlow, contentColor = FreshGreenPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("O StepIn Tech", fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        if (showAboutDialog) {
            Dialog(onDismissRequest = { showAboutDialog = false }) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = DarkSurface,
                    border = BorderStroke(1.dp, BorderNatural)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "O StepIn Tech",
                            color = CreamText,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Ahoj! Jmenuji se Štěpán a NutriKalk vznikl z jednoho náhodného nápadu během mého studia programování. Chtěl jsem vytvořit něco, co bude lidem reálně pomáhat. Tak vzniklo mé studio StepIn Tech – název v sobě skrývá nejen kus mého jména, ale i symbolický „vstup“ do světa chytrých technologií.",
                            color = CreamText.copy(alpha = 0.8f),
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Děkuji, že NutriKalk používáš! Aplikaci neustále posouvám dál, takže s radostí a vděčností uvítám jakoukoliv zpětnou vazbu, nápady na zlepšení nebo hlášení chyb.",
                            color = CreamText.copy(alpha = 0.8f),
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "✉️ Napiš mi na: stepintech.cz@gmail.com",
                            color = FreshGreenPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { showAboutDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = FreshGreenPrimary)
                        ) {
                            Text("Zavřít", color = DarkBg)
                        }
                    }
                }
            }
        }
        
        if (showFamilyPairingDialog) {
            Dialog(onDismissRequest = { showFamilyPairingDialog = false }) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = DarkSurface,
                    modifier = Modifier.fillMaxSize(0.9f)
                ) {
                    FamilyPairingScreen(
                        viewModel = viewModel,
                        householdManager = householdManager,
                        onPairingSuccess = { showFamilyPairingDialog = false },
                        onCancel = { showFamilyPairingDialog = false }
                    )
                }
            }
        }
    }
}

@Composable
fun MacroIndicator(
    label: String,
    value: String,
    color: Color,
    percentage: Float
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, color = CreamText.copy(alpha = 0.8f), fontSize = 12.sp)
            Text(value, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = Math.min(percentage, 1f),
            color = color,
            trackColor = Color.White.copy(alpha = 0.08f),
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
        )
    }
}

// Model for past meal habit shortcuts
data class HabitMeal(
    val name: String,
    val calories: Int,
    val protein: Int,
    val carbs: Int,
    val fat: Int
)

fun getMockHabits(type: String, isSlovak: Boolean): List<HabitMeal> {
    return when (type) {
        "Snídaně" -> if (isSlovak) {
            listOf(
                HabitMeal("Chlieb s džemom a maslom", 290, 8, 48, 10),
                HabitMeal("Mliečna krupicová kaša", 320, 12, 54, 7),
                HabitMeal("Vajcia natvrdo s syrom", 260, 21, 2, 18)
            )
        } else {
            listOf(
                HabitMeal("Chléb s džemem a máslem", 290, 8, 48, 10),
                HabitMeal("Mléčná krupicová kaše", 320, 12, 54, 7),
                HabitMeal("Míchaná vejce na másle", 270, 16, 2, 22)
            )
        }
        "Oběd" -> if (isSlovak) {
            listOf(
                HabitMeal("Slovenský zemiakový šalát", 440, 10, 48, 22),
                HabitMeal("Zapekané kuracie prsia", 390, 36, 10, 20),
                HabitMeal("Paradajková polievka", 210, 6, 28, 8)
            )
        } else {
            listOf(
                HabitMeal("Svíčková omáčka s knedlíkem", 650, 28, 85, 25),
                HabitMeal("Pečené kuřecí stehno", 420, 38, 5, 26),
                HabitMeal("Bramboračka s houbami", 185, 5, 24, 7)
            )
        }
        "Svačina" -> if (isSlovak) {
            listOf(
                HabitMeal("Kyslé mlieko s pečivom", 240, 11, 32, 6),
                HabitMeal("Čerstvé jablko a vlašské orechy", 190, 4, 18, 12)
            )
        } else {
            listOf(
                HabitMeal("Bílý jogurt s medem", 160, 9, 20, 5),
                HabitMeal("Makový závin", 280, 6, 46, 9)
            )
        }
        else -> if (isSlovak) {
            listOf(
                HabitMeal("Obložený syrový tanier", 350, 25, 2, 28),
                HabitMeal("Pečené rybie filé", 220, 24, 4, 12),
                HabitMeal("Zeleninová polievka s krupicou", 140, 4, 18, 5)
            )
        } else {
            listOf(
                HabitMeal("Chléb Šumava se sýrem", 320, 16, 38, 11),
                HabitMeal("Párky s hořčicí", 380, 14, 4, 34),
                HabitMeal("Šopský salát s balkánem", 210, 8, 12, 15)
            )
        }
    }
}
