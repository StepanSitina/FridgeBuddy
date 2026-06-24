package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PantryScreen(
    viewModel: FridgeBuddyViewModel,
    modifier: Modifier = Modifier,
    onNavigateToTab: ((String) -> Unit)? = null
) {
    val isSlovak by viewModel.isSlovak.collectAsState()
    val pantryItems by viewModel.pantryItems.collectAsState()
    val scanHistory by viewModel.scanHistory.collectAsState()
    val isAiLoading by viewModel.isAiLoading.collectAsState()
    val scope = rememberCoroutineScope()

    // EAN Barcode Scanner View States
    var showBarcodeScanner by remember { mutableStateOf(false) }
    var typedBarcode by remember { mutableStateOf("") }
    var isSearchLoading by remember { mutableStateOf(false) }
    var scanErrorText by remember { mutableStateOf<String?>(null) }
    var activeBarcodeCode by remember { mutableStateOf("") }
    var showScanHistory by remember { mutableStateOf(false) }

    var activePantryTab by remember { mutableStateOf("Všetko") } // Všetko, Lednice/Chladnička, Mrazák, Spižírna
    var showAddItemDialog by remember { mutableStateOf(false) }
    var showScannerOptionsSheet by remember { mutableStateOf(false) }

    var itemToSuggestShopping by remember { mutableStateOf<PantryItem?>(null) }

    // Forms
    var manualName by remember { mutableStateOf("") }
    var manualQty by remember { mutableStateOf("1 ks") }
    var manualCat by remember { mutableStateOf("Fridge") }
    var manualExpDays by remember { mutableStateOf("5") }
    var manualPrice by remember { mutableStateOf("29.0") }

    // Direct Camera Scanner State
    var showCameraSimulator by remember { mutableStateOf(false) }
    
    var showUnknownProductDialog by remember { mutableStateOf(false) }
    var unknownBarcode by remember { mutableStateOf("") }
    var unknownDetectedName by remember { mutableStateOf("") }

    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("fridgebuddy_prefs", android.content.Context.MODE_PRIVATE) }
    var isTooltipVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val hasSeenTooltip = sharedPrefs.getBoolean("pantry_tooltip_seen", false)
        if (!hasSeenTooltip) {
            isTooltipVisible = true
            sharedPrefs.edit().putBoolean("pantry_tooltip_seen", true).apply()
            kotlinx.coroutines.delay(5000)
            isTooltipVisible = false
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted1 ->
        if (isGranted1) {
            showBarcodeScanner = true
        }
    }
    
    // Currently Scanned Food Details
    var scannedFoodName by remember { mutableStateOf("") }
    var scannedFoodCalories by remember { mutableStateOf(100) }
    var scannedFoodProtein by remember { mutableStateOf("5g") }
    var scannedFoodCarbs by remember { mutableStateOf("12g") }
    var scannedFoodFat by remember { mutableStateOf("2g") }
    var scannedFoodSugar by remember { mutableStateOf("0g") }
    var scannedFoodServing by remember { mutableStateOf("100g") }
    var scannedFoodBrand by remember { mutableStateOf("") }
    var scannedFoodCategory by remember { mutableStateOf("Fridge") }
    var scannedFoodPrice by remember { mutableStateOf(35.0) }
    var scannedFoodEmoji by remember { mutableStateOf("🍏") }
    
    var showScanResultDialog by remember { mutableStateOf(false) }
    var scanResultPiecesToAdd by remember(showScanResultDialog) { mutableStateOf(1) }
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isSlovak) "Spíž & Lednice" else "Spíž & Lednice",
                            color = CreamText,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isSlovak) "Inteligentná správa potravín" else "Inteligentní správa potravin",
                            color = CreamText.copy(alpha = 0.5f),
                            fontSize = 11.sp
                        )
                    }
                    
                    // Compact History trigger in the top right to save premium space
                    IconButton(
                        onClick = { showScanHistory = true },
                        modifier = Modifier
                            .background(LightSurface, RoundedCornerShape(8.dp))
                            .size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.TopEnd) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = if (isSlovak) "História" else "Historie",
                                tint = SaffronGoldSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                            if (scanHistory.isNotEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .offset(x = 3.dp, y = (-3).dp)
                                        .background(FreshGreenPrimary, CircleShape)
                                        .size(8.dp)
                                )
                            }
                        }
                    }
                }
            }


                        

                        




            stickyHeader {
                // 3. Category Filter tabs
                Surface(
                    color = DarkBg,
                    modifier = Modifier.fillMaxWidth()
                ) {
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
                }
            }

            // 4. Pantry items
            if (filteredPantryItems.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                Icons.Default.Kitchen,
                                contentDescription = "Empty",
                                tint = CreamText.copy(alpha = 0.2f),
                                modifier = Modifier.size(80.dp)
                            )
                            Text(
                                text = if (isSlovak) "Zatiaľ tu nič nie je." else "Zatím tu nic není.",
                                color = CreamText.copy(alpha = 0.7f),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isSlovak) "Klikni na + a pridaj prvú surovinu, alebo naskenuj QR kód rodiny." 
                                     else "Klikni na + a přidej první surovinu, nebo naskenuj QR kód rodiny.",
                                color = CreamText.copy(alpha = 0.5f),
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            } else {
                items(filteredPantryItems) { item ->
                    ProductCard(
                        item = item,
                        onDelete = { itemToSuggestShopping = item },
                        isSlovak = isSlovak
                    )
                }
            }
        }

        // 5. FAB Floating Action Button context holding tooltip
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Column(horizontalAlignment = Alignment.End) {
                if (isTooltipVisible) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = TomatoRedTertiary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .widthIn(max = 200.dp)
                            .padding(bottom = 8.dp)
                            .clickable { isTooltipVisible = false }
                    ) {
                        Text(
                            text = if (isSlovak) "Tu môžeš naskenovať suroviny alebo QR kód domácnosti!" else "Zde můžeš naskenovat suroviny nebo QR kód domácnosti!",
                            color = DarkBg,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
                FloatingActionButton(
                    onClick = { showScannerOptionsSheet = true },
                    containerColor = FreshGreenPrimary,
                    contentColor = Color.Black,
                    modifier = Modifier.testTag("add_pantry_fab")
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = if (isSlovak) "Skenovanie a pridanie" else "Skenování a přidání",
                        tint = Color.Black,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }

        // --- Smart Scanner & Add Options Sheet ---
        if (showScannerOptionsSheet) {
            ModalBottomSheet(
                onDismissRequest = { showScannerOptionsSheet = false },
                containerColor = DarkSurface,
                dragHandle = { BottomSheetDefaults.DragHandle(color = BorderNatural) }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .padding(bottom = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = if (isSlovak) "Možnosti pridania a skenovania" else "Možnosti přidání a skenování",
                        color = FreshGreenPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // 1. EAN Barcode Camera Scanner (Direct ML Kit Camera)
                    Card(
                        colors = CardDefaults.cardColors(containerColor = LightSurface),
                        border = BorderStroke(1.dp, BorderNatural),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showScannerOptionsSheet = false
                                val isCameraPermissionGranted = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.CAMERA
                                ) == PackageManager.PERMISSION_GRANTED
                                if (isCameraPermissionGranted) {
                                    showBarcodeScanner = true
                                } else {
                                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                }
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = "EAN Scanner",
                                tint = SaffronGoldSecondary,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = if (isSlovak) "EAN Skener čiarových kódov" else "EAN Skener čárových kódů",
                                    color = CreamText,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = if (isSlovak) "Rýchle skenovanie kódov potravín kamerou" else "Rychlé skenování kódů potravin kamerou",
                                    color = CreamText.copy(alpha = 0.6f),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    // 2. OCR Skener účteniek / Voice Dictation
                    Card(
                        colors = CardDefaults.cardColors(containerColor = LightSurface),
                        border = BorderStroke(1.dp, BorderNatural),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showScannerOptionsSheet = false
                                onNavigateToTab?.invoke("scanner")
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.DocumentScanner,
                                contentDescription = "OCR / AI Scanner",
                                tint = FreshGreenPrimary,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = if (isSlovak) "OCR Skener Účteniek & AI / Hlas" else "OCR Skener Účtenek & AI / Hlas",
                                    color = CreamText,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = if (isSlovak) "Rozpoznanie celého nákupu z fotky alebo hlasom" else "Rozpoznání celého nákupu z fotky nebo hlasem",
                                    color = CreamText.copy(alpha = 0.6f),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    // 3. Manual Add Dialog
                    Card(
                        colors = CardDefaults.cardColors(containerColor = LightSurface),
                        border = BorderStroke(1.dp, BorderNatural),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showScannerOptionsSheet = false
                                showAddItemDialog = true
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Manual Add",
                                tint = SaffronGoldSecondary,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = if (isSlovak) "Ručné pridanie potraviny" else "Ruční přidání potraviny",
                                    color = CreamText,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = if (isSlovak) "Zadajte názov, expiráciu a detaily ručne" else "Zadejte název, expiraci a detaily ručně",
                                    color = CreamText.copy(alpha = 0.6f),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // --- Scan History Bottom Sheet ---
        if (showScanHistory) {
            ModalBottomSheet(
                onDismissRequest = { showScanHistory = false },
                containerColor = DarkSurface,
                dragHandle = { BottomSheetDefaults.DragHandle(color = BorderNatural) }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .padding(bottom = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isSlovak) "História rodinnej chladničky" else "Historie rodinné lednice",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    if (scanHistory.isEmpty()) {
                        Text(
                            text = if (isSlovak) "Zatiaľ tu nič nie je." else "Zatím tu nic není.",
                            color = CreamText.copy(alpha = 0.5f),
                            modifier = Modifier.padding(vertical = 24.dp)
                        )
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(scanHistory.take(20)) { item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(DarkBg, RoundedCornerShape(8.dp))
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.name,
                                            color = CreamText,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = "EAN: ${item.barcode}",
                                                color = CaptionTextNatural,
                                                fontSize = 11.sp
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .background(SoftGreenGlow, RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                                            ) {
                                                Text(
                                                    text = item.category,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = FreshGreenPrimary
                                                )
                                            }
                                        }
                                    }
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        // "Add another piece" action
                                        IconButton(
                                            onClick = {
                                                viewModel.addManualPantryItem(
                                                    item.name, "1 ks", item.category, 7, item.approxPrice
                                                )
                                            },
                                            modifier = Modifier.size(40.dp).background(FreshGreenPrimary.copy(alpha=0.15f), CircleShape)
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = "Add", tint = FreshGreenPrimary, modifier = Modifier.size(20.dp))
                                        }

                                        // "Remove Scan history" action
                                        IconButton(
                                            onClick = { viewModel.removeScanHistoryItem(item.id) },
                                            modifier = Modifier.size(40.dp).background(TomatoRedTertiary.copy(alpha=0.15f), CircleShape)
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = TomatoRedTertiary, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // --- Low Stock Suggestion Dialog ---
        if (itemToSuggestShopping != null) {
            AlertDialog(
                onDismissRequest = { 
                    viewModel.deleteItem(itemToSuggestShopping!!)
                    itemToSuggestShopping = null 
                },
                title = { Text(if (isSlovak) "Došli zásoby?" else "Došly zásoby?", color = CreamText, fontWeight = FontWeight.Bold) },
                text = { Text(if (isSlovak) "Chcete pridať ${itemToSuggestShopping!!.name} do nákupného zoznamu (Nízky stav / 0 ks)?" else "Chcete přidat ${itemToSuggestShopping!!.name} na nákupní seznam (Nízký stav / 0 ks)?", color = CreamText) },
                containerColor = DarkSurface,
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.addShoppingItem(itemToSuggestShopping!!.name, "1 ks", "Lidl", itemToSuggestShopping!!.approxPrice)
                            viewModel.deleteItem(itemToSuggestShopping!!)
                            itemToSuggestShopping = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = FreshGreenPrimary)
                    ) {
                        Text(if (isSlovak) "Áno, pridať do nákupu" else "Ano, přidat do nákupu", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        viewModel.deleteItem(itemToSuggestShopping!!)
                        itemToSuggestShopping = null
                    }) {
                        Text(if (isSlovak) "Len vymazať" else "Jen vymazat", color = SaffronGoldSecondary)
                    }
                }
            )
        }

        // --- Manual Add Item Dialog ---
        if (showAddItemDialog) {
            // Canonical ingredient names that match recipe keywords exactly
            val commonIngredients = remember {
                listOf(
                    "Mléko", "Máslo", "Vejce", "Cibule", "Česnek", "Sůl", "Pepř",
                    "Hladká mouka", "Polohrubá mouka", "Cukr", "Moučkový cukr",
                    "Smetana na vaření", "Zakysaná smetana", "Šlehačka",
                    "Tvaroh", "Sýr Eidam", "Hermelín", "Bryndza", "Parmazán",
                    "Brambory", "Rýže", "Těstoviny", "Špagety",
                    "Kuřecí maso", "Vepřové maso", "Hovězí maso", "Mleté maso",
                    "Šunka", "Salám", "Slanina", "Klobása", "Párek",
                    "Mrkev", "Celer", "Petržel", "Rajčata", "Paprika", "Brokolice",
                    "Špenát", "Cuketa", "Dýně", "Hrášek", "Kukuřice", "Fazole",
                    "Houby", "Žampiony", "Hříbky",
                    "Jogurt", "Kefír", "Podmáslí",
                    "Ocet", "Slunečnicový olej", "Olivový olej",
                    "Hořčice", "Kečup", "Majonéza",
                    "Citron", "Jablka", "Banán", "Borůvky", "Jahody",
                    "Vlašské ořechy", "Mandle", "Rozinky",
                    "Mletá paprika", "Kmín", "Majoránka", "Tymián", "Bazalka",
                    "Droždí", "Kypřicí prášek", "Vanilkový cukr", "Kakao",
                    "Čokoláda", "Džem", "Med", "Sádlo"
                )
            }

            var nameSuggestions by remember { mutableStateOf(emptyList<String>()) }

            // Quick-add chips organized by category
            val quickAddGroups = remember {
                listOf(
                    "🥛 Mléčné" to listOf("Mléko", "Máslo", "Vejce", "Tvaroh", "Smetana na vaření", "Jogurt"),
                    "🥩 Maso" to listOf("Kuřecí maso", "Vepřové maso", "Hovězí maso", "Šunka", "Salám", "Mleté maso"),
                    "🥔 Základ" to listOf("Brambory", "Cibule", "Česnek", "Rýže", "Těstoviny", "Hladká mouka"),
                    "🌿 Koření" to listOf("Sůl", "Pepř", "Cukr", "Mletá paprika", "Kmín", "Majoránka"),
                    "🫙 Ostatní" to listOf("Olej", "Ocet", "Kečup", "Hořčice", "Droždí", "Kypřicí prášek")
                )
            }
            var activeQuickGroup by remember { mutableStateOf(0) }

            Dialog(onDismissRequest = {
                showAddItemDialog = false
                manualName = ""
                nameSuggestions = emptyList()
            }) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(20.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isSlovak) "Pridať potravinu" else "Přidat potravinu",
                                color = FreshGreenPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(onClick = { showAddItemDialog = false; manualName = "" }) {
                                Icon(Icons.Default.Close, contentDescription = null, tint = CaptionTextNatural)
                            }
                        }

                        // --- QUICK ADD CHIPS ---
                        Text(
                            text = if (isSlovak) "⚡ Rýchle pridanie" else "⚡ Rychlé přidání",
                            color = CreamText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        // Group selector tabs
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            quickAddGroups.forEachIndexed { idx, (groupName, _) ->
                                val selected = activeQuickGroup == idx
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = if (selected) FreshGreenPrimary else DarkBg,
                                    modifier = Modifier.clickable { activeQuickGroup = idx }
                                ) {
                                    Text(
                                        text = groupName,
                                        fontSize = 11.sp,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selected) Color.Black else CaptionTextNatural,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                        // Ingredient chips for selected group
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            quickAddGroups[activeQuickGroup].second.forEach { ingredient ->
                                val alreadyInPantry = pantryItems.any { it.name.equals(ingredient, ignoreCase = true) }
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = if (alreadyInPantry) SoftGreenGlow else NavBgNatural,
                                    border = BorderStroke(
                                        1.dp,
                                        if (alreadyInPantry) FreshGreenPrimary else BorderNatural
                                    ),
                                    modifier = Modifier.clickable {
                                        // Instant add with sensible defaults
                                        val defaultDays = when (manualCat) {
                                            "Freezer" -> 365
                                            "Pantry" -> 90
                                            else -> 7
                                        }
                                        viewModel.addManualPantryItem(
                                            ingredient, "1 ks", manualCat, defaultDays, 0.0
                                        )
                                    }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        if (alreadyInPantry) {
                                            Icon(
                                                Icons.Default.Check,
                                                contentDescription = null,
                                                tint = FreshGreenPrimary,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(Modifier.width(4.dp))
                                        }
                                        Text(
                                            text = ingredient,
                                            fontSize = 12.sp,
                                            color = if (alreadyInPantry) FreshGreenPrimary else CreamText
                                        )
                                    }
                                }
                            }
                        }

                        HorizontalDivider(color = BorderNatural)

                        // --- NAME FIELD WITH AUTOCOMPLETE ---
                        Text(
                            text = if (isSlovak) "Alebo zadajte vlastný názov:" else "Nebo zadejte vlastní název:",
                            color = CaptionTextNatural,
                            fontSize = 11.sp
                        )
                        Column {
                            OutlinedTextField(
                                value = manualName,
                                onValueChange = { v ->
                                    manualName = v
                                    nameSuggestions = if (v.length >= 2) {
                                        commonIngredients.filter {
                                            it.lowercase().contains(v.lowercase())
                                        }.take(5)
                                    } else emptyList()
                                },
                                label = { Text(if (isSlovak) "Název (např. Máslo)" else "Název (např. Máslo)", fontSize = 12.sp) },
                                leadingIcon = {
                                    Icon(Icons.Default.Edit, contentDescription = null, tint = FreshGreenPrimary, modifier = Modifier.size(18.dp))
                                },
                                trailingIcon = {
                                    if (manualName.isNotEmpty()) {
                                        IconButton(onClick = { manualName = ""; nameSuggestions = emptyList() }) {
                                            Icon(Icons.Default.Close, contentDescription = null, tint = CaptionTextNatural, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = CreamText,
                                    unfocusedTextColor = CreamText,
                                    focusedBorderColor = FreshGreenPrimary,
                                    unfocusedBorderColor = BorderNatural,
                                    focusedContainerColor = DarkBg,
                                    unfocusedContainerColor = DarkBg,
                                    focusedLabelColor = FreshGreenPrimary,
                                    unfocusedLabelColor = CaptionTextNatural
                                ),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            // Autocomplete dropdown
                            if (nameSuggestions.isNotEmpty()) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = DarkBg),
                                    shape = RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp),
                                    border = BorderStroke(1.dp, FreshGreenPrimary.copy(alpha = 0.4f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    nameSuggestions.forEach { suggestion ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    manualName = suggestion
                                                    nameSuggestions = emptyList()
                                                }
                                                .padding(horizontal = 16.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Search,
                                                contentDescription = null,
                                                tint = FreshGreenPrimary,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Text(suggestion, color = CreamText, fontSize = 13.sp)
                                        }
                                        HorizontalDivider(color = BorderNatural, thickness = 0.5.dp)
                                    }
                                }
                            }
                        }

                        // --- QUANTITY PRESETS ---
                        Text(
                            text = if (isSlovak) "Množstvo:" else "Množství:",
                            color = CreamText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("1 ks", "2 ks", "250g", "500g", "1 kg", "1 l", "0.5 l").forEach { preset ->
                                val sel = manualQty == preset
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = if (sel) FreshGreenPrimary else DarkBg,
                                    border = BorderStroke(1.dp, if (sel) FreshGreenPrimary else BorderNatural),
                                    modifier = Modifier.clickable { manualQty = preset }
                                ) {
                                    Text(
                                        text = preset,
                                        fontSize = 12.sp,
                                        fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal,
                                        color = if (sel) Color.Black else CreamText,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }

                        // --- LOCATION ---
                        Text(
                            text = if (isSlovak) "Umiestnenie:" else "Umístění:",
                            color = CreamText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val cats = listOf(
                                "Fridge" to "🧊 ${categoryLabelFridge}",
                                "Freezer" to "❄️ ${categoryLabelFreezer}",
                                "Pantry" to "🏠 ${categoryLabelPantry}"
                            )
                            cats.forEach { (c, label) ->
                                val sel = manualCat == c
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (sel) FreshGreenPrimary else DarkBg,
                                    border = BorderStroke(1.dp, if (sel) FreshGreenPrimary else BorderNatural),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            manualCat = c
                                            // Auto-adjust expiry when switching to/from Freezer
                                            manualExpDays = when (c) {
                                                "Freezer" -> "365"
                                                "Pantry" -> "90"
                                                else -> "7"
                                            }
                                        }
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 11.sp,
                                        fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal,
                                        color = if (sel) Color.Black else CreamText,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }
                            }
                        }

                        // --- EXPIRY PRESETS ---
                        Text(
                            text = if (isSlovak) "Expirace:" else "Expirace:",
                            color = CreamText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val expiryPresets = listOf(
                                "3" to if (isSlovak) "3 dni" else "3 dny",
                                "7" to if (isSlovak) "1 týždeň" else "1 týden",
                                "14" to if (isSlovak) "2 týždne" else "2 týdny",
                                "30" to if (isSlovak) "1 mesiac" else "1 měsíc",
                                "90" to if (isSlovak) "3 mesiace" else "3 měsíce",
                                "365" to if (isSlovak) "1 rok" else "1 rok"
                            )
                            expiryPresets.forEach { (days, label) ->
                                val sel = manualExpDays == days
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = if (sel) SaffronGoldSecondary else DarkBg,
                                    border = BorderStroke(1.dp, if (sel) SaffronGoldSecondary else BorderNatural),
                                    modifier = Modifier.clickable { manualExpDays = days }
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 11.sp,
                                        fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal,
                                        color = if (sel) Color.Black else CreamText,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }

                        // --- SAVE BUTTON ---
                        Button(
                            onClick = {
                                val name = manualName.ifBlank { null }
                                if (name != null) {
                                    viewModel.addManualPantryItem(
                                        name,
                                        manualQty,
                                        manualCat,
                                        manualExpDays.toIntOrNull() ?: 7,
                                        manualPrice.toDoubleOrNull() ?: 0.0
                                    )
                                    manualName = ""
                                    nameSuggestions = emptyList()
                                    showAddItemDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (manualName.isNotBlank()) FreshGreenPrimary else BorderNatural
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = if (manualName.isNotBlank()) Color.Black else CaptionTextNatural, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = if (isSlovak) "Uložiť do spíže" else "Uložit do spíže",
                                color = if (manualName.isNotBlank()) Color.Black else CaptionTextNatural,
                                fontWeight = FontWeight.Bold
                            )
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
                                                        activeBarcodeCode = barcode
                                                        scannedFoodName = result.name
                                                        scannedFoodCalories = result.calories
                                                        scannedFoodProtein = "${result.protein}g"
                                                        scannedFoodCarbs = "${result.carbohydrates}g"
                                                        scannedFoodFat = "${result.fat}g"
                                                        scannedFoodSugar = "${if (result.sugars >= 0) result.sugars else 0.0}g"
                                                        scannedFoodServing = "${result.servingSizeG ?: 100}g"
                                                        scannedFoodBrand = result.brand ?: ""
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
                                                        unknownBarcode = barcode
                                                        showUnknownProductDialog = true
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
                                                    activeBarcodeCode = typedBarcode
                                                    scannedFoodName = result.name
                                                    scannedFoodCalories = result.calories
                                                    scannedFoodProtein = "${result.protein}g"
                                                    scannedFoodCarbs = "${result.carbohydrates}g"
                                                    scannedFoodFat = "${result.fat}g"
                                                    scannedFoodSugar = "${if (result.sugars >= 0) result.sugars else 0.0}g"
                                                    scannedFoodServing = "${result.servingSizeG ?: 100}g"
                                                    scannedFoodBrand = result.brand ?: ""
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
                                                    unknownBarcode = typedBarcode
                                                    showUnknownProductDialog = true
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

                        if (scannedFoodBrand.isNotBlank()) {
                            Text(
                                text = "🏷 ${if (isSlovak) "Značka" else "Značka"}: $scannedFoodBrand",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = SaffronGoldSecondary
                            )
                        }

                        Box(
                            modifier = Modifier
                                .background(SoftGreenGlow, RoundedCornerShape(12.dp))
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "🔥 $scannedFoodCalories kcal / 100g",
                                fontWeight = FontWeight.Bold,
                                color = FreshGreenPrimary,
                                fontSize = 16.sp
                            )
                        }

                        Text(
                            text = (if (isSlovak) "🍽 Odporúčaná porcia: " else "🍽 Doporučená porce: ") + scannedFoodServing,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = SaffronGoldSecondary
                        )

                        HorizontalDivider(color = BorderNatural)

                        // Nutritional macros info (na 100 g/ml)
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
                                Text(if (isSlovak) "z toho cukry" else "z toho cukry", fontSize = 11.sp, color = CaptionTextNatural)
                                Text(scannedFoodSugar, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TomatoRedTertiary)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Tuky", fontSize = 11.sp, color = CaptionTextNatural)
                                Text(scannedFoodFat, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = CreamText)
                            }
                        }

                        HorizontalDivider(color = BorderNatural)

                        // Chosen pieces custom counter selector
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(DarkBg, RoundedCornerShape(12.dp))
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isSlovak) "Počet kusov k pridaniu" else "Počet kusů k přidání",
                                color = CreamText,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                IconButton(
                                    onClick = { if (scanResultPiecesToAdd > 1) scanResultPiecesToAdd-- },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(Color(0xFF2D312E), CircleShape)
                                        .testTag("pieces_minus_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Remove,
                                        contentDescription = "Minus",
                                        tint = SaffronGoldSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Text(
                                    text = "$scanResultPiecesToAdd ks",
                                    color = CreamText,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    modifier = Modifier.testTag("pieces_count_text")
                                )
                                IconButton(
                                    onClick = { scanResultPiecesToAdd++ },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(Color(0xFF2D312E), CircleShape)
                                        .testTag("pieces_plus_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Plus",
                                        tint = SaffronGoldSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        HorizontalDivider(color = BorderNatural)

                        Button(
                            onClick = {
                                repeat(scanResultPiecesToAdd) {
                                    viewModel.addManualPantryItem(
                                        scannedFoodName,
                                        "1 ks",
                                        scannedFoodCategory,
                                        7,
                                        scannedFoodPrice
                                    )
                                }
                                // Sken úspěšně ověřen -> zapíšeme do historie!
                                viewModel.addScanHistoryItem(
                                    scannedFoodName,
                                    activeBarcodeCode.ifEmpty { "8594008124117" },
                                    scannedFoodCategory,
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

        if (showUnknownProductDialog) {
            UnknownProductHandler(
                initialName = unknownDetectedName,
                barcode = unknownBarcode,
                onDismiss = {
                    showUnknownProductDialog = false
                    unknownBarcode = ""
                    unknownDetectedName = ""
                }
            )
        }
    }
}

/**
 * INTELIGENTNÍ SYSTÉM IKON (Dynamic Asset Mapping)
 * Přiřadí realistickou, moderní ikonu na základě názvu suroviny.
 * 
 * DESIGN TIP (Custom Vectors): Chcete-li použít vlastní vektorové sady ze složky res/drawable,
 * nahraďte 'rememberVectorPainter(image = ...)' za 'painterResource(id = R.drawable.vlastni_ikona)'.
 */
@Composable
fun getIconForProduct(productName: String): Painter {
    val lowercase = productName.lowercase()
    val icon = when {
        // Pokud je produkt "Camembert" nebo "Sýr", přiřadí ikonu sýra
        lowercase.contains("sýr") || lowercase.contains("syr") || lowercase.contains("camembert") || lowercase.contains("hermelín") || lowercase.contains("cheese") || lowercase.contains("bryndz") || lowercase.contains("eidam") || lowercase.contains("goda") || lowercase.contains("gouda") || lowercase.contains("tvaroh") -> {
            Icons.Default.BreakfastDining // REPRESENTACE SÝRA / MLÉČNÉ SNÍDANĚ
        }
        // Pokud "Šunka" ou "Maso", přiřadí ikonu masa
        lowercase.contains("maso") || lowercase.contains("meat") || lowercase.contains("šunka") || lowercase.contains("sunka") || lowercase.contains("párky") || lowercase.contains("parky") || lowercase.contains("klobás") || lowercase.contains("salám") || lowercase.contains("bůček") || lowercase.contains("hovězí") || lowercase.contains("vepřové") || lowercase.contains("kuřecí") || lowercase.contains("steak") -> {
            Icons.Default.Restaurant // REPRESENTACE MASA / RESTAURACE
        }
        // Pokud "Mléko/Smetana", přiřadí ikonu mléčného výrobku
        lowercase.contains("mléko") || lowercase.contains("mlieko") || lowercase.contains("smetana") || lowercase.contains("smotana") || lowercase.contains("jogurt") || lowercase.contains("milk") || lowercase.contains("kefír") || lowercase.contains("kefir") || lowercase.contains("acid") -> {
            Icons.Default.LocalDrink // REPRESENTACE SKLENICE MLÉKA
        }
        // Pokud zelenina nebo ovoce, přiřadí botanickou ikonu
        lowercase.contains("zelenina") || lowercase.contains("ovoce") || lowercase.contains("mrkev") || lowercase.contains("jablko") || lowercase.contains("citrón") || lowercase.contains("citron") || lowercase.contains("salát") || lowercase.contains("salat") || lowercase.contains("rajče") || lowercase.contains("rajčata") || lowercase.contains("brambor") || lowercase.contains("cibule") || lowercase.contains("česnek") || lowercase.contains("cesnek") || lowercase.contains("vegetable") || lowercase.contains("fruit") -> {
            Icons.Default.Spa // REPRESENTACE ROSTLINY / ZELENINY / SPA
        }
        // Pokud chléb nebo pečivo, přiřadí ikonu pekárny
        lowercase.contains("chléb") || lowercase.contains("chlieb") || lowercase.contains("pečivo") || lowercase.contains("pecivo") || lowercase.contains("rohlik") || lowercase.contains("rohlík") || lowercase.contains("houska") || lowercase.contains("bread") || lowercase.contains("bageta") || lowercase.contains("toast") || lowercase.contains("briošk") || lowercase.contains("muff") -> {
            Icons.Default.BakeryDining // REPRESENTACE PEČIVA / PEKÁRNY
        }
        // Výchozí ikona pro ostatní suroviny
        else -> {
            Icons.Default.ShoppingBag
        }
    }
    return rememberVectorPainter(image = icon)
}

/**
 * KOMPONENTA "PREMIUM CARD" (Základní stavební prvek)
 * Implementuje jedinečný a vizuálně bohatý vzhled Varianty B: Bankovní styl (Modern Premium).
 */
@Composable
fun ProductCard(
    item: PantryItem,
    onDelete: () -> Unit,
    isSlovak: Boolean
) {
    val daysLeft = ((item.expirationTimestamp - System.currentTimeMillis()) / (24 * 60 * 60 * 1000L)).toInt()
    
    // Určení barvy indikátoru čerstvosti (10dp šířka) a popisku dle expirace
    val (statusColor, statusLabel) = when {
        daysLeft < 0 -> Pair(TomatoRedTertiary, if (isSlovak) "Expirovalo!" else "Prošlé!")
        daysLeft == 0 -> Pair(TomatoRedTertiary, if (isSlovak) "Dnes expiruje!" else "Dnes expiruje!")
        daysLeft == 1 -> Pair(ExpiringOrange, if (isSlovak) "Expiruje zajtra!" else "Expiruje zítra!")
        daysLeft <= 3 -> Pair(SaffronGoldSecondary, if (isSlovak) "Expiruje do $daysLeft dní" else "Expiruje do $daysLeft dnů")
        else -> Pair(SafeGreen, if (isSlovak) "Čerstvé - $daysLeft dní" else "Čerstvé - $daysLeft dnů")
    }

    val currency = if (isSlovak) "EUR" else "Kč"

    Card(
        colors = CardDefaults.cardColors(containerColor = LightSurface), // Stejně skvělý vzhled ve světlém i tmavém režimu
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .testTag("product_card_${item.name.replace(" ", "_")}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Vlevo barevný vertikální indikátor čerstvosti (10dp šířka) pro rychlou navigaci očima
            Box(
                modifier = Modifier
                    .width(10.dp)
                    .fillMaxHeight()
                    .background(statusColor)
            )

            // Vnitřní klientská plocha karty s elegantními rozestupy
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Informační sekce: Tučný název (Brand + Product) a doplňující parametry
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = item.name,
                        color = CreamText,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Lokace / kategorie uložení
                        Text(
                            text = when (item.category) {
                                "Fridge" -> if (isSlovak) "Chladnička" else "Lednice"
                                "Freezer" -> if (isSlovak) "Mrazák" else "Mrazák"
                                else -> if (isSlovak) "Spižírna" else "Spižírna"
                            },
                            color = CreamText.copy(alpha = 0.5f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Text(
                            text = "•",
                            color = CreamText.copy(alpha = 0.25f),
                            fontSize = 10.sp
                        )

                        // Odhadovaná cena produktu detailu
                        if (item.approxPrice > 0.0) {
                            Text(
                                text = "${item.approxPrice} $currency",
                                color = SaffronGoldSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "•",
                                color = CreamText.copy(alpha = 0.25f),
                                fontSize = 10.sp
                            )
                        }

                        // Expirace / Čerstvost
                        Text(
                            text = statusLabel,
                            color = statusColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Pravá strana: Ikona produktu + Quantity Badge ("1 ks") a akční odstranění
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 2. Inteligentní ikona s doplňujícím ohraničením (Design ze studia StepIn Tech)
                    val productIconPainter = getIconForProduct(productName = item.name)
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(DarkBg, RoundedCornerShape(8.dp))
                            .border(1.dp, BorderNatural.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = productIconPainter,
                            contentDescription = item.name,
                            tint = FreshGreenPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // 3. Vizualizace "Quantity Badge" (např. "1 ks"), jasně oddělené a přehledné
                    Box(
                        modifier = Modifier
                            .background(SoftGreenGlow, RoundedCornerShape(8.dp))
                            .padding(horizontal = 9.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = item.quantityHint,
                            color = FreshGreenPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    // Odstraňovací tlačítko s Material ripple feedbackem
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(36.dp)
                            .background(LightSurface, CircleShape)
                            .border(1.dp, BorderNatural.copy(alpha = 0.2f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Odebrat",
                            tint = TomatoRedTertiary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
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
