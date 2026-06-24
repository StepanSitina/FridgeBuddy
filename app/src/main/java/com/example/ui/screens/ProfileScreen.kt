package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.FridgeBuddyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: FridgeBuddyViewModel,
    onNavigateToFamily: () -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val isSlovak by viewModel.isSlovak.collectAsState()
    val userEmail by viewModel.userEmail.collectAsState()
    val userNickname by viewModel.userNickname.collectAsState()
    val householdId by viewModel.householdId.collectAsState()
    val pairingCodeState by viewModel.userPairingCode.collectAsState()

    val displayEmail = userEmail ?: "anonymous@gmail.com"
    val displayNickname = userNickname.ifBlank { "NutriČlen" }
    
    // Fallback: if pairing code is not set yet in state flow, generate one or try to load from Firestore via FamilyManager (or fall back to placeholder or local custom flow)
    val displayPairingCode = if (!pairingCodeState.isNullOrBlank()) {
        pairingCodeState!!
    } else {
        // Just generate mock-like fallback, but ideally it was already generated in Onboarding/Login.
        "NK-888888"
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = DarkBg
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
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = if (isSlovak) "Môj Profil" else "Můj Profil",
                        fontWeight = FontWeight.Bold,
                        color = CreamText
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.background(LightSurface, CircleShape)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = CreamText)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = DarkBg)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Avatar Section
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(
                        Brush.radialGradient(listOf(SoftGreenGlow, FreshGreenPrimary)),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = displayNickname.take(1).uppercase(),
                    color = Color.White,
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Text(
                text = displayNickname,
                style = MaterialTheme.typography.titleLarge,
                color = CreamText,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = displayEmail,
                style = MaterialTheme.typography.bodyMedium,
                color = CreamText.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 1. ZOBRAZENÍ MOJEHO SPOJOVACÍHO KÓDU - GENERATES & DISPLAYS FOR OTHERS TO ADD
            Card(
                colors = CardDefaults.cardColors(containerColor = LightSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = if (isSlovak) "VÁŠ POZÝVACÍ KÓD" else "VÁŠ ZVACÍ KÓD",
                        color = FreshGreenPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .background(DarkBg, RoundedCornerShape(12.dp))
                            .border(1.dp, BorderNatural.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .clickable {
                                clipboardManager.setText(AnnotatedString(displayPairingCode))
                                Toast.makeText(
                                    context,
                                    if (isSlovak) "Pozývací kód bol skopírovaný do schránky!" else "Zvací kód byl zkopírován do schránky!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            .padding(horizontal = 24.dp, vertical = 14.dp)
                    ) {
                        Text(
                            text = displayPairingCode,
                            color = CreamText,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy code",
                            tint = SaffronGoldSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Text(
                        text = if (isSlovak) {
                            "Dajte tento unikátny 6-miestny kód rodinným príslušníkom. Ak ho zadajú do svojej aplikácie, zašle sa vám žiadosť o prepojenie domova."
                        } else {
                            "Dejte tento unikátní 6-místný kód rodinným příslušníkům. Jakmile jej zadají do své aplikace, zašle se vám žádost o propojení domova."
                        },
                        color = CreamText.copy(alpha = 0.7f),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }

            // 2. DOMÁCNOST DETAIL CARD
            Card(
                colors = CardDefaults.cardColors(containerColor = NavBgNatural),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, BorderNatural.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.HomeWork, contentDescription = null, tint = SaffronGoldSecondary)
                        Text(
                            text = if (isSlovak) "Aktuálna domácnosť" else "Aktuální domácnost",
                            color = CreamText,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }

                    Text(
                        text = if (householdId == "NK-FAMILY-DEMO123" || householdId.isBlank()) {
                            if (isSlovak) "Máte samostatný lokálny účet (Demo domov)." else "Máte samostatný lokální účet (Demo domov)."
                        } else {
                            householdId
                        },
                        color = if (householdId == "NK-FAMILY-DEMO123") CreamText.copy(alpha = 0.5f) else FreshGreenPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = onNavigateToFamily,
                        colors = ButtonDefaults.buttonColors(containerColor = SaffronGoldSecondary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Group, contentDescription = null, tint = Color.Black)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isSlovak) "Spravovať rodinné prepojenia" else "Spravovat rodinná propojení",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // Logout card
            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    viewModel.logoutUser()
                    onClose()
                },
                colors = ButtonDefaults.buttonColors(containerColor = TomatoRedTertiary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = "Logout", tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isSlovak) "Odhlásiť sa z účtu" else "Odhlásit se z účtu",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
