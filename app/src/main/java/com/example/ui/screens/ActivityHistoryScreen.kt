package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.HouseholdManager
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityHistoryScreen(
    householdManager: HouseholdManager,
    householdId: String,
    onClose: () -> Unit,
    isSlovak: Boolean
) {
    val activityStream = remember(householdId) { 
        householdManager.syncActivityLogLive(householdId)
    }.collectAsState(initial = emptyList()).value

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = DarkSurface
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            CenterAlignedTopAppBar(
                title = { Text(if (isSlovak) "Historické Logy" else "Historické Logy", color = CreamText, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Zavřít", tint = CreamText)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = DarkBg)
            )

            if (activityStream.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = if (isSlovak) "Zatiaľ žiadna aktivita." else "Zatím žádná aktivita.",
                        color = CreamText.copy(alpha = 0.5f)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(activityStream.take(50)) { log ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = DarkBg),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.History, contentDescription = null, tint = SoftGreenGlow)
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(text = log.userName, color = CaptionTextNatural, fontSize = 12.sp)
                                    Text(
                                        text = householdManager.formatActivityLog(log, isSlovak),
                                        color = CreamText,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
