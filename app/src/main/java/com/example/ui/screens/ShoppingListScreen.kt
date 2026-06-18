package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ShoppingItem
import com.example.ui.theme.*
import com.example.ui.viewmodel.FridgeBuddyViewModel

@Composable
fun ShoppingListScreen(
    viewModel: FridgeBuddyViewModel,
    modifier: Modifier = Modifier
) {
    val isSlovak by viewModel.isSlovak.collectAsState()
    val shoppingItems by viewModel.shoppingItems.collectAsState()

    var showQuickAdder by remember { mutableStateOf(false) }
    var newItemName by remember { mutableStateOf("") }
    var newItemQty by remember { mutableStateOf("1 ks") }
    var newItemStore by remember { mutableStateOf("Lidl") }
    var newItemPrice by remember { mutableStateOf("25") }

    val currency = if (isSlovak) "EUR" else "Kč"

    // Group items by store for ergonomic CZ/SK supermarket shopping trips
    val groupedItems = shoppingItems.groupBy { it.targetStore }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBg)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        // 1. Header block
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurface)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isSlovak) "Chytrý Nákupný Zoznam" else "Chytrý Nákupní Seznam",
                    color = FreshGreenPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                
                IconButton(onClick = { showQuickAdder = !showQuickAdder }) {
                    Icon(
                        if (showQuickAdder) Icons.Default.Close else Icons.Default.AddCircle,
                        contentDescription = "Toggle add",
                        tint = SaffronGoldSecondary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            Text(
                text = if (isSlovak) {
                    "Zoradené podľa predajní s odhadovaným rozpočtom."
                } else {
                    "Seřazené podle prodejen s odhadovaným rozpočtem."
                },
                color = CreamText.copy(alpha = 0.7f),
                fontSize = 12.sp
            )
        }

        // 2. Sliding Quick Adder Menu
        AnimatedVisibility(visible = showQuickAdder) {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = if (isSlovak) "Rýchle pridanie položky:" else "Rychlé přidání položky:",
                        color = CreamText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    OutlinedTextField(
                        value = newItemName,
                        onValueChange = { newItemName = it },
                        label = { Text(if (isSlovak) "Čo chcete kúpiť (napr. Vajcia)" else "Co chcete koupit (např. Vejce)") },
                        modifier = Modifier.fillMaxWidth().testTag("shopping_name_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = CreamText,
                            unfocusedTextColor = CreamText
                        )
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = newItemQty,
                            onValueChange = { newItemQty = it },
                            label = { Text(if (isSlovak) "Množstvo" else "Množství") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = newItemPrice,
                            onValueChange = { newItemPrice = it },
                            label = { Text("Cena ($currency)") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Store selector presets
                    Text(
                        text = if (isSlovak) "Cieľová predajňa (Affiliate linky):" else "Cílová prodejna (Affiliate linky):",
                        color = CreamText.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val localStores = listOf("Lidl", "Albert", "Rohlík.cz", "Tesco")
                        localStores.forEach { store ->
                            Button(
                                onClick = { newItemStore = store },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (newItemStore == store) FreshGreenPrimary else DarkBg
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(30.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(store, fontSize = 10.sp)
                            }
                        }
                    }

                    Button(
                        onClick = {
                            if (newItemName.isNotBlank()) {
                                viewModel.addShoppingItem(
                                    newItemName,
                                    newItemQty,
                                    newItemStore,
                                    newItemPrice.toDoubleOrNull() ?: 25.0
                                )
                                newItemName = ""
                                showQuickAdder = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = FreshGreenPrimary),
                        modifier = Modifier.fillMaxWidth().testTag("save_shopping_item_btn")
                    ) {
                        Text(if (isSlovak) "Pridovať na zoznam" else "Přidat do seznamu", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Budget summary calculator widget
        val totalEstimateBudget = shoppingItems.filter { !it.isPurchased }.sumOf { it.priceEstimate }
        Card(
            colors = CardDefaults.cardColors(containerColor = FreshGreenPrimary.copy(alpha = 0.1f)),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, BorderNatural),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Calculate, contentDescription = "Budget", tint = SaffronGoldSecondary, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isSlovak) "Celkový odhadovaný nákupný rozpočet:" else "Celkový odhadovaný nákupní rozpočet:",
                        color = CreamText.copy(alpha = 0.8f),
                        fontSize = 12.sp
                    )
                }
                Text(
                    text = "${String.format("%.2f", totalEstimateBudget)} $currency",
                    color = SaffronGoldSecondary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }

        // 3. Lazy Column divided by supermarket headings
        if (shoppingItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.LocalMall, contentDescription = "Nothing to buy", tint = CreamText.copy(alpha = 0.2f), modifier = Modifier.size(64.dp))
                    Text(
                        text = if (isSlovak) "Zoznam nakupovania je prázdny." else "Seznam nakupování je prázdný.",
                        color = CreamText.copy(alpha = 0.5f),
                        fontSize = 14.sp
                    )
                    Text(
                        text = if (isSlovak) "Chýbajúce receptové položky sa tu zídu automaticky." else "Chybějící receptové položky se sem sesypou automaticky.",
                        color = CreamText.copy(alpha = 0.3f),
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 16.dp, start = 16.dp, end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                groupedItems.forEach { (store, itemsInStore) ->
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🛒 Supermarket: $store",
                                color = SaffronGoldSecondary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            val subBudget = itemsInStore.filter { !it.isPurchased }.sumOf { it.priceEstimate }
                            Text(
                                text = "${String.format("%.2f", subBudget)} $currency",
                                color = CreamText.copy(alpha = 0.5f),
                                fontSize = 11.sp
                            )
                        }
                    }

                    items(itemsInStore) { item ->
                        ShoppingItemRow(
                            item = item,
                            onToggle = { viewModel.toggleShoppingItemPurchased(item) },
                            onDelete = { viewModel.deleteShoppingItem(item) },
                            currency = currency
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ShoppingItemRow(
    item: ShoppingItem,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    currency: String
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, BorderNatural),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Checkbox(
                    checked = item.isPurchased,
                    onCheckedChange = { onToggle() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = FreshGreenPrimary,
                        uncheckedColor = CreamText.copy(alpha = 0.4f)
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    Text(
                        text = item.name,
                        color = if (item.isPurchased) CreamText.copy(alpha = 0.4f) else CreamText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        textDecoration = if (item.isPurchased) TextDecoration.LineThrough else null
                    )
                    Text(
                        text = item.neededQty,
                        color = CreamText.copy(alpha = 0.5f),
                        fontSize = 11.sp
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${item.priceEstimate} $currency",
                    color = if (item.isPurchased) CreamText.copy(alpha = 0.3f) else SaffronGoldSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    textDecoration = if (item.isPurchased) TextDecoration.LineThrough else null
                )

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.DeleteOutline,
                        contentDescription = "Delete",
                        tint = TomatoRedTertiary.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}
