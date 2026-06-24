package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun TutorialScreen(
    onFinish: () -> Unit,
    isSlovak: Boolean
) {
    var currentPage by remember { mutableStateOf(0) }
    val totalPages = 3

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = DarkBg
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.weight(1f))
            
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(SoftGreenGlow.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                when (currentPage) {
                    0 -> Icon(Icons.Default.QrCode, contentDescription = null, tint = FreshGreenPrimary, modifier = Modifier.size(64.dp))
                    1 -> Icon(Icons.Default.RestaurantMenu, contentDescription = null, tint = FreshGreenPrimary, modifier = Modifier.size(64.dp))
                    2 -> Icon(Icons.Default.History, contentDescription = null, tint = FreshGreenPrimary, modifier = Modifier.size(64.dp))
                }
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            Text(
                text = when (currentPage) {
                    0 -> if (isSlovak) "Vitaj v NutriKalku!" else "Vítej v NutriKalku!"
                    1 -> if (isSlovak) "AI Kulinársky asistent" else "AI Kulinářský asistent"
                    else -> if (isSlovak) "Rodinná synchronizácia" else "Rodinná synchronizace"
                },
                color = CreamText,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = when (currentPage) {
                    0 -> if (isSlovak) "Pozvi rodinu cez QR kód a zdieľajte jednu chladničku." else "Pozvi rodinu přes QR kód a sdílejte jednu lednici."
                    1 -> if (isSlovak) "Nerieš, čo uvariť. StepInTech AI vyberie recept z toho, čo máš v chladničke." else "Neřeš, co uvařit. StepInTech AI vybere recept z toho, co máš v lednici."
                    else -> if (isSlovak) "Sleduj, kto čo nakúpil a kto práve varí v reálnom čase." else "Sledujte, kdo co nakoupil a kdo zrovna vaří v reálném čase."
                },
                color = CreamText.copy(alpha = 0.8f),
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Indicators
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 0 until totalPages) {
                    Box(
                        modifier = Modifier
                            .size(if (i == currentPage) 10.dp else 8.dp)
                            .clip(CircleShape)
                            .background(if (i == currentPage) FreshGreenPrimary else BorderNatural)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = { 
                    if (currentPage < totalPages - 1) {
                        currentPage++
                    } else {
                        onFinish()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = FreshGreenPrimary),
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (currentPage < totalPages - 1) (if (isSlovak) "Ďalej" else "Dále") else (if (isSlovak) "Začať" else "Začít"),
                    color = DarkBg,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
