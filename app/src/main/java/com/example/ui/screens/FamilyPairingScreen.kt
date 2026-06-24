package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.HouseholdManager
import com.example.data.FamilyManager
import com.example.data.PairingRequest
import com.example.ui.theme.*
import com.example.ui.viewmodel.FridgeBuddyViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FamilyPairingScreen(
    viewModel: FridgeBuddyViewModel,
    householdManager: HouseholdManager,
    onPairingSuccess: (String) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()

    val isSlovak by viewModel.isSlovak.collectAsState()
    val currentHouseholdId by viewModel.householdId.collectAsState()
    val userEmailState by viewModel.userEmail.collectAsState()
    val userNicknameState by viewModel.userNickname.collectAsState()
    val pairingCodeState by viewModel.userPairingCode.collectAsState()

    val emailToUse = userEmailState ?: "anonymous_user@gmail.com"
    val nicknameToUse = userNicknameState.ifBlank { "NutriČlen" }

    // FamilyManager for code generation and real-time syncing
    val familyManager = remember { FamilyManager() }
    
    // States
    var myPairingCode by remember { mutableStateOf<String?>(null) }
    var targetCodeInput by remember { mutableStateOf("") }
    var isSendingRequest by remember { mutableStateOf(false) }
    var isCreatingFamily by remember { mutableStateOf(false) }
    var familyNameInput by remember { mutableStateOf("") }
    var showExitConfirmDialog by remember { mutableStateOf(false) }

    // Generujeme nebo stahujeme kód při načtení obrazovky
    LaunchedEffect(emailToUse, nicknameToUse, currentHouseholdId, pairingCodeState) {
        familyManager.getOrCreatePairingCode(
            email = emailToUse,
            currentNickname = nicknameToUse,
            currentHouseholdId = currentHouseholdId,
            preferredCode = pairingCodeState
        ) { code ->
            myPairingCode = code
        }
    }

    // Sledování příchozích žádostí v reálném čase
    val incomingRequests by remember(emailToUse) {
        familyManager.syncIncomingRequests(emailToUse)
    }.collectAsState(initial = emptyList())

    // Sledování odeslaných žádostí, které druhá strana schválila
    val outgoingApprovedRequests by remember(emailToUse) {
        familyManager.syncOutgoingApprovedRequests(emailToUse)
    }.collectAsState(initial = emptyList())

    // Speciální efekt: Jakmile někdo potvrdí naši odeslanou žádost, okamžitě se spárujeme!
    LaunchedEffect(outgoingApprovedRequests) {
        val approvedReq = outgoingApprovedRequests.firstOrNull()
        if (approvedReq != null) {
            val sharedId = approvedReq.sharedHouseholdId
            if (sharedId.isNotEmpty() && sharedId != currentHouseholdId) {
                viewModel.updateHouseholdId(sharedId)
                familyManager.markRequestCompleted(approvedReq.id, emailToUse, sharedId)
                Toast.makeText(
                    context,
                    if (isSlovak) "Úspešne prepojené s užívateľom ${approvedReq.toUserNickname}!" else "Úspěšně propojeno s uživatelem ${approvedReq.toUserNickname}!",
                    Toast.LENGTH_LONG
                ).show()
                onPairingSuccess(sharedId)
            }
        }
    }

    // Určení stavu rodiny
    val hasNoFamily = currentHouseholdId == "NK-FAMILY-DEMO123" || currentHouseholdId.isBlank()

    // Sync live activity log
    val activityLogs by remember(currentHouseholdId) {
        if (!hasNoFamily) {
            householdManager.syncActivityLogLive(currentHouseholdId)
        } else {
            kotlinx.coroutines.flow.flowOf(emptyList())
        }
    }.collectAsState(initial = emptyList())

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onCancel,
                    modifier = Modifier.background(LightSurface, CircleShape)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = CreamText)
                }

                Text(
                    text = if (isSlovak) "Prepojenie Členov" else "Propojení Členů",
                    style = MaterialTheme.typography.titleMedium,
                    color = SaffronGoldSecondary,
                    fontWeight = FontWeight.Bold
                )

                Box(modifier = Modifier.size(48.dp)) // Spacer to keep balanced alignment
            }

            // 1. Zobrazit vlastní kód uživatele (Moje párovací ID) - PRÉMIOVÁ KARTA
            Card(
                colors = CardDefaults.cardColors(containerColor = LightSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = if (isSlovak) "TVOJ PÁROVACÍ KÓD" else "TVŮJ PÁROVACÍ KÓD",
                        color = FreshGreenPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .background(DarkBg, RoundedCornerShape(12.dp))
                            .border(1.dp, BorderNatural.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .clickable {
                                myPairingCode?.let {
                                    clipboardManager.setText(AnnotatedString(it))
                                    Toast.makeText(
                                        context,
                                        if (isSlovak) "Kód odkopírovaný!" else "Kód odkopírován!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                            .padding(horizontal = 24.dp, vertical = 14.dp)
                    ) {
                        Text(
                            text = myPairingCode ?: "NK-......",
                            color = CreamText,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Kopírovat",
                            tint = SaffronGoldSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Text(
                        text = if (isSlovak) {
                            "Pošli tento kód druhému členovi rodiny. Ten ho zadá do svojho telefónu a ty uvidíš jeho žiadosť pre potvrdenie."
                        } else {
                            "Pošli tento kód druhému členovi rodiny. Ten ho zadá do svého telefonu a ty uvidíš jeho žádost pro potvrzení."
                        },
                        color = CreamText.copy(alpha = 0.7f),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }

            // 2. Odeslání žádosti o propojení pomocí kódu jiného člena
            Card(
                colors = CardDefaults.cardColors(containerColor = NavBgNatural),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, BorderNatural.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = if (isSlovak) "Prepojiť sa s iným členom" else "Propojit se s jiným členem",
                        color = CreamText,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = targetCodeInput,
                            onValueChange = { targetCodeInput = it },
                            placeholder = { Text("např. NK-123456", color = CreamText.copy(alpha = 0.4f)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = FreshGreenPrimary,
                                unfocusedBorderColor = BorderNatural,
                                focusedTextColor = CreamText,
                                unfocusedTextColor = CreamText
                            ),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )

                        Button(
                            onClick = {
                                if (targetCodeInput.isBlank()) {
                                    Toast.makeText(context, if (isSlovak) "Zadajte kód najskôr!" else "Zadejte kód nejdříve!", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                isSendingRequest = true
                                familyManager.sendPairingRequest(
                                    myEmail = emailToUse,
                                    myNickname = nicknameToUse,
                                    myHouseholdId = currentHouseholdId,
                                    targetCode = targetCodeInput
                                ) { success, msg ->
                                    isSendingRequest = false
                                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                    if (success) {
                                        targetCodeInput = ""
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = FreshGreenPrimary),
                            shape = RoundedCornerShape(10.dp),
                            enabled = !isSendingRequest,
                            modifier = Modifier.height(52.dp)
                        ) {
                            if (isSendingRequest) {
                                CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
                            } else {
                                Icon(Icons.Default.Send, contentDescription = "Odeslat", tint = Color.Black)
                            }
                        }
                    }
                }
            }

            // 3. PŘÍCHOZÍ ŽÁDOSTI O PROPOJENÍ - REALTIME S CONFIRM/DECLINE ACTIONS
            if (incomingRequests.isNotEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (isSlovak) "Příchozí Žiadosti o Prepojenie" else "Příchozí Žádosti o Propojení",
                        color = SaffronGoldSecondary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        modifier = Modifier.padding(start = 4.dp)
                    )

                    incomingRequests.forEach { req ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = LightSurface),
                            border = BorderStroke(1.5.dp, FreshGreenPrimary.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = req.fromUserNickname,
                                        color = CreamText,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = req.fromUserEmail,
                                        color = CreamText.copy(alpha = 0.5f),
                                        fontSize = 12.sp
                                    )
                                }

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Odmítnout [Červené]
                                    IconButton(
                                        onClick = {
                                            familyManager.respondToPairingRequest(
                                                requestId = req.id,
                                                accept = false,
                                                incomingRequest = req,
                                                myHouseholdId = currentHouseholdId,
                                                myEmail = emailToUse
                                            ) { _, _ ->
                                                Toast.makeText(context, if (isSlovak) "Žiadosť odmietnutá" else "Žádost odmítnuta", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(TomatoRedTertiary.copy(alpha = 0.2f), CircleShape)
                                            .border(1.dp, TomatoRedTertiary.copy(alpha = 0.5f), CircleShape)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "Odmítnout", tint = TomatoRedTertiary, modifier = Modifier.size(18.dp))
                                    }

                                    // Potvrdit [Zelené]
                                    IconButton(
                                        onClick = {
                                            familyManager.respondToPairingRequest(
                                                requestId = req.id,
                                                accept = true,
                                                incomingRequest = req,
                                                myHouseholdId = currentHouseholdId,
                                                myEmail = emailToUse
                                            ) { sharedId, err ->
                                                if (err != null) {
                                                    Toast.makeText(context, "Chyba: ${err.message}", Toast.LENGTH_LONG).show()
                                                } else if (sharedId != null) {
                                                    viewModel.updateHouseholdId(sharedId)
                                                    Toast.makeText(context, if (isSlovak) "Úspešne prepojené!" else "Úspěšně propojeno!", Toast.LENGTH_SHORT).show()
                                                    onPairingSuccess(sharedId)
                                                }
                                            }
                                        },
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(FreshGreenPrimary.copy(alpha = 0.2f), CircleShape)
                                            .border(1.dp, FreshGreenPrimary.copy(alpha = 0.5f), CircleShape)
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = "Potvrdit", tint = FreshGreenPrimary, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 4. Pokud uživatel nemá založenou žádnou rodinu, umožníme založit novou čistou
            if (hasNoFamily) {
                Divider(color = BorderNatural, thickness = 1.dp, modifier = Modifier.padding(vertical = 4.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = if (isSlovak) "Prípadne vytvorte novú prázdnu domácnosť" else "Případně vytvořte novou prázdnou domácnost",
                        color = CreamText.copy(alpha = 0.6f),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )

                    OutlinedTextField(
                        value = familyNameInput,
                        onValueChange = { familyNameInput = it },
                        label = { Text(if (isSlovak) "Názov vašej rodiny" else "Název vaší rodiny", color = CreamText.copy(alpha = 0.5f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FreshGreenPrimary,
                            unfocusedBorderColor = BorderNatural,
                            focusedTextColor = CreamText,
                            unfocusedTextColor = CreamText
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    if (isCreatingFamily) {
                        CircularProgressIndicator(color = FreshGreenPrimary)
                    } else {
                        Button(
                            onClick = {
                                if (familyNameInput.isBlank()) {
                                    Toast.makeText(context, if (isSlovak) "Zadajte názov rodiny!" else "Zadejte název rodiny!", Toast.LENGTH_SHORT).show()
                                } else {
                                    isCreatingFamily = true
                                    familyManager.createHousehold(familyNameInput, emailToUse) { generatedId, _ ->
                                        isCreatingFamily = false
                                        if (generatedId != null) {
                                            viewModel.updateHouseholdId(generatedId)
                                            Toast.makeText(
                                                context,
                                                if (isSlovak) "Domácnosť úspešne vytvorená!" else "Domácnost úspěšně vytvořena!",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SaffronGoldSecondary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Icon(Icons.Default.AddHome, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isSlovak) "Založiť rodinnú skupinu" else "Založit rodinnou skupinu",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            } else {
                // --- KDYŽ JE RODINA AKTIVNÍ: Zobrazit info + logy ---
                Divider(color = BorderNatural, thickness = 1.dp, modifier = Modifier.padding(vertical = 4.dp))

                // Zobrazení ID aktuální propojené skupiny
                Card(
                    colors = CardDefaults.cardColors(containerColor = NavBgNatural),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, BorderNatural),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = if (isSlovak) "ID vašej aktuálnej rodiny:" else "ID vaší aktuální rodiny:",
                                color = CreamText.copy(alpha = 0.5f),
                                fontSize = 11.sp
                            )
                            Text(
                                text = currentHouseholdId,
                                color = CreamText,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        IconButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(currentHouseholdId))
                                Toast.makeText(
                                    context,
                                    if (isSlovak) "ID odkopírované!" else "ID odkopírováno!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            modifier = Modifier.background(LightSurface, RoundedCornerShape(8.dp))
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy ID",
                                tint = SaffronGoldSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Shared Activity Feed (Společné Aktivity)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (isSlovak) "Spoločné Aktivity" else "Společné Aktivity",
                        color = FreshGreenPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                    )

                    if (activityLogs.isEmpty()) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = NavBgNatural),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, BorderNatural),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (isSlovak) "Zatiaľ žiadna spoločná aktivita." else "Zatím žádná společná aktivita.",
                                    color = CreamText.copy(alpha = 0.4f),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    } else {
                        activityLogs.take(6).forEach { log ->
                            val formattedLog = householdManager.formatActivityLog(log, isSlovak)
                            if (formattedLog.isNotEmpty()) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = NavBgNatural),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, BorderNatural.copy(alpha = 0.5f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .background(FreshGreenPrimary, CircleShape)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = formattedLog,
                                            color = CreamText,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Leave Family Button
                OutlinedButton(
                    onClick = { showExitConfirmDialog = true },
                    border = BorderStroke(1.dp, TomatoRedTertiary),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TomatoRedTertiary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Icon(Icons.Default.ExitToApp, contentDescription = null, tint = TomatoRedTertiary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isSlovak) "Opustiť domácnosť / Odpojiť sa" else "Opustit domácnost / Odpojit se",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }

        // Leave Confirmation Dialog
        if (showExitConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showExitConfirmDialog = false },
                containerColor = DarkSurface,
                title = {
                    Text(
                        text = if (isSlovak) "Opustiť domácnosť?" else "Opustit domácnost?",
                        color = CreamText,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = if (isSlovak) {
                            "Naozaj sa chcete odpojiť? Týmto prídete o prepojenie s rodinnými členmi a vrátite sa k samostatnému režimu."
                        } else {
                            "Opravdu se chcete odpojit? Tímto přijdete o propojení s rodinnými členy a vrátíte se k samostatnému režimu."
                        },
                        color = CreamText.copy(alpha = 0.8f)
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showExitConfirmDialog = false
                            viewModel.updateHouseholdId("NK-FAMILY-DEMO123")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TomatoRedTertiary)
                    ) {
                        Text(if (isSlovak) "Áno, odpojiť" else "Ano, odpojit", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showExitConfirmDialog = false }) {
                        Text(if (isSlovak) "Zrušiť" else "Zrušit", color = CreamText)
                    }
                }
            )
        }
    }
}
