package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BmiCalculatorScreen(
    onClose: () -> Unit,
    isSlovak: Boolean
) {
    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("Muž") } // "Muž" or "Žena"
    var bmiResult by remember { mutableStateOf<Float?>(null) }
    var bmrResult by remember { mutableStateOf<Float?>(null) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = DarkSurface
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            CenterAlignedTopAppBar(
                title = { Text(if (isSlovak) "BMI & BMR Kalkulačka" else "BMI & BMR Kalkulačka", color = CreamText, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Zavřít", tint = CreamText)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = DarkBg)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Gender selection
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = if (isSlovak) "Pohlavie:" else "Pohlaví:",
                        color = CreamText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier.width(64.dp)
                    )
                    listOf("Muž", "Žena").forEach { g ->
                        val isSelected = gender == g
                        Button(
                            onClick = { gender = g },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) FreshGreenPrimary else NavBgNatural,
                                contentColor = if (isSelected) DarkBg else CreamText
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(g, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Age input
                OutlinedTextField(
                    value = age,
                    onValueChange = { age = it.filter { c -> c.isDigit() } },
                    label = { Text(if (isSlovak) "Vek (roky)" else "Věk (roky)", color = CreamText.copy(alpha=0.7f)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = FreshGreenPrimary,
                        unfocusedBorderColor = BorderNatural,
                        focusedTextColor = CreamText,
                        unfocusedTextColor = CreamText
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = height,
                    onValueChange = { height = it },
                    label = { Text(if (isSlovak) "Výška (cm)" else "Výška (cm)", color = CreamText.copy(alpha=0.7f)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = FreshGreenPrimary,
                        unfocusedBorderColor = BorderNatural,
                        focusedTextColor = CreamText,
                        unfocusedTextColor = CreamText
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    label = { Text(if (isSlovak) "Váha (kg)" else "Váha (kg)", color = CreamText.copy(alpha=0.7f)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = FreshGreenPrimary,
                        unfocusedBorderColor = BorderNatural,
                        focusedTextColor = CreamText,
                        unfocusedTextColor = CreamText
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        val h = height.toFloatOrNull()
                        val w = weight.toFloatOrNull()
                        val a = age.toIntOrNull()
                        if (h != null && w != null && h > 0) {
                            val hMeters = h / 100f
                            bmiResult = w / (hMeters * hMeters)
                            if (a != null && a > 0) {
                                bmrResult = if (gender == "Muž") {
                                    (10f * w) + (6.25f * h) - (5f * a) + 5f
                                } else {
                                    (10f * w) + (6.25f * h) - (5f * a) - 161f
                                }
                            } else {
                                bmrResult = null
                            }
                        } else {
                            bmiResult = null
                            bmrResult = null
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = FreshGreenPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(if (isSlovak) "Spočítať BMI & BMR" else "Spočítat BMI & BMR", color = DarkBg, fontWeight = FontWeight.Bold)
                }

                if (bmiResult != null) {
                    val status = when {
                        bmiResult!! < 18.5f -> if (isSlovak) "Podváha" else "Podváha"
                        bmiResult!! < 25f -> if (isSlovak) "Normálna váha" else "Normální váha"
                        bmiResult!! < 30f -> if (isSlovak) "Nadváha" else "Nadváha"
                        else -> "Obezita"
                    }
                    val roundedBmi = (bmiResult!! * 10f).roundToInt() / 10f

                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkBg),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Výsledné BMI", fontSize = 14.sp, color = CaptionTextNatural)
                                    Text("$roundedBmi", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = FreshGreenPrimary)
                                    Text(status, fontSize = 16.sp, color = CreamText, fontWeight = FontWeight.Medium)
                                }
                                if (bmrResult != null) {
                                    val roundedBmr = bmrResult!!.roundToInt()
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(if (isSlovak) "Denná spotreba (BMR)" else "Denní spotřeba (BMR)", fontSize = 14.sp, color = CaptionTextNatural)
                                        Text("$roundedBmr", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = SaffronGoldSecondary)
                                        Text("kcal/deň", fontSize = 14.sp, color = CreamText)
                                    }
                                }
                            }
                            if (bmrResult != null) {
                                Divider(color = BorderNatural.copy(alpha=0.4f), modifier = Modifier.padding(vertical=4.dp))
                                Text(
                                    text = if (isSlovak) {
                                        "Odporúčaný príjem pre udržanie váhy s ohľadom na vek ($age rokov) a pohlavie ($gender)."
                                    } else {
                                        "Doporučený příjem pro udržení váhy s ohledem na věk ($age let) a pohlaví ($gender)."
                                    },
                                    fontSize = 11.sp,
                                    color = CaptionTextNatural,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
