package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.FridgeBuddyViewModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CentralScannerScreen(
    modifier: Modifier = Modifier,
    viewModel: FridgeBuddyViewModel? = null
) {
    val context = LocalContext.current
    val isSlovak = viewModel?.isSlovak?.collectAsState()?.value ?: false
    val scope = rememberCoroutineScope()

    // Central state for text/OCR
    var centralText by remember { mutableStateOf("") }
    var isListening by remember { mutableStateOf(false) }
    var speechErrorText by remember { mutableStateOf<String?>(null) }
    var isProcessingReceipt by remember { mutableStateOf(false) }

    // Nutrient Analysis States
    var analyzedNutrients by remember { mutableStateOf<com.example.data.ProductNutrients?>(null) }
    var isAnalyzingNutrients by remember { mutableStateOf(false) }
    var portionGrams by remember { mutableStateOf("100") }
    var loggingCategory by remember { mutableStateOf("Svačina") }

    // Media selector launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                try {
                    isProcessingReceipt = true
                    val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        val src = ImageDecoder.createSource(context.contentResolver, uri)
                        ImageDecoder.decodeBitmap(src)
                    } else {
                        @Suppress("DEPRECATION")
                        MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                    }

                    val image = InputImage.fromBitmap(bitmap, 0)
                    val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

                    textRecognizer.process(image)
                        .addOnSuccessListener { visionText ->
                            val resultText = visionText.text
                            if (resultText.isNotBlank()) {
                                centralText = resultText
                                viewModel?.processReceiptOcr(resultText)
                                Toast.makeText(context, if (isSlovak) "Účtenka bola naskenovaná a odoslaná k AI spracovaniu!" else "Účtenka byla naskenována a odeslána k AI zpracování!", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(context, if (isSlovak) "Na obrázku nebol nájdený žiadny text." else "Na obrázku nebyl nalezen žádný text.", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(context, "Chyba: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                        .addOnCompleteListener {
                            isProcessingReceipt = false
                        }
                } catch (e: Exception) {
                    isProcessingReceipt = false
                    Toast.makeText(context, "Chyba načítání: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBg)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SoftGreenGlow),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.DocumentScanner,
                    contentDescription = "Scanner icon",
                    tint = FreshGreenPrimary,
                    modifier = Modifier.size(26.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = if (isSlovak) "Chytré Pridávanie" else "Chytré Přidávání",
                    style = MaterialTheme.typography.titleLarge,
                    color = FreshGreenPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isSlovak) "Špičkové hlasové zadávanie a nahrávanie účteniek" else "Špičkové hlasové zadávání a nahrávání účtenek",
                    style = MaterialTheme.typography.bodySmall,
                    color = CreamText.copy(alpha = 0.6f)
                )
            }
        }

        Divider(color = BorderNatural, thickness = 1.dp)

        // --- SECTION A: VOICE DICTATION WIDGET (BEAUTIFUL PULSE/CARD) ---
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = BorderStroke(1.dp, if (isListening) FreshGreenPrimary else BorderNatural),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Mic",
                        tint = if (isListening) FreshGreenPrimary else SaffronGoldSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isSlovak) "Hlasové Zadávanie" else "Hlasové Zadávání",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = CreamText
                    )
                }

                Text(
                    text = if (isSlovak) "Kliknite na tlačidlo, nadiktujte nákup (napr. 'dve mlieka a maslo') a AI kúzlo naplní Vašu špajzu automaticky!" else "Klikněte na tlačítko, nadiktujte nákup (např. 'dvě mléka a máslo') a AI kouzlo naplní Vaši spižírnu automaticky!",
                    fontSize = 12.sp,
                    color = CreamText.copy(alpha = 0.7f),
                    textAlign = TextAlign.Start,
                    lineHeight = 16.sp
                )

                // Large Interactive Button
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(40.dp))
                        .background(
                            Brush.radialGradient(
                                if (isListening) {
                                    listOf(FreshGreenPrimary, SoftGreenGlow, Color.Transparent)
                                } else {
                                    listOf(Color(0xFF2C3E50), Color(0xFF1A252F))
                                }
                            )
                        )
                        .border(
                            width = 2.dp,
                            color = if (isListening) FreshGreenPrimary else SaffronGoldSecondary,
                            shape = RoundedCornerShape(40.dp)
                        )
                        .clickable {
                            isListening = !isListening
                            if (isListening) {
                                speechErrorText = null
                            }
                        }
                        .testTag("button_start_voice"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isListening) Icons.Default.Pause else Icons.Default.Mic,
                        contentDescription = "Voice dictation action",
                        tint = if (isListening) Color.White else SaffronGoldSecondary,
                        modifier = Modifier.size(32.dp)
                    )
                }

                if (isListening) {
                    Text(
                        text = if (isSlovak) "Počúvam... Hovorte teraz 🎙️" else "Poslouchám... Mluvte nyní 🎙️",
                        style = MaterialTheme.typography.bodySmall,
                        color = FreshGreenPrimary,
                        fontWeight = FontWeight.Bold
                    )

                    // Nativní SpeechRecognizer effect
                    DisposableEffect(Unit) {
                        var speechRecognizer: SpeechRecognizer? = null
                        try {
                            if (SpeechRecognizer.isRecognitionAvailable(context)) {
                                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, if (isSlovak) "sk-SK" else "cs-CZ")
                                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, if (isSlovak) "sk-SK" else "cs-CZ")
                                        putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, if (isSlovak) "sk-SK" else "cs-CZ")
                                    }

                                    setRecognitionListener(object : RecognitionListener {
                                        override fun onReadyForSpeech(params: Bundle?) { speechErrorText = null }
                                        override fun onBeginningOfSpeech() {}
                                        override fun onRmsChanged(rmsdB: Float) {}
                                        override fun onBufferReceived(buffer: ByteArray?) {}
                                        override fun onEndOfSpeech() { isListening = false }
                                        override fun onError(error: Int) {
                                            isListening = false
                                            val errCodeString = when (error) {
                                                SpeechRecognizer.ERROR_AUDIO -> "Chyba nahrávání zvuku."
                                                SpeechRecognizer.ERROR_CLIENT -> "Klientská chyba."
                                                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Chybějící oprávnění k mikrofonu."
                                                SpeechRecognizer.ERROR_NETWORK -> "Chyba sítě."
                                                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Síťový časový limit."
                                                SpeechRecognizer.ERROR_NO_MATCH -> "Nebylo rozumět slovům. Zkuste to znovu."
                                                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Služba rozpoznávání je zaneprázdněná."
                                                SpeechRecognizer.ERROR_SERVER -> "Chyba serveru."
                                                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Žádný zvukový vstup."
                                                else -> "Rozpoznávání řeči selhalo ($error)."
                                            }
                                            speechErrorText = errCodeString
                                        }

                                        override fun onResults(results: Bundle?) {
                                            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                                            if (!matches.isNullOrEmpty()) {
                                                centralText = matches[0]
                                            }
                                            isListening = false
                                        }
                                        override fun onPartialResults(partialResults: Bundle?) {}
                                        override fun onEvent(eventType: Int, params: Bundle?) {}
                                    })
                                    startListening(intent)
                                }
                            } else {
                                isListening = false
                                speechErrorText = "Hlasové zadávání není na tomto zařízení k dispozici."
                            }
                        } catch (e: Exception) {
                            isListening = false
                            speechErrorText = "Error: ${e.message}"
                        }

                        onDispose {
                            try {
                                speechRecognizer?.stopListening()
                                speechRecognizer?.destroy()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
            }
        }

        // --- SECTION B: RECEIPT UPLOADER / SCANNER ---
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = BorderStroke(1.dp, BorderNatural),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ReceiptLong,
                        contentDescription = "Receipt",
                        tint = SaffronGoldSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isSlovak) "Nahrávanie Účteniek" else "Nahrávání Účtenek",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = CreamText
                    )
                }

                Text(
                    text = if (isSlovak) "Nahrajte fotografiu nákupnej účtenky z prepojenej galérie. Naša integrovaná AI vyextrahuje položky a rovno ich naskladní do Vašich zásob." else "Nahrajte fotografii nákupní účtenky z propojené galerie. Naše integrovaná AI vyextrahuje položky a rovnou je naskladní do Vašich zásob.",
                    fontSize = 12.sp,
                    color = CreamText.copy(alpha = 0.7f),
                    lineHeight = 16.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Upload button
                    Button(
                        onClick = { imagePickerLauncher.launch("image/*") },
                        colors = ButtonDefaults.buttonColors(containerColor = SaffronGoldSecondary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("button_upload_receipt")
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = "Upload", tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isSlovak) "Nahrať Fotku" else "Nahrát Fotku", color = Color.White, fontSize = 13.sp)
                    }

                    // Demo Template receipt trigger
                    Button(
                        onClick = {
                            val dummyReceipt = "LIDL CESKA REPUBLIKA\n" +
                                    "PILOS JOGURT RECKY JAHODA 150G  2x  39.80\n" +
                                    "DULANO VIDENSKE PARKY 250G       1x  59.90\n" +
                                    "MASLO CERSTVE 250G               1x  49.90\n" +
                                    "PENAM TOASTOVY CHLEB 500G        1x  34.90\n" +
                                    "CELKEM CZK                       184.50"
                            centralText = dummyReceipt
                            viewModel?.processReceiptOcr(dummyReceipt)
                            Toast.makeText(context, if (isSlovak) "Vzorová účtenka Lidl odoslaná k AI spracovaniu!" else "Vzorová účtenka Lidl odeslána k AI zpracování!", Toast.LENGTH_LONG).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C3E50)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1.2f)
                            .height(48.dp)
                            .testTag("button_sample_receipt")
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = "Sample", tint = FreshGreenPrimary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isSlovak) "Vzorová Účtenka" else "Vzorová Účtenka", color = Color.White, fontSize = 12.sp)
                    }
                }
            }
        }

        // --- SECTION C: RESULT BOX & CUSTOM PROCESSING LAYOUT ---
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = if (isSlovak) "Aktuálny textový vstup / OCR:" else "Aktuální textový vstup / OCR:",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = SaffronGoldSecondary,
                modifier = Modifier.align(Alignment.Start)
            )

            OutlinedTextField(
                value = centralText,
                onValueChange = { centralText = it },
                label = { Text(if (isSlovak) "Zadané informácie" else "Zadané informace", color = SaffronGoldSecondary) },
                placeholder = { Text(if (isSlovak) "Tu sa objaví nadiktovaný nákup alebo text prečítaný z účtenky..." else "Zde se objeví nadiktovaný nákup nebo text přečtený z účtenky...", color = CreamText.copy(alpha = 0.4f)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("central_text_input")
                    .heightIn(min = 100.dp),
                textStyle = LocalTextStyle.current.copy(color = CreamText, fontSize = 14.sp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = FreshGreenPrimary,
                    unfocusedBorderColor = BorderNatural,
                    focusedTextColor = CreamText,
                    unfocusedTextColor = CreamText,
                    focusedContainerColor = DarkSurface,
                    unfocusedContainerColor = DarkSurface
                ),
                shape = RoundedCornerShape(12.dp),
                maxLines = 6
            )

            if (isProcessingReceipt) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SoftGreenGlow.copy(alpha = 0.2f)),
                    border = BorderStroke(1.dp, FreshGreenPrimary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = FreshGreenPrimary, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(if (isSlovak) "Prebieha OCR rozpoznávanie textu..." else "Probíhá OCR rozpoznávání textu...", fontSize = 12.sp, color = CreamText, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (speechErrorText != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0x33CF6679)),
                    border = BorderStroke(1.dp, Color(0xFFCF6679)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "⚠️ $speechErrorText",
                        color = Color(0xFFCF6679),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            if (centralText.isNotBlank() && viewModel != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Button 1: AI Nutrient Analysis
                    Button(
                        onClick = {
                            scope.launch {
                                isAnalyzingNutrients = true
                                try {
                                    val result = com.example.data.StepInTechAiService.extractNutrients(centralText)
                                    analyzedNutrients = result
                                    portionGrams = if (result.serving_size_g != null && result.serving_size_g > 0) {
                                        result.serving_size_g.toString()
                                    } else if (result.weight_g != null && result.weight_g > 0) {
                                        result.weight_g.toString()
                                    } else {
                                        "100"
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Analýza selhala: ${e.message}", Toast.LENGTH_SHORT).show()
                                } finally {
                                    isAnalyzingNutrients = false
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SaffronGoldSecondary),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("button_nutri_explain")
                    ) {
                        Icon(Icons.Default.Restaurant, contentDescription = "Nutri", tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isSlovak) "AI Analýza" else "AI Analýza", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    // Button 2: Quick parse and populate pantry
                    Button(
                        onClick = {
                            viewModel.processVoiceDictation(centralText)
                            Toast.makeText(context, if (isSlovak) "Odoslané do špajzy!" else "Odesláno do spižírny!", Toast.LENGTH_SHORT).show()
                            centralText = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = FreshGreenPrimary),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("button_process_central_text")
                    ) {
                        Icon(Icons.Default.Kitchen, contentDescription = "Pantry", tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isSlovak) "Zásoby" else "Zásoby", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // --- SECTION D: AI NUTRIENTS DETAILED LOGGING CARD ---
        if (isAnalyzingNutrients) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SoftGreenGlow.copy(alpha = 0.15f)),
                border = BorderStroke(1.dp, SaffronGoldSecondary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = SaffronGoldSecondary, strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(if (isSlovak) "StepInTech AI analyzuje nutričné hodnoty..." else "StepInTech AI analyzuje nutriční hodnoty...", fontSize = 12.sp, color = CreamText)
                }
            }
        }

        analyzedNutrients?.let { nutri ->
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.5.dp, SaffronGoldSecondary),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("analyzed_nutrients_card")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isSlovak) "🥗 Detaily Živín z AI" else "🥗 Detaily Živin z AI",
                            style = MaterialTheme.typography.titleMedium,
                            color = SaffronGoldSecondary,
                            fontWeight = FontWeight.Bold
                        )

                        nutri.brand?.let { b ->
                            Box(
                                modifier = Modifier
                                    .background(SoftGreenGlow, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(b, fontSize = 10.sp, color = FreshGreenPrimary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    HorizontalDivider(color = BorderNatural, thickness = 1.dp)

                    Text(
                        text = nutri.product_name,
                        style = MaterialTheme.typography.headlineSmall,
                        color = CreamText,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        nutri.weight_g?.let { w ->
                            Text(
                                text = (if (isSlovak) "Celková hmotnosť: " else "Celková hmotnost: ") + "${w}g",
                                fontSize = 12.sp,
                                color = CreamText.copy(alpha = 0.6f)
                            )
                        }
                        nutri.serving_size_g?.let { s ->
                            Text(
                                text = (if (isSlovak) "Odporúčaná porcia: " else "Doporučená porce: ") + "${s}g",
                                fontSize = 12.sp,
                                color = CreamText.copy(alpha = 0.6f)
                            )
                        }
                    }

                    HorizontalDivider(color = BorderNatural.copy(alpha = 0.5f), thickness = 1.dp)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = portionGrams,
                            onValueChange = { portionGrams = it.filter { char -> char.isDigit() } },
                            label = { Text(if (isSlovak) "Množstvo (g)" else "Množství (g)", fontSize = 11.sp, color = SaffronGoldSecondary) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("portion_grams_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SaffronGoldSecondary,
                                unfocusedBorderColor = BorderNatural,
                                focusedTextColor = CreamText,
                                unfocusedTextColor = CreamText
                            ),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )

                        val habits = if (isSlovak) {
                            listOf("Raňajky", "Obed", "Olovrant", "Večera")
                        } else {
                            listOf("Snídaně", "Oběd", "Svačina", "Večeře")
                        }

                        Column(modifier = Modifier.weight(1.3f)) {
                            Text(
                                text = if (isSlovak) "Jedlo dňa:" else "Jídlo dne:",
                                fontSize = 10.sp,
                                color = CreamText.copy(alpha = 0.6f),
                                modifier = Modifier.padding(start = 2.dp, bottom = 2.dp)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                habits.forEach { h ->
                                    val isSelected = loggingCategory == h || (loggingCategory == "Svačina" && h == "Olovrant") || (loggingCategory == "Raňajky" && h == "Snídaně") || (loggingCategory == "Večera" && h == "Večeře")
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(
                                                color = if (isSelected) SaffronGoldSecondary else DarkBg,
                                                shape = RoundedCornerShape(6.dp)
                                            )
                                            .border(1.dp, if (isSelected) SaffronGoldSecondary else BorderNatural, RoundedCornerShape(6.dp))
                                            .clip(RoundedCornerShape(6.dp))
                                            .clickable {
                                                loggingCategory = when (h) {
                                                    "Raňajky" -> "Snídaně"
                                                    "Olovrant" -> "Svačina"
                                                    "Večera" -> "Večeře"
                                                    else -> h
                                                }
                                            }
                                            .padding(vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = h.take(2),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) DarkBg else CreamText
                                        )
                                    }
                                }
                            }
                        }
                    }

                    val scaleFactor = (portionGrams.toDoubleOrNull() ?: 100.0) / 100.0
                    val scaledCalories = (nutri.calories_per_100g * scaleFactor).toInt()
                    val scaledProtein = (nutri.macronutrients.protein_g * scaleFactor)
                    val scaledCarbs = (nutri.macronutrients.carbohydrates_g * scaleFactor)
                    val scaledFat = (nutri.macronutrients.fat_g * scaleFactor)

                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkBg),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, BorderNatural),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Energie / Kalorie:", fontSize = 12.sp, color = CreamText)
                                Text("$scaledCalories kcal", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = FreshGreenPrimary)
                            }

                            val progressRatio = (scaledCalories / 600f).coerceIn(0f, 1f)
                            LinearProgressIndicator(
                                progress = progressRatio,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = FreshGreenPrimary,
                                trackColor = BorderNatural
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                    Text(if (isSlovak) "Bielkoviny" else "Bílkoviny", fontSize = 10.sp, color = CreamText.copy(alpha = 0.5f))
                                    Text(String.format(Locale.US, "%.1fg", scaledProtein), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SaffronGoldSecondary)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                    Text("Sacharidy", fontSize = 10.sp, color = CreamText.copy(alpha = 0.5f))
                                    Text(String.format(Locale.US, "%.1fg", scaledCarbs), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = FreshGreenPrimary)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                    Text("Tuky", fontSize = 10.sp, color = CreamText.copy(alpha = 0.5f))
                                    Text(String.format(Locale.US, "%.1fg", scaledFat), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TomatoRedTertiary)
                                }
                            }
                        }
                    }

                    Button(
                        onClick = {
                            if (viewModel != null) {
                                viewModel.logMealDirect(
                                    name = nutri.product_name,
                                    cal = scaledCalories,
                                    carbs = scaledCarbs.toInt(),
                                    prot = scaledProtein.toInt(),
                                    fat = scaledFat.toInt(),
                                    categoryName = loggingCategory
                                )
                                Toast.makeText(
                                    context,
                                    if (isSlovak) "Jedlo bolo zapísané do kalorického denníka! 🎯" else "Jídlo bylo zapsáno do trackeru kalorií! 🎯",
                                    Toast.LENGTH_LONG
                                ).show()
                                analyzedNutrients = null
                                centralText = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = FreshGreenPrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("button_log_analyzed_meal"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Log Check")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isSlovak) "Zapísať do kalorického trackera" else "Zapsat do trackeru kalorií", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
