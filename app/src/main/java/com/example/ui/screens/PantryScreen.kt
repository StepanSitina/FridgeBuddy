package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.data.PantryItem
import com.example.ui.theme.*
import com.example.ui.viewmodel.FridgeBuddyViewModel
import java.text.SimpleDateFormat
import java.util.*
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.ui.viewinterop.AndroidView
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import androidx.compose.ui.platform.LocalLifecycleOwner
import java.util.concurrent.Executors
import kotlinx.coroutines.launch

@Composable
fun PantryScreen(
    viewModel: FridgeBuddyViewModel,
    modifier: Modifier = Modifier,
    onNavigateToTab: ((String) -> Unit)? = null
) {
    val isSlovak by viewModel.isSlovak.collectAsState()
    val pantryItems by viewModel.pantryItems.collectAsState()
    val isAiLoading by viewModel.isAiLoading.collectAsState()
    val scope = rememberCoroutineScope()

    // EAN Barcode Scanner View States
    var showBarcodeScanner by remember { mutableStateOf(false) }
    var typedBarcode by remember { mutableStateOf("") }
    var isSearchLoading by remember { mutableStateOf(false) }
    var scanErrorText by remember { mutableStateOf<String?>(null) }

    var activePantryTab by remember { mutableStateOf("Všetko") } // Všetko, Lednice/Chladnička, Mrazák, Spižírna
    var showAddItemDialog by remember { mutableStateOf(false) }

    // Forms
    var manualName by remember { mutableStateOf("") }
    var manualQty by remember { mutableStateOf("1 ks") }
    var manualCat by remember { mutableStateOf("Fridge") }
    var manualExpDays by remember { mutableStateOf("5") }
    var manualPrice by remember { mutableStateOf("29.0") }

    // Direct Camera Scanner State
    var showCameraSimulator by remember { mutableStateOf(false) }

    val context = LocalContext.current

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted1 ->
        if (isGranted1) {
            showBarcodeScanner = true
        }
    }
    
    // Currently Scanned Food Details
    var scannedFoodName by remember { mutableStateOf("") }
    var scannedFoodBrand by remember { mutableStateOf<String?>(null) }
    var scannedFoodCalories by remember { mutableStateOf(100) }
    var scannedFoodProtein by remember { mutableStateOf("5g") }
    var scannedFoodCarbs by remember { mutableStateOf("12g") }
    var scannedFoodFat by remember { mutableStateOf("2g") }
    var scannedFoodCategory by remember { mutableStateOf("Fridge") }
    var scannedFoodPrice by remember { mutableStateOf(35.0) }
    var scannedFoodEmoji by remember { mutableStateOf("🍏") }
    
    var showScanResultDialog by remember { mutableStateOf(false) }
    var isCameraCountingDown by remember { mutableStateOf(false) }

    // Multi-Language Strings
    val titleText = if (isSlovak) "Moja Spíž & Chladnička" else "Moje Spižírna & Lednice"
    val categoryLabelFridge = if (isSlovak) "Chladnička" else "Lednice"
    val categoryLabelFreezer = if (isSlovak) "Mrazák" else "Mrazák"
    val categoryLabelPantry = if (isSlovak) "Spižírna" else "Spižírna"
    val tabAllLabel = if (isSlovak) "Všetko" else "Vše"

    val headerDesc = if (isSlovak) "Inteligentná správa potravín, redukcia odpadu a vizuálna expirácia." else "Inteligentní správa potravin, redukce odpadu a vizuální expirace."

    // Filter items based on selected tab selector
    val filteredPantryItems = pantryItems.filter {
        when (activePantryTab) {
            "Fridge", categoryLabelFridge -> it.category == "Fridge"
            "Freezer", categoryLabelFreezer -> it.category == "Freezer"
            "Pantry", categoryLabelPantry -> it.category == "Pantry"
            else -> true
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
        // 1. Cozy header banner image with transparent overlay
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_pantry_banner),
                contentDescription = "Pantry background",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            // dark filter overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(FreshGreenPrimary.copy(alpha = 0.5f))
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Bottom
            ) {
                Text(
                    text = titleText,
                    color = CreamText,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = headerDesc,
                    color = CreamText.copy(alpha = 0.8f),
                    fontSize = 12.sp
                )
            }
        }

        // 2. Intelligent inputs trigger row (Receipt Scanning, Voice Dictation, Camera)
        Card(
            colors = CardDefaults.cardColors(containerColor = NavBgNatural),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, BorderNatural),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = if (isSlovak) "✨ Rýchli AI Pomocníci" else "✨ Rychlí AI Pomocníci",
                    color = FreshGreenPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                
                Card(
                    colors = CardDefaults.cardColors(containerColor = SaffronGoldSecondary.copy(alpha = 0.12f)),
                    border = BorderStroke(1.dp, SaffronGoldSecondary.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToTab?.invoke("scanner") }
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.DocumentScanner,
                            contentDescription = "Real OCR Skener",
                            tint = SaffronGoldSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isSlovak) "Vyskúšajte Možnosť A" else "Vyzkoušejte Možnost A",
                                color = SaffronGoldSecondary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Text(
                                text = if (isSlovak) "Reálny skener s OCR na automatické rozpoznávanie nákupov!" else "Reálný skener s OCR pro automatické rozpoznávání nákupů!",
                                color = CreamText.copy(alpha = 0.82f),
                                fontSize = 11.sp,
                                lineHeight = 14.sp
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Forward",
                            tint = SaffronGoldSecondary.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                
                // Camera Scanner Button (extremely visible & prominent)
                IconButtonCard(
                    onClick = {
                        val isCameraPermissionGranted = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED

                        if (isCameraPermissionGranted) {
                            showBarcodeScanner = true
                        } else {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    },
                    icon = Icons.Default.QrCodeScanner,
                    label = if (isSlovak) "EAN Skener čiarových kódov" else "EAN Skener čárových kódů",
                    modifier = Modifier.fillMaxWidth().testTag("button_camera_scanner")
                )
            }
        }



        // 3. Category Filter tabs (All, Fridge, Freezer, Pantry)
        TabRow(
            selectedTabIndex = when (activePantryTab) {
                tabAllLabel -> 0
                categoryLabelFridge -> 1
                categoryLabelFreezer -> 2
                categoryLabelPantry -> 3
                else -> 0
            },
            containerColor = NavBgNatural,
            contentColor = FreshGreenPrimary,
            divider = { Divider(color = BorderNatural) }
        ) {
            val tabs = listOf(tabAllLabel, categoryLabelFridge, categoryLabelFreezer, categoryLabelPantry)
            tabs.forEach { tab ->
                Tab(
                    selected = activePantryTab == tab,
                    onClick = { activePantryTab = tab },
                    text = { Text(tab, fontWeight = FontWeight.SemiBold, fontSize = 12.sp) },
                    selectedContentColor = FreshGreenPrimary,
                    unselectedContentColor = SaffronGoldSecondary
                )
            }
        }

        // 4. Pantry lazy column list with vertical green/orange/red traffic border
        if (filteredPantryItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        Icons.Default.Inbox,
                        contentDescription = "Empty",
                        tint = CreamText.copy(alpha = 0.2f),
                        modifier = Modifier.size(64.dp)
                    )
                    Text(
                        text = if (isSlovak) "Žiadne sedia v tejto sekcii" else "Žádné potraviny v této sekci",
                        color = CreamText.copy(alpha = 0.5f),
                        fontSize = 14.sp
                    )
                    Text(
                        text = if (isSlovak) "Pridajte položku manuálne alebo naskenujte účtenku." else "Přidejte položku manuálně nebo naskenujte účtenku.",
                        color = CreamText.copy(alpha = 0.3f),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredPantryItems) { item ->
                    PantryItemCard(
                        item = item,
                        onDelete = { viewModel.deleteItem(item) },
                        isSlovak = isSlovak
                    )
                }
            }
        }

        } // Close primary scrollable/structural Column content

        // 5. FAB Floating Action Button to add item manually (floats cleanly above list)
        FloatingActionButton(
            onClick = { showAddItemDialog = true },
            containerColor = SaffronGoldSecondary,
            contentColor = Color.Black,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("add_pantry_fab")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Manual add")
        }

        // --- Manual Add Item Dialog Dialog ---
        if (showAddItemDialog) {
            Dialog(onDismissRequest = { showAddItemDialog = false }) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = if (isSlovak) "Pridať potravinu manuálne" else "Přidat potravinu manuálně",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )

                        OutlinedTextField(
                            value = manualName,
                            onValueChange = { manualName = it },
                            label = { Text(if (isSlovak) "Názov (napr. Maslo)" else "Název (např. Máslo)") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = manualQty,
                            onValueChange = { manualQty = it },
                            label = { Text(if (isSlovak) "Množstvo (napr. 250g)" else "Množství (např. 250g)") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text(
                            text = if (isSlovak) "Umiestnenie (Kategória):" else "Umístění (Kategorie):",
                            color = CreamText,
                            fontSize = 12.sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val cats = listOf("Fridge", "Freezer", "Pantry")
                            cats.forEach { c ->
                                val label = when (c) {
                                    "Fridge" -> categoryLabelFridge
                                    "Freezer" -> categoryLabelFreezer
                                    else -> categoryLabelPantry
                                }
                                Button(
                                    onClick = { manualCat = c },
                                    colors = ButtonDefaults.buttonColors(containerColor = if (manualCat == c) FreshGreenPrimary else DarkBg),
                                    modifier = Modifier.weight(1f).height(32.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text(label, fontSize = 10.sp)
                                }
                            }
                        }

                        OutlinedTextField(
                            value = manualExpDays,
                            onValueChange = { manualExpDays = it },
                            label = { Text(if (isSlovak) "Dni expirácie odteraz" else "Dny expirace odteď") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = manualPrice,
                            onValueChange = { manualPrice = it },
                            label = { Text(if (isSlovak) "Odhad ceny" else "Odhad ceny") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { showAddItemDialog = false },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Zrušit", color = CreamText)
                            }
                            Button(
                                onClick = {
                                    if (manualName.isNotBlank()) {
                                        viewModel.addManualPantryItem(
                                            manualName,
                                            manualQty,
                                            manualCat,
                                            manualExpDays.toIntOrNull() ?: 5,
                                            manualPrice.toDoubleOrNull() ?: 0.0
                                        )
                                        manualName = ""
                                        showAddItemDialog = false
                                    }
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

        // --- Interactive Camera Food Scanner Dialog ---
        if (showBarcodeScanner) {
            Dialog(onDismissRequest = { showBarcodeScanner = false }) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = LightSurface),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, BorderNatural),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(18.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = if (isSlovak) "📸 Inteligentný Skener EAN" else "📸 Inteligentní Skener EAN",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = FreshGreenPrimary
                        )

                        // Mode Selector Tab (Camera vs. Manual)
                        var scannerMode by remember { mutableStateOf("Camera") } // "Camera" or "Manual"

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkBg),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .background(if (scannerMode == "Camera") FreshGreenPrimary else Color.Transparent)
                                    .clickable { scannerMode = "Camera" },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (isSlovak) "Kamera" else "Kamera",
                                    color = if (scannerMode == "Camera") Color.Black else CreamText,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .background(if (scannerMode == "Manual") FreshGreenPrimary else Color.Transparent)
                                    .clickable { scannerMode = "Manual" },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (isSlovak) "Ručné zadanie" else "Ruční zadání",
                                    color = if (scannerMode == "Manual") Color.Black else CreamText,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }

                        if (scannerMode == "Camera") {
                            // Camera preview with live barcode scanning!
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF1E231C)),
                                contentAlignment = Alignment.Center
                            ) {
                                BarcodeScannerView(
                                    onBarcodeScanned = { barcode ->
                                        if (!isSearchLoading) {
                                            isSearchLoading = true
                                            scope.launch {
                                                try {
                                                    val result = viewModel.fetchBarcodeProduct(barcode)
                                                    if (result != null) {
                                                        scannedFoodName = result.name
                                                        scannedFoodBrand = result.brand
                                                        scannedFoodCalories = result.calories
                                                        scannedFoodProtein = "${result.protein}g"
                                                        scannedFoodCarbs = "${result.carbohydrates}g"
                                                        scannedFoodFat = "${result.fat}g"
                                                        scannedFoodCategory = result.category
                                                        scannedFoodPrice = when (result.category) {
                                                            "Pantry" -> 35.0
                                                            "Freezer" -> 69.0
                                                            else -> 29.0
                                                        }
                                                        scannedFoodEmoji = when {
                                                            result.name.lowercase().contains("mléko") || result.name.lowercase().contains("mlieko") -> "🥛"
                                                            result.name.lowercase().contains("sýr") || result.name.lowercase().contains("syr") || result.name.lowercase().contains("eidam") || result.name.lowercase().contains("bryndza") || result.name.lowercase().contains("hermelín") -> "🧀"
                                                            result.name.lowercase().contains("párky") || result.name.lowercase().contains("šunka") || result.name.lowercase().contains("maso") || result.name.lowercase().contains("salám") -> "🥩"
                                                            result.name.lowercase().contains("chléb") || result.name.lowercase().contains("chlieb") || result.name.lowercase().contains("toast") || result.name.lowercase().contains("mouka") || result.name.lowercase().contains("múka") -> "🍞"
                                                            result.name.lowercase().contains("pizza") -> "🍕"
                                                            result.name.lowercase().contains("ryb") || result.name.lowercase().contains("ryby") -> "🐟"
                                                            result.name.lowercase().contains("tvaroh") -> "🍨"
                                                            result.name.lowercase().contains("kefír") || result.name.lowercase().contains("kefir") || result.name.lowercase().contains("jogurt") -> "🥛"
                                                            else -> "🥫"
                                                        }
                                                        showBarcodeScanner = false
                                                        showScanResultDialog = true
                                                    } else {
                                                        scanErrorText = if (isSlovak) "Kód $barcode nebol v databázi nájdený." else "Kód $barcode nebyl v databázi nalezen."
                                                    }
                                                } catch (e: Exception) {
                                                    scanErrorText = "Chyba: ${e.message}"
                                                } finally {
                                                    isSearchLoading = false
                                                }
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxSize()
                                )

                                // Viewfinder frame overlay
                                Box(
                                    modifier = Modifier
                                        .size(150.dp, 100.dp)
                                        .border(BorderStroke(2.dp, SoftGreenGlow), RoundedCornerShape(8.dp))
                                )

                                // Real-time pulse line
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(FreshGreenPrimary)
                                )
                            }

                            Text(
                                text = if (isSlovak) "Namierte fotoaparát na čiarový kód (EAN) produktu." else "Namiřte fotoaparát na čárový kód (EAN) produktu.",
                                fontSize = 11.sp,
                                color = CaptionTextNatural,
                                textAlign = TextAlign.Center
                            )
                        } else {
                            // Manual Input with fallback buttons!
                            OutlinedTextField(
                                value = typedBarcode,
                                onValueChange = { typedBarcode = it.filter { char -> char.isDigit() } },
                                label = { Text("EAN", color = SaffronGoldSecondary) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = FreshGreenPrimary,
                                    unfocusedBorderColor = BorderNatural,
                                    focusedTextColor = CreamText,
                                    unfocusedTextColor = CreamText,
                                    focusedContainerColor = DarkSurface,
                                    unfocusedContainerColor = DarkSurface
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Button(
                                onClick = {
                                    if (typedBarcode.isNotBlank()) {
                                        isSearchLoading = true
                                        scope.launch {
                                            try {
                                                val result = viewModel.fetchBarcodeProduct(typedBarcode)
                                                if (result != null) {
                                                    scannedFoodName = result.name
                                                    scannedFoodBrand = result.brand
                                                    scannedFoodCalories = result.calories
                                                    scannedFoodProtein = "${result.protein}g"
                                                    scannedFoodCarbs = "${result.carbohydrates}g"
                                                    scannedFoodFat = "${result.fat}g"
                                                    scannedFoodCategory = result.category
                                                    scannedFoodPrice = when (result.category) {
                                                        "Pantry" -> 35.0
                                                        "Freezer" -> 69.0
                                                        else -> 29.0
                                                    }
                                                    scannedFoodEmoji = when {
                                                        result.name.lowercase().contains("mléko") || result.name.lowercase().contains("mlieko") -> "🥛"
                                                        result.name.lowercase().contains("sýr") || result.name.lowercase().contains("syr") || result.name.lowercase().contains("eidam") || result.name.lowercase().contains("bryndza") || result.name.lowercase().contains("hermelín") -> "🧀"
                                                        result.name.lowercase().contains("párky") || result.name.lowercase().contains("šunka") || result.name.lowercase().contains("maso") || result.name.lowercase().contains("salám") -> "🥩"
                                                        result.name.lowercase().contains("chléb") || result.name.lowercase().contains("chlieb") || result.name.lowercase().contains("toast") || result.name.lowercase().contains("mouka") || result.name.lowercase().contains("múka") -> "🍞"
                                                        result.name.lowercase().contains("pizza") -> "🍕"
                                                        result.name.lowercase().contains("ryb") || result.name.lowercase().contains("ryby") -> "🐟"
                                                        result.name.lowercase().contains("tvaroh") -> "🍨"
                                                        result.name.lowercase().contains("kefír") || result.name.lowercase().contains("kefir") || result.name.lowercase().contains("jogurt") -> "🥛"
                                                        else -> "🥫"
                                                    }
                                                    showBarcodeScanner = false
                                                    showScanResultDialog = true
                                                } else {
                                                    scanErrorText = if (isSlovak) "EAN $typedBarcode nebol v databázi nájdený." else "EAN $typedBarcode nebyl v databázi nalezen."
                                                }
                                            } catch (e: Exception) {
                                                scanErrorText = "Chyba: ${e.message}"
                                            } finally {
                                                isSearchLoading = false
                                            }
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = FreshGreenPrimary),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(if (isSlovak) "Vyhľadať produkt" else "Vyhledat produkt", color = Color.White)
                            }

                            // Presets for quick desktop click testing!
                            Text(
                                text = if (isSlovak) "Pre rýchle vyskúšanie kliknite:" else "Pro rychlé vyzkoušení klikněte:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = CreamText,
                                modifier = Modifier.align(Alignment.Start)
                            )

                            val demoEans = listOf(
                                "8594008124117" to "Mléko",
                                "8594008124032" to "Párky",
                                "8594008124230" to "Hermelín",
                                "8594008124261" to "Toastový chléb",
                                "8594008124445" to "Slovenská Bryndza",
                                "8594008124421" to "Pizza"
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                demoEans.forEach { (ean, label) ->
                                    Card(
                                        modifier = Modifier
                                            .clickable { typedBarcode = ean },
                                        colors = CardDefaults.cardColors(containerColor = DarkBg),
                                        border = BorderStroke(1.dp, BorderNatural)
                                    ) {
                                        Text(
                                            text = label,
                                            fontSize = 11.sp,
                                            color = CreamText,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                        }

                        if (isSearchLoading) {
                            CircularProgressIndicator(color = SoftGreenGlow)
                        }

                        scanErrorText?.let { err ->
                            Text(
                                text = "⚠️ $err",
                                color = Color(0xFFCF6679),
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center
                            )
                        }

                        Button(
                            onClick = { showBarcodeScanner = false },
                            colors = ButtonDefaults.buttonColors(containerColor = SaffronGoldSecondary),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (isSlovak) "Zavrieť" else "Zavřít", color = Color.White)
                        }
                    }
                }
            }
        }

        // --- Scanning Result Analysis Sheet Dialog ---
        if (showScanResultDialog) {
            Dialog(onDismissRequest = { showScanResultDialog = false }) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = LightSurface),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, BorderNatural),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .testTag("scan_result_dialog")
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = scannedFoodEmoji,
                            fontSize = 54.sp
                        )

                        Text(
                            text = scannedFoodName,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = FreshGreenPrimary
                        )

                        scannedFoodBrand?.takeIf { it.isNotBlank() }?.let { brand ->
                            Box(
                                modifier = Modifier
                                    .background(SaffronGoldSecondary.copy(alpha = 0.14f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = (if (isSlovak) "Značka: " else "Značka: ") + brand,
                                    color = SaffronGoldSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .background(SoftGreenGlow, RoundedCornerShape(12.dp))
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "🔥 $scannedFoodCalories kcal",
                                fontWeight = FontWeight.Bold,
                                color = FreshGreenPrimary,
                                fontSize = 16.sp
                            )
                        }

                        HorizontalDivider(color = BorderNatural)

                        // Nutritional macros info
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(if (isSlovak) "Bielkoviny" else "Bílkoviny", fontSize = 11.sp, color = CaptionTextNatural)
                                Text(scannedFoodProtein, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = CreamText)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Sacharidy", fontSize = 11.sp, color = CaptionTextNatural)
                                Text(scannedFoodCarbs, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = CreamText)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Tuky", fontSize = 11.sp, color = CaptionTextNatural)
                                Text(scannedFoodFat, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = CreamText)
                            }
                        }

                        HorizontalDivider(color = BorderNatural)

                        Button(
                            onClick = {
                                val storedName = scannedFoodBrand
                                    ?.takeIf { it.isNotBlank() && !scannedFoodName.contains(it, ignoreCase = true) }
                                    ?.let { "$scannedFoodName ($it)" }
                                    ?: scannedFoodName
                                viewModel.addManualPantryItem(
                                    storedName,
                                    "1 ks",
                                    scannedFoodCategory,
                                    7,
                                    scannedFoodPrice
                                )
                                showScanResultDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = FreshGreenPrimary),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (isSlovak) "Pridat do zásob" else "Přidat do zásob", color = Color.White)
                        }

                        OutlinedButton(
                            onClick = { showScanResultDialog = false },
                            border = BorderStroke(1.dp, BorderNatural),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (isSlovak) "Zrušiť" else "Zrušit", color = CreamText)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PantryItemCard(
    item: PantryItem,
    onDelete: () -> Unit,
    isSlovak: Boolean
) {
    val daysLeft = ((item.expirationTimestamp - System.currentTimeMillis()) / (24 * 60 * 60 * 1000L)).toInt()
    
    // Traffic light warning bar logic
    val (statusColor, statusLabel) = when {
        daysLeft < 0 -> Pair(TomatoRedTertiary, if (isSlovak) "Expirovalo!" else "Prošlé!")
        daysLeft == 0 -> Pair(TomatoRedTertiary, if (isSlovak) "Dnes expiruje!" else "Dnes expiruje!")
        daysLeft == 1 -> Pair(ExpiringOrange, if (isSlovak) "Expiruje zajtra!" else "Expiruje zítra!")
        daysLeft <= 3 -> Pair(SaffronGoldSecondary, if (isSlovak) "Expiruje do $daysLeft dní" else "Expiruje do $daysLeft dnů")
        else -> Pair(SafeGreen, if (isSlovak) "Čerstvé - $daysLeft dní" else "Čerstvé - $daysLeft dnů")
    }

    val currency = if (isSlovak) "EUR" else "Kč"

    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, BorderNatural),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Traffic Light color coding bar
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .align(Alignment.CenterVertically)
                    .background(statusColor)
                    .height(80.dp) // standard height block
            )

            Row(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = item.name,
                            color = CreamText,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .background(FreshGreenPrimary.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = item.quantityHint,
                                color = FreshGreenPrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Category pill
                        Box(
                            modifier = Modifier
                                .background(BorderNatural, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = when (item.category) {
                                    "Fridge" -> if (isSlovak) "V Chladničke" else "V Lednici"
                                    "Freezer" -> if (isSlovak) "V Mrazáku" else "V Mrazáku"
                                    else -> if (isSlovak) "V Spíži" else "Ve Spižírně"
                                },
                                color = CreamText.copy(alpha = 0.6f),
                                fontSize = 10.sp
                            )
                        }

                        // Price approximation if logged
                        if (item.approxPrice > 0.0) {
                            Text(
                                text = "~ ${item.approxPrice} $currency",
                                color = SaffronGoldSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = statusLabel,
                        color = statusColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete Item",
                        tint = CreamText.copy(alpha = 0.3f)
                    )
                }
            }
        }
    }
}

@Composable
fun IconButtonCard(
    onClick: () -> Unit,
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable { onClick() }
            .height(86.dp),
        colors = CardDefaults.cardColors(containerColor = LightSurface),
        border = BorderStroke(1.dp, BorderNatural),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = FreshGreenPrimary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = CreamText,
                textAlign = TextAlign.Center,
                lineHeight = 13.sp
            )
        }
    }
}

@Composable
fun BarcodeScannerView(
    onBarcodeScanned: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val barcodeScanner = BarcodeScanning.getClient()
                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()

                val executor = Executors.newSingleThreadExecutor()
                imageAnalysis.setAnalyzer(executor) { imageProxy ->
                    @androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
                    val mediaImage = imageProxy.image
                    if (mediaImage != null) {
                        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                        barcodeScanner.process(image)
                            .addOnSuccessListener { barcodes ->
                                for (barcode in barcodes) {
                                    val rawValue = barcode.rawValue
                                    if (rawValue != null && rawValue.isNotBlank()) {
                                        onBarcodeScanned(rawValue)
                                        break
                                    }
                                }
                            }
                            .addOnFailureListener {
                                // Handle failure
                            }
                            .addOnCompleteListener {
                                imageProxy.close()
                            }
                    } else {
                        imageProxy.close()
                    }
                }

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageAnalysis
                    )
                } catch (exc: Exception) {
                    exc.printStackTrace()
                }
            }, ContextCompat.getMainExecutor(ctx))

            previewView
        },
        modifier = modifier
    )
}
