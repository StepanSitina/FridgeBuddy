package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsOverlayScreen(
    onClose: () -> Unit,
    isSlovak: Boolean
) {
    val context = LocalContext.current
    var showAboutDialog by remember { mutableStateOf(false) }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            containerColor = DarkSurface,
            shape = RoundedCornerShape(20.dp),
            title = {
                Text(
                    text = "O StepIn Tech",
                    color = CreamText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = if (isSlovak)
                            "Ahoj! Volám sa Štěpán a NutriKalk vznikol z jedného náhodného nápadu počas môjho štúdia programovania. Chcel som vytvoriť niečo, čo bude ľuďom reálne pomáhať. Tak vzniklo moje štúdio StepIn Tech – názov v sebe skrýva nielen kúsok môjho mena, ale aj symbolický „vstup" do sveta chytrých technológií.\n\nĎakujem, že NutriKalk používaš! Aplikáciu neustále posúvam ďalej, takže s radosťou a vďačnosťou privítam akúkoľvek spätnú väzbu, nápady na zlepšenie alebo hlásenie chýb."
                        else
                            "Ahoj! Jmenuji se Štěpán a NutriKalk vznikl z jednoho náhodného nápadu během mého studia programování. Chtěl jsem vytvořit něco, co bude lidem reálně pomáhat. Tak vzniklo mé studio StepIn Tech – název v sobě skrývá nejen kus mého jména, ale i symbolický „vstup" do světa chytrých technologií.\n\nDěkuji, že NutriKalk používáš! Aplikaci neustále posouvám dál, takže s radostí a vděčností uvítám jakoukoliv zpětnou vazbu, nápady na zlepšení nebo hlášení chyb.",
                        color = CreamText.copy(alpha = 0.9f),
                        fontSize = 14.sp,
                        lineHeight = 22.sp
                    )
                    Divider(color = BorderNatural)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Email, contentDescription = null, tint = FreshGreenPrimary, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "stepintech.cz@gmail.com",
                            color = FreshGreenPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:stepintech.cz@gmail.com"))
                        intent.putExtra(Intent.EXTRA_SUBJECT, "NutriKalk - Zpětná vazba")
                        try { context.startActivity(intent) } catch (_: Exception) {}
                    }
                ) {
                    Text(if (isSlovak) "Napísať e-mail" else "Napsat e-mail", color = FreshGreenPrimary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text(if (isSlovak) "Zavrieť" else "Zavřít", color = CaptionTextNatural)
                }
            }
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = DarkSurface
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        if (isSlovak) "Nastavenia & Studio" else "Nastavení & Studio",
                        color = CreamText,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Zavřít", tint = CreamText)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = DarkBg)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // --- Studio Banner ---
                Card(
                    colors = CardDefaults.cardColors(containerColor = SoftGreenGlow.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .background(FreshGreenPrimary.copy(alpha = 0.15f), RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("SI", color = FreshGreenPrimary, fontSize = 28.sp, fontWeight = FontWeight.Black)
                        }
                        Spacer(Modifier.height(12.dp))
                        Text("StepIn Tech", color = CreamText, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        Text("studio", color = SaffronGoldSecondary, fontSize = 13.sp)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = if (isSlovak) "Chytré aplikácie pre každodenný život" else "Chytré aplikace pro každodenní život",
                            color = CaptionTextNatural,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/StepanSitina/FridgeBuddy"))
                                try { context.startActivity(intent) } catch (_: Exception) {}
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = FreshGreenPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.OpenInNew, contentDescription = null, tint = DarkBg, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(if (isSlovak) "Sledovať novinky" else "Sledovat novinky", color = DarkBg, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // --- App Info ---
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkBg),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(if (isSlovak) "Informácie o aplikácii" else "Informace o aplikaci", color = CreamText, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        SettingsInfoRow(icon = Icons.Default.Info, label = if (isSlovak) "Verzia" else "Verze", value = "NutriKalk v1.0")
                        SettingsInfoRow(icon = Icons.Default.Android, label = if (isSlovak) "Platforma" else "Platforma", value = "Android")
                        SettingsInfoRow(icon = Icons.Default.SmartToy, label = "AI", value = "StepInTech AI")
                    }
                }

                // --- Co chystáme (Roadmap) ---
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkBg),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            if (isSlovak) "Čo chystáme" else "Co chystáme",
                            fontWeight = FontWeight.Bold,
                            color = CreamText,
                            fontSize = 16.sp
                        )
                        Spacer(Modifier.height(12.dp))

                        RoadmapItem(text = if (isSlovak) "Pripravujeme: AI Kalkulačka nákladov" else "Připravujeme: AI Kalkulačka nákladů")
                        RoadmapItem(text = if (isSlovak) "Pripravujeme: Spotify integrácia" else "Připravujeme: Spotify integrace")
                        RoadmapItem(text = if (isSlovak) "Pripravujeme: Skenovanie účteniek" else "Připravujeme: Skenování účtenek")
                        RoadmapItem(text = if (isSlovak) "Pripravujeme: Widget pre domovskú obrazovku" else "Připravujeme: Rozšířený widget")
                        RoadmapItem(text = if (isSlovak) "Pripravujeme: Poľské produkty (PL)" else "Připravujeme: Polské produkty (PL)")
                    }
                }

                // --- Kontakt ---
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkBg),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(if (isSlovak) "Kontakt & Podpora" else "Kontakt & Podpora", color = CreamText, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:stepintech.cz@gmail.com"))
                                intent.putExtra(Intent.EXTRA_SUBJECT, "NutriKalk - Zpětná vazba / Hlášení chyby")
                                try { context.startActivity(intent) } catch (_: Exception) {}
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SaffronGoldSecondary.copy(alpha = 0.15f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Email, contentDescription = null, tint = SaffronGoldSecondary, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                if (isSlovak) "Napísať vývojárovi" else "Napsat vývojáři",
                                color = SaffronGoldSecondary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                // --- Version + About button ---
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "NutriKalk v1.0",
                        color = CaptionTextNatural,
                        fontSize = 12.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { showAboutDialog = true },
                        border = androidx.compose.foundation.BorderStroke(1.dp, FreshGreenPrimary.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = FreshGreenPrimary, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "O StepIn Tech",
                            color = FreshGreenPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun SettingsInfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = FreshGreenPrimary, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(label, color = CaptionTextNatural, fontSize = 14.sp)
        }
        Text(value, color = CreamText, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun RoadmapItem(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Icon(
            Icons.Default.RadioButtonUnchecked,
            contentDescription = null,
            tint = SaffronGoldSecondary,
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(10.dp))
        Text(text, color = CaptionTextNatural, fontSize = 14.sp)
    }
}
