package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsOverlayScreen(
    onClose: () -> Unit,
    isSlovak: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = DarkSurface
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            CenterAlignedTopAppBar(
                title = { Text(if (isSlovak) "Nastavenia & Studio" else "Nastavení & Studio", color = CreamText, fontWeight = FontWeight.Bold) },
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
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Info Banner
                Card(
                    colors = CardDefaults.cardColors(containerColor = SoftGreenGlow.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = FreshGreenPrimary, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "O StepIn Tech",
                            color = CreamText,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Verze v1.0",
                            color = SaffronGoldSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (isSlovak) 
                                "Ahoj! Volám sa Štěpán. Môj vstup do sveta technológií začal obyčajnou zvedavosťou počas štúdií – chcel som zistiť, ako môžeme pomôcť ľuďom lepšie žiť moderné technológie. NutriKalk vznikol ako slobodný projekt zameraný na reálnu podporu rodiny a jednotlivcov v sledovaní výživy.\n\nAk máš akékoľvek nápady alebo by si chcel spolupracovať na budúcich vylepšeniach, ozvi sa mi priamo na e-mail: stepintech.cz@gmail.com" 
                                else "Ahoj! Jmenuji se Štěpán. Můj vstup do světa technologií začal obyčejnou zvědavostí během studií – chtěl jsem zjistit, jak můžeme pomoci lidem lépe žít moderní technologie. NutriKalk vznikl jako svobodný projekt zaměřený na reálnou podporu rodiny a jednotlivců ve sledování výživy.\n\nPokud máš jakékoliv nápady nebo bys chtěl spolupracovat na budoucích vylepšeních, ozvi se mi přímo na e-mail: stepintech.cz@gmail.com",
                            color = CreamText.copy(alpha = 0.9f),
                            fontSize = 14.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }

                // Chystáme (Roadmap)
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkBg),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text("Co chystáme", fontWeight = FontWeight.Bold, color = CreamText, fontSize = 18.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = false, onCheckedChange = null)
                            Text("Připravujeme: AI Kalkulačka nákladů", color = CaptionTextNatural)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = false, onCheckedChange = null)
                            Text("Připravujeme: Spotify integrace", color = CaptionTextNatural)
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))
                
                Button(
                    onClick = { /* Odkaz na web */ },
                    colors = ButtonDefaults.buttonColors(containerColor = TomatoRedTertiary),
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Notifications, contentDescription = null, tint = DarkBg)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sledovat novinky", color = DarkBg, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
