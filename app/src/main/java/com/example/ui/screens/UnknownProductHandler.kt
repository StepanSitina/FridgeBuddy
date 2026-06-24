package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnknownProductHandler(
    initialName: String = "",
    barcode: String = "",
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var productName by remember { mutableStateOf(initialName) }
    var quantity by remember { mutableStateOf("") }
    
    // Optional backend logging directly in background
    val firestore = try {
        FirebaseFirestore.getInstance()
    } catch (e: Exception) {
        null
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Science, contentDescription = null, tint = SaffronGoldSecondary)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Pomoz nám vylepšit NutriKalk",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = CreamText
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "Náš AI skener produkt zatím nezná. Zadej jeho údaje a získej +10 StepInTech bodů!",
                    color = CaptionTextNatural,
                    fontSize = 14.sp
                )
                
                OutlinedTextField(
                    value = productName,
                    onValueChange = { productName = it },
                    label = { Text("Název produktu", color = CreamText.copy(alpha=0.7f)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = CreamText,
                        unfocusedTextColor = CreamText,
                        focusedBorderColor = FreshGreenPrimary,
                        unfocusedBorderColor = BorderNatural
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Množství/Balení (např. 250g)", color = CreamText.copy(alpha=0.7f)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = CreamText,
                        unfocusedTextColor = CreamText,
                        focusedBorderColor = FreshGreenPrimary,
                        unfocusedBorderColor = BorderNatural
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    sendContribution(context, productName, quantity, barcode, firestore)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = FreshGreenPrimary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Send, contentDescription = null, tint = DarkBg, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Odeslat ke schválení do StepInTech", color = DarkBg, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Zrušit", color = CaptionTextNatural)
            }
        }
    )
}

private fun sendContribution(
    context: Context,
    productName: String,
    quantity: String,
    barcode: String,
    firestore: FirebaseFirestore?
) {
    val jsonPayload = """
        {
            "barcode": "$barcode",
            "detectedName": "$productName",
            "quantity": "$quantity",
            "timestamp": ${System.currentTimeMillis()},
            "userId": "unknown_local"
        }
    """.trimIndent()

    // 1. Odeslání e-mailu (Background via ACTION_SENDTO)
    val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("mailto:stepintech.cz@gmail.com")
        putExtra(Intent.EXTRA_SUBJECT, "[Neznámý produkt - NutriKalk]")
        putExtra(Intent.EXTRA_TEXT, "Dobrý den,\n\npřidávám návrh na nový produkt do databáze NutriKalk:\n\n$jsonPayload")
    }

    try {
        context.startActivity(Intent.createChooser(emailIntent, "Odeslat návrh"))
        Toast.makeText(context, "Děkujeme! Tvoje data pomáhají vylepšit naši databázi. Získáváš +10 bodů do StepInTech profilu.", Toast.LENGTH_LONG).show()
    } catch (e: Exception) {
        Toast.makeText(context, "E-mailový klient nebyl nalezen, ukládám lokálně.", Toast.LENGTH_SHORT).show()
    }

    // 2. Volitelné: Zápis do Firestore - PendingProducts
    try {
        val mappedData = mapOf(
            "barcode" to barcode,
            "ocrResult" to productName,
            "quantity" to quantity,
            "timestamp" to System.currentTimeMillis()
        )
        firestore?.collection("PendingProducts")?.add(mappedData)
    } catch (e: Exception) {
        // Ignore failure
    }
}
